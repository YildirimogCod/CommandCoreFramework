package com.payguru.framework;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Component;
import com.payguru.framework.annotations.Execute;
import com.payguru.framework.annotations.Value;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    public void start() throws Exception {
        init();
    }

    public void init() throws Exception {
        System.out.println("Framework başlatılıyor, komutlar taranıyor...");

        var classScanner = new ClassScanner("com.payguru"); // Tarayacağımız paket
        var allScannedClasses = classScanner.findAllClasses();

        Map<Class<?>, Object> componentRegistry = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses) {
            if (scannedClass.isAnnotationPresent(Component.class)) {
                Object componentInstance = scannedClass.getConstructor().newInstance();
                componentRegistry.put(scannedClass, componentInstance);
                System.out.println("Component bulundu ve yaratıldı: " + scannedClass.getSimpleName());
            }
        }

        for (Object componentInstance : componentRegistry.values()) {
            for (Field field : componentInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    Class<?> dependencyType = field.getType();
                    Object dependencyInstance = componentRegistry.get(dependencyType);
                    field.set(componentInstance, dependencyInstance);
                }
            }
        }

        Map<String, CommandAction> commandMap = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses) {
            for (Method method : scannedClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Execute.class)) {
                    Execute executeAnnotation = method.getAnnotation(Execute.class);
                    String commandKey = executeAnnotation.value();
                    List<Field> requiredFields = new ArrayList<>();
                    for (Field field : scannedClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Value.class) || field.isAnnotationPresent(Autowired.class)) {
                            requiredFields.add(field);
                        }
                    }
                    CommandAction action = new CommandAction(scannedClass, method, requiredFields);
                    commandMap.put(commandKey, action);
                }
            }
        }

        System.out.println("Framework hazır! Kullanılabilir komutlar: " + commandMap.keySet());

        //
        //repl.start();

        ServerSocket serverSocket = new ServerSocket(80);

        while(true) {
            Socket clientSocket = serverSocket.accept();

            var repl = new Repl(clientSocket, commandMap, componentRegistry);
            repl.start();
        }
    }
}