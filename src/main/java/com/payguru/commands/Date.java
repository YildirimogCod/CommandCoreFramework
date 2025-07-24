package com.payguru.commands;

import com.payguru.framework.annotations.Execute;

public class Date {
    @Execute("date")
    public void showDate() {
        java.util.Date date = new java.util.Date();
        System.out.println(date);
    }
}