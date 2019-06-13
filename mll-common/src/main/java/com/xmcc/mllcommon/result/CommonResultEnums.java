package com.xmcc.mllcommon.result;

import lombok.Getter;

@Getter
public enum CommonResultEnums implements ResultEnums {
    SUCCESS(0,"成功"),
    FAIL(1,"失败"),
    SERVER_EROOR(2,"服务器错误");

    private int code;
    private String msg;

    CommonResultEnums(int code,String msg){
        this.code = code;
        this.msg = msg;
    }
}
