package com.payguru.framework.annotations.components;

import com.payguru.framework.annotations.Component;

@Component
public class
CelsiusToKelvin {
    private final double kelvin = 273.15;
    public double celsiusToKelvinService(double celsius){
        return celsius + kelvin;
    }

}
