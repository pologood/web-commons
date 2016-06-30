package com.lianjia.iprd.util.common;

import com.lianjia.iprd.common.BaseException;
import com.lianjia.iprd.common.ErrorCode;

/**
 * Created by fengxiao on 16/6/30.
 */
public class ExcelException extends BaseException {

    public ExcelException(ErrorCode errorCode) {
        super(errorCode);
    }
}
