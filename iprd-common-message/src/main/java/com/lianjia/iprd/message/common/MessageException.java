package com.lianjia.iprd.message.common;

import com.lianjia.iprd.common.BaseException;
import com.lianjia.iprd.common.ErrorCode;

/**
 * Created by fengxiao on 16/6/30.
 */
public class MessageException extends BaseException {

    public MessageException(ErrorCode errorCode) {
        super(errorCode);
    }

}
