package com.foune.tracklinedemo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * descreption:
 * company: foune.com
 * Created by xuyanliang on 2017/2/7 0007.
 */

public class DateUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String toDate(Date date){
        return sdf.format(date);
    }
}
