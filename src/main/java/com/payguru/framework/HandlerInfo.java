package com.payguru.framework;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public record HandlerInfo(
        Class<?> commandClass, //ör:Temperature.class
        Method methodToExecute,//Çalıştırılacak method
        List<Field> requiredFields //Değer girilmesi gereken alanlar
) {
}
