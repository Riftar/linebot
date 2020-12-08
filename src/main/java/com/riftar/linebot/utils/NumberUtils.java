package com.riftar.linebot.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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

    public static String formatNumber(Integer number){
        String clean = NumberFormat.getNumberInstance(Locale.US).format(number);
        String result = clean.replace(",", ".");
        return result;
    }
}
