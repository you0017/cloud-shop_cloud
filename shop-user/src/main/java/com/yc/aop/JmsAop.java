package com.yc.aop;

import com.yc.bean.UserInformation;
import com.yc.utils.JmsMessageProducer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class JmsAop {

    @Autowired
    private JmsMessageProducer jmsMessageProducer;
    @Pointcut("execution(* com.yc.service.impl.UserInformationServiceImpl.register(..))")
    public void autoFillPointCut(){}

    @AfterReturning(pointcut = "autoFillPointCut()", returning = "result")
    public void afterReturning(Object result) {
        //新增成功,发送邮箱
        jmsMessageProducer.registerMessage((UserInformation) result);
        //System.out.println("afterReturning");
    }
}
