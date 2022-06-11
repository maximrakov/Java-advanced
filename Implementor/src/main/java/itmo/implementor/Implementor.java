package itmo.implementor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Implementor {
    public static void implement(Class<?> loadedClass) {
        String packageFolder = loadedClass.getPackageName();
        packageFolder = packageFolder.replace('.','/');
        try {
            Files.createDirectories(Path.of(packageFolder));
        } catch (IOException e) {
            System.out.println("File does not exist");
        }

        try(BufferedWriter bufferedWriter =
                    new BufferedWriter(new FileWriter(packageFolder + "/" + loadedClass.getSimpleName() + "Impl.java"))){
            bufferedWriter.write(CodeGenerator.generatePackage(loadedClass));
            bufferedWriter.write(CodeGenerator.generateClassDeclaration(loadedClass));
            bufferedWriter.write(CodeGenerator.generateFields(loadedClass));
            bufferedWriter.write(CodeGenerator.generateMethods(loadedClass));
            bufferedWriter.write(CodeGenerator.generateConstructors(loadedClass));
            bufferedWriter.write("}");
        } catch (IOException e) {
            System.out.println("File does not exist");
        }
    }

    Map<Integer, String> modifiers = new HashMap<>() {{
        put(1, "public");
        put(2, "private");
        put(4, "protected");
        put(8, "static");
        put(16, "final");
        put(32, "synchronized");
        put(64, "volatile");
        put(128, "transient");
        put(256, "native");
        put(512, "interface");
        put(1024, "abstract");
    }};

    public static void main(String[] args) {
        Class<?> loadedClass = null;
        try {
            loadedClass = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class isn't exist");
        }
        implement(loadedClass);
    }
}
