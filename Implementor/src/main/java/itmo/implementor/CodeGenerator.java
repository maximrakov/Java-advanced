package itmo.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeGenerator {

    public static String generatePackage(Class<?> loadedClass) {
        return "package " + loadedClass.getPackageName() + ";\n";
    }

    public static String generateClassDeclaration(Class<?> loadedClass) {
        String modifiersString = getModifiers(loadedClass.getModifiers());
        Class<?> superClass = loadedClass.getSuperclass();
        Class<?>[] interfaces = loadedClass.getInterfaces();
        StringBuilder interfacesString = new StringBuilder();
        Arrays.stream(interfaces).forEach(e -> interfacesString.append(e.getSimpleName() + ", "));
        if(interfaces.length != 0) {
            interfacesString.delete(interfacesString.length() - 2, interfacesString.length());
        }
        return modifiersString + " class " + loadedClass.getSimpleName() + "Impl"
                + (superClass != null ? (" extends " + superClass.getSimpleName()) : " ")
                + ((interfacesString.toString().length() != 0) ? " implements " + interfacesString.toString() : "" )+ " { \n";
    }

    public static String generateFields(Class<?> loadedClass) {
        Field[] fields = loadedClass.getDeclaredFields();
        StringBuilder fieldsCode = new StringBuilder();
        Arrays.stream(fields).forEach(e -> {
            fieldsCode.append(getModifiers(e.getModifiers()) + e.getType().getSimpleName() + " " + e.getName() + " = " + primitive(e.getType().getSimpleName()) + ";\n");
        });
        return fieldsCode.toString();
    }

    public static String generateMethods(Class<?> loadedClass) {
        Method[] methods = loadedClass.getDeclaredMethods();
        StringBuilder methodsCode = new StringBuilder();
        Arrays.stream(methods).forEach(e -> {
            methodsCode.append(getModifiers(e.getModifiers()) + e.getReturnType().getSimpleName() + " " + e.getName()
                    + generateArguments(e.getParameterTypes()) + " " + generateExceptions(e.getExceptionTypes()) + " {\n" +
                    "\t return"+ primitive(e.getReturnType().getSimpleName())+";\n"
                    +"}\n");
        });
        return methodsCode.toString();
    }

    public static String generateConstructors(Class<?> loadedClass) {
        Constructor[] constructors = loadedClass.getConstructors();
        StringBuilder constructorsCode = new StringBuilder();
        Arrays.stream(constructors).forEach(
                e -> {
                    constructorsCode.append(getModifiers(e.getModifiers())
                            + loadedClass.getSimpleName() + "" + generateArguments(e.getParameterTypes()) +
                            generateExceptions(e.getExceptionTypes())+ " {\n" +
                            "}\n");
                }
        );
        return constructorsCode.toString();
    }

    private static String generateExceptions(Class<?>[] exceptionTypes) {
        if(exceptionTypes.length == 0){
            return "";
        }
        StringBuilder exceptions = new StringBuilder("throws ");
        Arrays.stream(exceptionTypes).forEach(
                e -> {
                    exceptions.append(e.getSimpleName() + ", ");
                }
        );
        if(exceptionTypes.length != 0) {
            exceptions.delete(exceptions.length() - 2, exceptions.length());
        }
        return exceptions.toString();
    }

    private static String generateArguments(Class<?>[] argumentTypes) {
        StringBuilder arguments = new StringBuilder();
        arguments.append("(");
        AtomicInteger count = new AtomicInteger(0);
        Arrays.stream(argumentTypes).forEach(
                e -> {
                    arguments.append(e.getSimpleName() + " parameter" + count + ", ");
                    count.getAndIncrement();
                }
        );
        if(argumentTypes.length != 0) {
            arguments.delete(arguments.length() - 2, arguments.length());
        }
        arguments.append(")");
        return arguments.toString();
    }

    private static String getModifiers(int classModifiers) {
        StringBuilder modifiersString = new StringBuilder();
        for(Map.Entry<Integer, String> entry: modifiers.entrySet()) {
            if((classModifiers & entry.getKey()) != 0) {
                if(entry.getValue().equals("interface") || entry.getValue().equals("abstract")) {
                    continue;
                }
                modifiersString.append(entry.getValue() + " ");
            }
        }
        return modifiersString.toString();
    }

    static private String primitive(String primitiveType) {
        if(primitiveType == "void") {
            return "";
        }

        if(primitiveType == "boolean") {
            return " false";
        }
        if(!primitives.contains(primitiveType)) {
            return "null";
        }
        return " 0";
    }

    static Map<Integer, String> modifiers = new HashMap<>() {{
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

    static Set<String> primitives = new HashSet<String>(){{
        add("byte");
        add("short");
        add("int");
        add("long");
        add("float");
        add("double");
        add("char");
        add("boolean");
        add("void");
    }};
}
