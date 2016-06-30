package com.lianjia.iprd.common;

/**
 * Created by fengxiao on 16/6/30.
 */
public enum ErrorCode {

    INVOKE_REMOTE_API_ERROR(100002, "调用远程API出错"),
    ANNOTATION_ILLEGAL_USE(100003, "错误使用注解"),
    MESSAGE_CAPACITY_ALREADY_EXIST(100004, "队列参数重复设置"),
    EXCEL_SHEET_NOT_ANNOTATED(100005, "未标注为EXCEL表单"),
    EXCEL_DUMP_NOT_COMPLETE_ERROR(100006, "导出Excel尚未完成"),
    EXCEL_DUMP_ERROR(100007, "导出Excel出错"),
    EXCEL_SHEET_NOT_EXIST_ERROR(100008, "不存在EXCEL表单"),
    FIELD_ILLEGAL_ACCESS_ERROR(100009, "属性不允许访问"),;


    private Integer code;
    private String desc;

    ErrorCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
