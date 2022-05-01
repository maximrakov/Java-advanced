package walk;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitorImpl implements FileVisitor<Path> {
    private final int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    private final long initialValue = 0L;
    private BufferedWriter bufferedWriter;

    public FileVisitorImpl(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        long currentHash = initialValue;
        try(InputStream inputStreamReader = new FileInputStream(file.toString())){
            int count = 0;
            while((count = inputStreamReader.read(buffer)) >= 0) {
                currentHash = hashPjw(currentHash, count);
            }
        } catch (IOException | InvalidPathException e) {
            currentHash = 0;
        } finally {
            bufferedWriter.write(String.format("%016x %s\n", currentHash, file.toString()));
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        bufferedWriter.write(String.format("%016x %s\n", 0L, file.toString()));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private long hashPjw(final long prevHash, final int size) {
        final long bitesSize = 64L;
        final long highBits = 0xFFFF_FFFF_FFFF_FFFFL << (bitesSize / 8L * 7L);
        long hash = prevHash;
        for (int i = 0; i < size; i++) {
            hash = (hash << (bitesSize / 8L)) + (buffer[i] & 0xFF);
            final long hashAndHighBits;
            if ((hashAndHighBits = hash & highBits) != 0) {
                hash ^= hashAndHighBits >> (bitesSize / 4 * 3);
                hash &= ~hashAndHighBits;
            }
        }
        return hash;
    }
}
