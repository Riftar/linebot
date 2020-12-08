package com.riftar.linebot.Utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class NumberUtils {

    public static String formatDate(Long time){
        Instant instant = Instant.ofEpochMilli(time);
        LocalDateTime local = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return local.format(DateTimeFormatter.ofPattern("dd MMM"));
    }

    public static String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }
}
