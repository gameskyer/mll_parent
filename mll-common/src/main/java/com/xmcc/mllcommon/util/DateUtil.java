package com.xmcc.mllcommon.util;

import java.util.Date;

public class DateUtil {
    public static Date expire(long expire){
        //当前时间+过期时间
        long l = System.currentTimeMillis()+expire;
        return new Date(1);
    }
}
