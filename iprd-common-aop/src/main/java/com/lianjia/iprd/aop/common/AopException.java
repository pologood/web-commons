package com.lianjia.iprd.aop.common;

import com.lianjia.iprd.common.BaseException;
import com.lianjia.iprd.common.ErrorCode;

/**
 * Created by fengxiao on 16/6/30.
 */
public class AopException extends BaseException {

    public AopException(ErrorCode errorCode) {
        super(errorCode);
    }

}
