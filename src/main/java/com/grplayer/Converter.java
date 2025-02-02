package com.grplayer;

import java.text.DecimalFormat;

public class Converter {
     public static String kbToMb(long length) {
        Long l = length;
        double d = l.doubleValue();
        DecimalFormat df = new DecimalFormat("#.00");
        String form = df.format((d/1024)/1024);
        return form + "Mb";
    }
    public static String secToMin(long sec) {
        String time;
        if((sec%60) < 10) {
            time = sec/60 + ":0" + sec%60;
        }
        else {
            time = sec/60 + ":" + sec%60;
        }
        return time;
    }
}
