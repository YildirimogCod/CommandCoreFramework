package com.payguru.framework;

import com.payguru.framework.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {
    public void start() throws Exception {
        init();
    }

    public void init() throws Exception {
        System.out.println("Framework başlatılıyor, komutlar taranıyor...");

        var classScanner = new ClassScanner("com.payguru.app"); // Tarayacağımız paket
        var allScannedClasses = classScanner.findAllClasses();

        Map<Class<?>, Object> componentRegistry = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses) {
            if (scannedClass.isAnnotationPresent(Component.class)) {
                System.out.println(scannedClass);
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

        Map<String, HandlerInfo> getHandlerMap = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses) {
            for (Method method : scannedClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping getMappingAnnotation = method.getAnnotation(GetMapping.class);
                    String commandKey = getMappingAnnotation.value();
                    List<Field> requiredFields = new ArrayList<>();
                    for (Field field : scannedClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Value.class) || field.isAnnotationPresent(Autowired.class)) {
                            requiredFields.add(field);
                        }
                    }
                    HandlerInfo action = new HandlerInfo(scannedClass, method, requiredFields);
                    getHandlerMap.put(commandKey, action);
                }
            }
        }

        System.out.println("Framework hazır! Kullanılabilir GET yolları: " + getHandlerMap.keySet());

        Map<String, HandlerInfo> postHandlerMap = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses) {
            for (Method method : scannedClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostMapping.class)) {
                    PostMapping postMappingAnnotation = method.getAnnotation(PostMapping.class);
                    String handlerPath = postMappingAnnotation.value();
                    List<Field> requiredFields = new ArrayList<>();
                    for (Field field : scannedClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Value.class) || field.isAnnotationPresent(Autowired.class)) {
                            requiredFields.add(field);
                        }
                    }

                    HandlerInfo action = new HandlerInfo(scannedClass, method, requiredFields);
                    postHandlerMap.put(handlerPath, action);
                }
            }
        }

        System.out.println("Framework hazır! Kullanılabilir POST yolları: " + postHandlerMap.keySet());

        //
        //repl.start();

        ServerSocket serverSocket = new ServerSocket(80);

        while(true) {
            Socket clientSocket = serverSocket.accept();

            var repl = new HttpHandler(clientSocket, getHandlerMap, postHandlerMap, componentRegistry);
            repl.start();
        }
    }
}