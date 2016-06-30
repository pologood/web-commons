package com.lianjia.iprd.util.annotation;


import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注此 field 导出为 Excel 的列
 * Created by fengxiao on 16/5/27.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cell {

    /**
     * 列名字
     */
    String name() default StringUtils.EMPTY;

    /**
     * 列号,实际按列大小,从左向右排列
     */
    int column() default 0;

    /**
     * 当值为 null 时,设置默认值
     */
    String defaultValue() default StringUtils.EMPTY;

}
