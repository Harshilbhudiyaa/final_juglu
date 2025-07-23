package com.infowave.demo.adapters;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static String getTimeAgo(String isoDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            long time = sdf.parse(isoDate).getTime();
            long now = System.currentTimeMillis();
            CharSequence cs = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return cs.toString();
        } catch (ParseException e) {
            return "";
        }
    }

    public static String getSectionTitle(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;

        if (diff < DateUtils.DAY_IN_MILLIS) return "Today";
        if (diff < 2 * DateUtils.DAY_IN_MILLIS) return "Yesterday";
        if (diff < 7 * DateUtils.DAY_IN_MILLIS) return "Last 7 days";
        return "Older";
    }

    public static long parseIsoToMillis(String iso) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(iso).getTime();
        } catch (Exception e) {
            return 0;
        }
    }
}
