package com.mf.spring;

import com.mf.spring.annotation.*;
import com.mf.spring.aware.NameAware;
import lombok.val;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApplicationContext {

    private final Map<String, BeanDefinition> beanDefinitionHashMap = new ConcurrentHashMap<>();

    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<BeanPostProcessor> beanPostProcessorList = new CopyOnWriteArrayList<>();

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
                        getBeanDefinition(clazz);
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                            beanPostProcessorList.add((BeanPostProcessor) clazz.newInstance());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void getBeanDefinition(Class<?> clazz) {
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

        val clazz = beanDefinition.getType();
        try {

            // 实例化
            Object instance = clazz.getConstructor().newInstance();
            singletonMap.put(beanName, instance);
            // 依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }
            // aware
            if(instance instanceof NameAware){
                ((NameAware)instance).setBeanName(beanName);
            }

            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if(declaredMethod.isAnnotationPresent(PostConstruct.class)){
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(instance);
                }

            }
            // beanPostProcessor before
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
              instance = beanPostProcessor.beanPostProcessorBeforeInitialization(beanName, instance);
            }
            // 初始化 InitializingBean
            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // beanPostProcessor after
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
              instance = beanPostProcessor.beanPostProcessorAfterInitialization(beanName, instance);
            }


            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException  e) {
            throw new RuntimeException(e);
        }
    }


}
