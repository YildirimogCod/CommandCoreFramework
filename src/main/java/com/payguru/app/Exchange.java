package com.payguru.app;

import com.payguru.framework.annotations.GetMapping;
import com.payguru.framework.annotations.Value;

public class Exchange {
    @Value("45")
    public int rate;

    @GetMapping("/exchange")
    public String execute() {
        return rate + "";
    }
}