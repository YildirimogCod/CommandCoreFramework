package com.payguru.framework.annotations.components;

import com.payguru.framework.annotations.Component;

@Component
public class ExchangeRateProviderService {
        public double getCurrentRate() {
        return 40.0;
    }
}
