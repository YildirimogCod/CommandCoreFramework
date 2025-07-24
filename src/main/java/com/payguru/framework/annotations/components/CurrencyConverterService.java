package com.payguru.framework.annotations.components;

import com.payguru.framework.annotations.Component;

@Component
public class CurrencyConverterService {
    private final double rate = 40;

    public double convertToTurkishLira(double dolarAmount) {
        return dolarAmount * this.rate;
    }
}
