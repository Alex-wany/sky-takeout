package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 自动填充注解 用于自动填充数据库操作类型
//target: 用于描述注解的使用范围 METHOD:用于描述方法 FIELD:用于描述字段 TYPE:用于描述类
// INTERFACE:用于描述接口 PARAMETER:用于描述参数 CONSTRUCTOR:用于描述构造方法 LOCAL_VARIABLE:用于描述局部变量
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)


public @interface AutoFill {
    // 数据库操作类型,UPDATE,INSERT
    OperationType value();
}
