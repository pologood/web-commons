package com.lianjia.iprd.common;

/**
 * Created by fengxiao on 16/6/30.
 */
public class BaseException extends RuntimeException {

    private int code;
    private String message;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.code = errorCode.getCode();
        this.message = errorCode.getDesc();
    }

    public BaseException(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
