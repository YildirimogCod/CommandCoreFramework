package com.payguru.framework;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {
    private final String basePackage;

    public ClassScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<Class<?>> findAllClasses() throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);

        // --- DEBUG BÖLÜMÜ BAŞLANGICI ---
        if (resource == null) {
            System.out.println("[DEBUG] HATA: Paket kaynağı bulunamadı: '" + packagePath + "'");
            return classList;
        } else {
            System.out.println("[DEBUG] Paket kaynağı bulundu: " + resource.toURI());
        }
        // --- DEBUG BÖLÜMÜ SONU ---

        File directory = new File(resource.toURI());
        scanDirectory(directory, basePackage, classList);

        return classList;
    }

    private void scanDirectory(File directory, String currentPackage, List<Class<?>> classList) {
        if (!directory.exists() || directory.listFiles() == null) {
            System.out.println("[DEBUG] Dizin bulunamadı veya boş: " + directory.getPath());
            return;
        }

        System.out.println("[DEBUG] Dizin taranıyor: " + directory.getPath());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                System.out.println("[DEBUG] Alt dizine giriliyor: " + file.getName());
                scanDirectory(file, currentPackage + "." + file.getName(), classList);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classList.add(Class.forName(className));
                    System.out.println("[DEBUG] Sınıf bulundu ve yüklendi: " + className);
                } catch (ClassNotFoundException e) {
                    System.err.println("[DEBUG] Sınıf yüklenirken hata oluştu: " + className);
                }
            }
        }
    }
}