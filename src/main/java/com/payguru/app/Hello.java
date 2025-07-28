package com.payguru.app;

import com.payguru.framework.annotations.GetMapping;
import com.payguru.framework.annotations.PostMapping;
import com.payguru.framework.annotations.QueryParam;
import com.payguru.framework.annotations.RequestBody;

public class Hello {
    int i = 0;

    @GetMapping("/hello")
    public String sayHello(@QueryParam("name") String to) {
        i++;
        return "Hello " + to + "! [" + i + "]";
    }

    @PostMapping("/hello")
    public String sayHi(@RequestBody String to) {
        return to;
    }
}