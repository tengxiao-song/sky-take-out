package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// 自定义注解，用于标记需要自动填充的字段处理
@Target(ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 操作类型:update,insert
    OperationType value();
}
