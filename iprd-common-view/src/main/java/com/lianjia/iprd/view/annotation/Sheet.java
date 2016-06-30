package com.lianjia.iprd.view.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标示一个对象为表单对象
 * Created by fengxiao on 16/5/27.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sheet {

    /**
     * 表单索引,按从小到大,从左到右排列
     */
    int index() default 0;

    /**
     * 表单名字
     */
    String name() default StringUtils.EMPTY;

}
