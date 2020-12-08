package com.riftar.linebot.Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NumberUtils {

    public static String formatDate(Long time){
        Instant instant = Instant.ofEpochMilli(time);
        LocalDateTime local = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return local.format(DateTimeFormatter.ofPattern("dd MMM"));
    }
}
