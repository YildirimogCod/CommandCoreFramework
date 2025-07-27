package com.payguru.commands;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Execute;
import com.payguru.framework.annotations.Value;
import com.payguru.framework.annotations.components.CelsiusToKelvin;

public class Temperature {
    @Autowired
    private CelsiusToKelvin ctk;

    @Value("30")
    public double temperature;

    @Execute("/temperature")
    public String execute() {
        return this.temperature + "";
    }
}