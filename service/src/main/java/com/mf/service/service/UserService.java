package com.mf.service.service;

import com.mf.spring.BeanPostProcessor;
import com.mf.spring.InitializingBean;
import com.mf.spring.annotation.Autowired;
import com.mf.spring.annotation.Component;
import com.mf.spring.annotation.PostConstruct;
import com.mf.spring.aware.NameAware;

@Component
public class UserService implements NameAware, InitializingBean, BeanPostProcessor {

    @Autowired
    private OrderService orderService;


    @PostConstruct
    public void init(){
        System.out.println("PostConstruct:: init");
    }
    public void test(){
        System.out.println("zpp" + orderService);
    }

    @Override
    public void setBeanName(String beanName) {

        System.out.println(beanName);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet");
    }

    @Override
    public Object beanPostProcessorBeforeInitialization(String beanName, Object bean) {

        System.out.println("beanPostProcessorBeforeInitialization:::" + beanName);
        return bean;
    }

    @Override
    public Object beanPostProcessorAfterInitialization(String beanName, Object bean) {
        System.out.println("beanPostProcessorAfterInitialization:::" + beanName);
        return bean;
    }
}
