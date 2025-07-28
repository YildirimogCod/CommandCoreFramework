package com.payguru.framework;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.QueryParam;
import com.payguru.framework.annotations.RequestBody;
import com.payguru.framework.annotations.Value;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class HttpHandler extends Thread {
    private Socket clientSocket;
    private InputStream in;
    private PrintStream out;
    private Map<String, HandlerInfo> getHandlers;
    private Map<String, HandlerInfo> postHandlers;
    private Map<Class<?>, Object> componentRegistry;

    public HttpHandler(Socket clientSocket, Map<String, HandlerInfo> getHandlers, Map<String, HandlerInfo> postHandlers, Map<Class<?>, Object> componentRegistry) throws Exception {
        this.clientSocket =clientSocket;
        this.in = clientSocket.getInputStream();
        this.out = new PrintStream(clientSocket.getOutputStream());
        this.getHandlers = getHandlers;
        this.postHandlers = postHandlers;
        this.componentRegistry = componentRegistry;
    }

    public String readLine() throws Exception {
        StringBuilder line = new StringBuilder();
        int ch;

        while ((ch = in.read()) != -1) {
            if (ch == '\n') {
                break;
            } else if (ch != '\r') {
                line.append((char) ch);
            }
        }

        return line.toString();
    }

    public void run() {
        try {
            System.out.println("\n************************************************");

            var request = "";
            var line = "";

            do {
                line = readLine();
                request += line + "\n";
            } while (!line.isEmpty());

            System.out.println(request);

            String[] lines = request.split("\n");
            String requestLine = lines[0];

            // Saçma oldugunu bizde biliyoruz
            HashMap<String, String> headers = new HashMap<>();

            for(int i = 1; i < lines.length; i++) {
                var headerLine = lines[i];
                var headerName = headerLine.split(":")[0].trim().toLowerCase();
                var headerValue = headerLine.split(":")[1].trim();
                headers.put(headerName, headerValue);
            }

            String requestBody = "";
            HashMap<String, String> requestParameters = new HashMap<>();

            if(headers.containsKey("content-length")) {
                byte buffer[] = new byte[Integer.parseInt(headers.get("content-length"))];
                in.read(buffer);

                requestBody = new String(buffer, StandardCharsets.UTF_8);
                System.out.println(requestBody);
                System.out.println();
                System.out.println();

                if(headers.containsKey("application/x-www-form-urlencoded")) {
                    for (String pair : requestBody.split("&")) {
                        String key = pair.split("=")[0];
                        String value = pair.split("=")[1];
                        requestParameters.put(key, value);
                    }
                }
            }

            String parts[] = requestLine.split(" ");
            String requestTarget = parts[1];
            String method = parts[0];

            String path, queryString = null;
            HashMap<String, String> getParameters = new HashMap<>();

            if(!requestTarget.contains("?")) {
                path = requestTarget;
            } else {
                path = requestTarget.substring(0, requestTarget.indexOf('?'));
                queryString = requestTarget.substring(requestTarget.indexOf('?') + 1);

                for (String pair : queryString.split("&")) {
                    String key = pair.split("=")[0];
                    String value = pair.split("=")[1];
                    getParameters.put(key, value);
                }
            }

            System.out.println("Method = " + method);
            System.out.println("Path = " + path);
            System.out.println("Query String = " + queryString);
            System.out.println("Parameters = " + getParameters);
            System.out.println("headers = " + headers);
            System.out.println("requestBody = " + requestBody);
            System.out.println();

            HandlerInfo handler;

            if(method.equals("GET")) {
                handler = getHandlers.get(path);
            } else {
                handler = postHandlers.get(path);
            }

            if (handler != null) {
                Class<?> handlerClass = handler.commandClass();
                Constructor<?> handlerConstructor = handlerClass.getConstructor();
                Object handlerInstance = handlerConstructor.newInstance();

                for (Field field : handler.requiredFields()) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(Autowired.class)) {
                        Class<?> fieldType = field.getType();
                        Object serviceToInject = componentRegistry.get(fieldType);
                        if (serviceToInject != null) {
                            field.set(handlerInstance, serviceToInject);
                        } else {
                            out.println("Hata: " + fieldType.getName() + " tipinde bir Component bulunamadı!");
                        }
                    }

                    if (field.isAnnotationPresent(Value.class)) {
                        Value annotation = field.getAnnotation(Value.class);
                        String stringValue = annotation.value();
                        Object convertedValue = convertStringToType(stringValue, field.getType());
                        field.set(handlerInstance, convertedValue);
                    }
                }

                Method handlerMethod = handler.methodToExecute();
                Parameter[] handlerParameters = handlerMethod.getParameters();

                Object result;
                if(handlerParameters.length == 0) {
                    result = handlerMethod.invoke(handlerInstance);
                } else {
                    QueryParam queryParam = handlerParameters[0].getAnnotation(QueryParam.class);
                    if(queryParam != null) {
                        String paramName = queryParam.value();
                        String param = getParameters.get(paramName);
                        result = handlerMethod.invoke(handlerInstance, param);
                    } else {
                        RequestBody reqBodyAnnotation = handlerParameters[0].getAnnotation(RequestBody.class);
                        if(reqBodyAnnotation != null) {
                            result = handlerMethod.invoke(handlerInstance, requestBody);
                        } else {
                            result = "";
                        }
                    }
                }

                out.println("HTTP/1.0 200 OK");
                out.println("Content-Length: " + result.toString().getBytes(StandardCharsets.UTF_8).length);
                out.println("");
                out.write(result.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                Path filePath;

                if(!"example.com".equalsIgnoreCase(headers.get("host"))) {
                    filePath = Path.of("D:/example.com" + path);
                } else {
                    filePath = Path.of("D:/www" + path);
                }


                if (Files.exists(filePath)) {
                    byte[] content = Files.readAllBytes(filePath);
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Length:" + content.length);
                    out.println("");
                    out.write(content);
                } else {
                    out.println("HTTP/1.0 404 Not Found");
                    out.println("Content-Length: 0");
                    out.println("");
                }
            }

            //if(!"keep-alive".equalsIgnoreCase(headers.get("connection"))) {
                out.flush();
                clientSocket.close();
            //}
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
