package com.mf.service.service;

import com.mf.spring.annotation.Autowired;
import com.mf.spring.annotation.Component;

@Component
public class UserService {

    @Autowired
    private OrderService orderService;
    public void test(){
        System.out.println("zpp" + orderService);
    }
}
