package com.payguru.commands;

import com.payguru.framework.annotations.Autowired;
import com.payguru.framework.annotations.Execute;
import com.payguru.framework.annotations.Value;
import com.payguru.framework.annotations.components.CurrencyConverterService;

public class Exchange {
    @Autowired
    private CurrencyConverterService service;

    @Value("45")
    public int amount;

    @Execute("exchange")
    public void execute() {
        // Artık hesaplama işini kendisi yapmıyor, uzmana (servise) yaptırıyor.
        if (service == null) {
            System.err.println("HATA: CurrencyConverterService enjekte edilemedi!");
            return;
        }
        double tlResult = service.convertToTurkishLira(this.amount);
        System.out.println(this.amount + "$ = " + tlResult + "₺");
    }
}