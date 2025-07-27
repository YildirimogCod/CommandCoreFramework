package com.payguru.commands;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Execute;
import com.payguru.framework.annotations.Value;
import com.payguru.framework.annotations.components.CurrencyConverterService;

public class Exchange {
    @Autowired
    private CurrencyConverterService service;

    @Value("45")
    public int rate;

    @Execute("/exchange")
    public String execute() {
        return rate + "";
    }
}