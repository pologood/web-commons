package com.lianjia.iprd.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将此方法拆分为并发访问,结果集归并到一起,配合 @BatchArray 使用,主要用于访问 Dubbo 接口,而请求参数非常多的情况
 * 要求方法返回值必须是 Collection 类型
 * Created by fengxiao on 16/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrentBatchInvoke {

    /**
     * 设置并发访问的分页大小
     */
    int pageSize() default 30;

    /**
     * 并发线程调用时间间隔,单位毫秒
     */
    long interval() default 0L;

}
