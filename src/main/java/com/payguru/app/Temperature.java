package com.payguru.app;

import com.payguru.framework.annotations.GetMapping;
import com.payguru.framework.annotations.Value;

public class Temperature {
    @Value("30")
    public double temperature;

    @GetMapping("/temperature")
    public String execute() {
        return this.temperature + "";
    }
}