package com.mf.service;

import com.mf.service.service.UserService;
import com.mf.spring.ApplicationContext;

public class Test {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
