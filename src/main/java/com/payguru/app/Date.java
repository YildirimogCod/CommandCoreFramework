package com.payguru.app;

import com.payguru.framework.annotations.GetMapping;

public class Date {
    @GetMapping("/date")
    public String showDate() {
        java.util.Date date = new java.util.Date();
       return date.toString();
    }
}