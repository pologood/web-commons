package com.lianjia.iprd.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以拆分为分页访问的参数,配合 @ConcurrentBatchInvoke 使用, 且必须为最后一个参数
 * Created by fengxiao on 16/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BatchArray {

}
