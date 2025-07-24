package com.payguru.framework;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Component;
import com.payguru.framework.annotations.Execute;
import com.payguru.framework.annotations.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Framework başlatılıyor, komutlar taranıyor...");
        var classScanner = new ClassScanner("com.payguru"); // Tarayacağımız paket
        var allScannedClasses = classScanner.findAllClasses();

        Map<Class<?>,Object> componentRegistry = new HashMap<>();
        for (Class<?> scannedClass : allScannedClasses ) {
            if (scannedClass.isAnnotationPresent(Component.class)){
                Object componentInstance = scannedClass.getConstructor().newInstance();
                componentRegistry.put(scannedClass, componentInstance);
                System.out.println("Component bulundu ve yaratıldı: " + scannedClass.getSimpleName());
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
                        if (field.isAnnotationPresent(Value.class)|| field.isAnnotationPresent(Autowired.class)) {
                            requiredFields.add(field);
                        }
                    }
                    CommandAction action = new CommandAction(scannedClass, method, requiredFields);
                    commandMap.put(commandKey, action);
                }
            }
        }
        System.out.println("Framework hazır! Kullanılabilir komutlar: " + commandMap.keySet());

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("İşlem seçiniz > ");
            String commandName = input.next();

            if ("exit".equalsIgnoreCase(commandName)) break;
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
                                System.err.println("Hata: " + fieldType.getName() + " tipinde bir Component bulunamadı!");
                            }
                        }
                        if (field.isAnnotationPresent(Value.class)) {
                            Value annotation = field.getAnnotation(Value.class);
                            String stringValue = annotation.value();
                            Object convertedValue = convertStringToType(stringValue, field.getType());
                            field.set(commandInstance, convertedValue);
                        }
                    //Value annotation = field.getAnnotation(Value.class);
                    //System.out.print(annotation.value() + " ");
                    //String stringValue = annotation.value(); //anatasyondaki string değeri aldık.
                    /*
                    if(field.getType() == double.class) {
                        double tempValue = input.nextDouble();
                        field.set(commandInstance, tempValue);
                    }

                     */
                    //try{
                        //Object convertedValue = convertStringToType(stringValue,field.getType());
                        //field.set(commandInstance,convertedValue);
                    //}catch (NumberFormatException e) {
                        //System.err.println("Hata: '" + stringValue + "' değeri " + field.getType().getSimpleName() + " tipine dönüştürülemedi.");
                    }
                    action.methodToExecute().invoke(commandInstance);
                } else {
                System.out.println("Geçersiz komut!");
                }
            }
            input.close();
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