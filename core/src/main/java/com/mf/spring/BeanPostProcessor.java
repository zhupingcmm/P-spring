package com.mf.spring;

public interface BeanPostProcessor {

    Object beanPostProcessorBeforeInitialization(String beanName, Object bean);

    Object beanPostProcessorAfterInitialization(String beanName, Object bean);
}
