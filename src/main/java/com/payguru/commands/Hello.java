package com.payguru.commands;

import com.payguru.framework.annotations.Execute;

public class Hello {
    @Execute("hello")
    public void sayHello() {
        System.out.println("hello sweetie!");
    }
}
