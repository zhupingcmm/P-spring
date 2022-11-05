package com.mf.spring;

import lombok.Data;

@Data
public class BeanDefinition {

    private Class<?> type;

    private String scope = "singleton";

}
