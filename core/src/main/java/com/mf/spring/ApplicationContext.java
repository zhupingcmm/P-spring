package com.mf.spring;

import com.mf.spring.annotation.Component;
import com.mf.spring.annotation.ComponentScan;
import com.mf.spring.annotation.Scope;
import lombok.val;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private final Map<String, BeanDefinition> beanDefinitionHashMap = new ConcurrentHashMap<>();

    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();

    public ApplicationContext(Class<?> clazz){
        if (clazz.isAnnotationPresent(ComponentScan.class)){

            val componentScan = clazz.getAnnotation(ComponentScan.class);
            val scanPath = getScanPath(componentScan.value());
            parseFile(scanPath);
        }

    }

    private void parseFile (File file) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                parseFile(f);
            } else {
                val path = f.getPath().substring(f.getPath().indexOf("com"),f.getPath().indexOf(".class"));
                try {
                    val clazz = ApplicationContext.class.getClassLoader().loadClass(path.replace("\\", "."));
                    if (clazz.isAnnotationPresent(Component.class)) {
                        val component = clazz.getAnnotation(Component.class);
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setType(clazz);
                        if (clazz.isAnnotationPresent(Scope.class)){
                            beanDefinition.setScope(clazz.getAnnotation(Scope.class).value());
                        }
                        String beanName = component.value();
                        if (beanName.equals("")) {
                            beanName = Introspector.decapitalize(clazz.getSimpleName());
                        }
                        if (clazz.isAnnotationPresent(Scope.class)){
                            val scope = clazz.getAnnotation(Scope.class).value();
                            beanDefinition.setScope(scope);
                        }
                        beanDefinitionHashMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private File getScanPath (String scanPath) {
        return new File( ApplicationContext.class.getClassLoader().getResource(scanPath.replace(".","/")).getFile());
    }

    public Object getBean(String beanName){
        val beanDefinition = beanDefinitionHashMap.get(beanName);
        Object bean;
        if (beanDefinition.getScope() == "singleton") {
            bean = singletonMap.get(beanName);
            if (bean == null) {
                bean = createBean(beanName, beanDefinition);
            }
        } else {
            bean = createBean(beanName, beanDefinition);
        }
        return bean;
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        try {
            val instance = beanDefinition.getType().getConstructor().newInstance();
            singletonMap.put(beanName, instance);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


}
