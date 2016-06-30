package com.lianjia.iprd.view.common;

import java.util.List;

/**
 * Created by fengxiao on 16/5/30.
 */
public class SheetBean<T> {

    private Class<T> clazz;
    private List<T> dataList;

    public SheetBean(Class<T> clazz, List<T> dataList) {
        this.clazz = clazz;
        this.dataList = dataList;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public List<T> getDataList() {
        return dataList;
    }
}
