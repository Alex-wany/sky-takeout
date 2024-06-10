package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 自定义切面实现公共字段的自动填充
 */

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     * execution(* com.sky.mapper.*.*(..)) 表示切入com.sky.mapper包下的所有类的所有方法
     * @annotation(com.sky.annotation.AutoFill) 表示切入带有AutoFill注解的方法
     * && 表示并且 两个条件都满足
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill))")
    public void autoFillPointCut(){}

    /**
     * 前置通知,为新增和修改操作的公共字段自动填充
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行自动填充公共字段...");
        //获取方法上的注解,进而获取数据库操作类型
        //Signature接口提供的信息是比较少，例如可以获取方法名，但无法获取方法的参数类型、返回类型等更详细的信息
        //为了能够访问这些更详细的方法签名信息，需要将Signature对象转型为MethodSignature对象。MethodSignature是Signature的子接口
        OperationType operationType = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(AutoFill.class).value();
        //获取方法的参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0 ){
            log.error("方法参数为空,无法进行自动填充...");
            return;
        }
        //参数实体类 一般是第一个参数 使用 object类型接收 更加通用
        Object entity = args[0];
        //获取自动填充数据
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime currentTime = LocalDateTime.now();
        //反射获取当前类是否具备设置公共字段的方法
        if(operationType == OperationType.INSERT){
            try {
                //通过反射获取当前类的部分set方法
                //语法：Method method = 类名.class.getClass().getDeclaredMethod("方法名",参数类型.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射设置公共字段
                setCreateTime.invoke(entity,currentTime);
                setUpdateTime.invoke(entity,currentTime);
                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
            }
            catch(Exception e){
                log.error("反射设置公共字段失败:{}",e.getMessage());
            }

        }else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射设置公共字段
                setUpdateTime.invoke(entity,currentTime);
                setUpdateUser.invoke(entity,currentId);
            }
            catch(Exception e){
                log.error("反射设置公共字段失败:{}",e.getMessage());
            }
        }

    }

}
