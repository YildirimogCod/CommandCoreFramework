package com.payguru.framework.annotations.components;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Component;

@Component
public class CurrencyConverterService {
    @Autowired
    private ExchangeRateProviderService rateProvider;


    public double convertToTurkishLira(double dolarAmount) {
        double currentRate = rateProvider.getCurrentRate();
        return dolarAmount * currentRate;
    }
}
