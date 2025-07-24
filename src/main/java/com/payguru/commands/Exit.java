package com.payguru.commands;

import com.payguru.framework.annotations.Execute;

public class Exit {
    @Execute("exit")
    public void execute() {
        System.exit(1);
    }
}