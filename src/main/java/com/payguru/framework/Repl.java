package com.payguru.framework;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Value;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;


public class Repl extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintStream out;
    private Map<String, CommandAction> commandMap;
    private Map<Class<?>, Object> componentRegistry;

    public Repl(Socket clientSocket, Map<String, CommandAction> commandMap, Map<Class<?>, Object> componentRegistry) throws Exception {
        this.clientSocket =clientSocket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintStream(clientSocket.getOutputStream());
        this.commandMap = commandMap;
        this.componentRegistry = componentRegistry;
    }

    public void run() {
        try {
            Scanner input = new Scanner(in);
            var paragraph = "";
            var line = "";

            paragraph = "";

            do {
                line = in.readLine();
                paragraph += line + "\n" ;
            } while (!line.isEmpty());

            System.out.println(paragraph);
            System.out.println();

            String[] lines = paragraph.split("\n");
            String commandLine = lines[0];

            String parts[] = commandLine.split(" ");
            String commandName = parts[1];

            CommandAction action = commandMap.get(commandName);

            if (action != null) {
                Object commandInstance = action.commandClass().getConstructor().newInstance();
                for (Field field : action.requiredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Class<?> fieldType = field.getType();
                        Object serviceToInject = componentRegistry.get(fieldType);
                        if (serviceToInject != null) {
                            field.set(commandInstance, serviceToInject);
                        } else {
                            out.println("Hata: " + fieldType.getName() + " tipinde bir Component bulunamadı!");
                        }
                    }
                    if (field.isAnnotationPresent(Value.class)) {
                        Value annotation = field.getAnnotation(Value.class);
                        String stringValue = annotation.value();
                        Object convertedValue = convertStringToType(stringValue, field.getType());
                        field.set(commandInstance, convertedValue);
                    }
                }   

                var result = (String) action.methodToExecute().invoke(commandInstance);
                out.println("HTTP/1.0 200 OK");
                out.println("");
                out.println(result);
            } else {
                Path path = Path.of("D:/www" + commandName);

                if (Files.exists(path)) {
                    byte[] content = Files.readAllBytes(path);
                    out.println("HTTP/1.0 200 OK");
                    out.println("");
                    out.write(content);
                } else {
                    out.println("HTTP/1.0 404 Not Found");
                    out.println("");
                }
            }

            out.flush();
            clientSocket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertStringToType(String value, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == String.class) {
            return value;
        }

        throw new IllegalArgumentException("Desteklenmeyen tip: " + targetType.getName());
    }
}
