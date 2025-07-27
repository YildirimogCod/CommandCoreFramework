package com.payguru.commands;

import com.payguru.framework.annotations.Execute;

public class Date {
    @Execute("/date")
    public String showDate() {
        java.util.Date date = new java.util.Date();
       return date.toString();
    }
}