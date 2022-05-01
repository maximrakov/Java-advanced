package walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecursiveWalk {
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Expected 2 arguments");
            return;
        }
        File input = new File(args[0]);
        File output = new File(args[1]);
        if(!Files.exists(Path.of(output.getParent()))){
            try {
                Files.createDirectories(Path.of(output.getParent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(args[0]))) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(args[1]))) {
                String currentDir;
                FileVisitorImpl fileVisitor = new FileVisitorImpl(bufferedWriter);
                while ((currentDir = bufferedReader.readLine()) != null) {
                    Files.walkFileTree(Path.of(currentDir), fileVisitor);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File is not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
