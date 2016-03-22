package com.github.okamayana.liftmate.util;

import android.widget.Chronometer;

import java.util.concurrent.TimeUnit;

public class DateTimeUtil {

    public static String getSetTimeViewString(boolean reset, long targetSetTime,
                                              long timeInSet, String format) {
        long millis = reset ? targetSetTime : timeInSet;
        return getTimestampFromMillis(millis, format);
    }

    public static String getTimestampFromMillis(long millis, String format) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format(format, minutes, seconds);
    }

    public static long getMillisFromChronometer(Chronometer chronometer) {
        String chronometerString = chronometer.getText().toString();
        String[] chronometerStringArray = chronometerString.split(":");

        long timeMillis = 0;
        if (chronometerStringArray.length == 2) {
            int minutes = Integer.parseInt(chronometerStringArray[0]);
            int seconds = Integer.parseInt(chronometerStringArray[1]);

            timeMillis = minutes * 60 * 1000 + seconds * 1000;
        } else if (chronometerStringArray.length == 3) {
            int hours = Integer.parseInt(chronometerStringArray[0]);
            int minutes = Integer.parseInt(chronometerStringArray[1]);
            int seconds = Integer.parseInt(chronometerStringArray[2]);

            timeMillis = hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000;
        }

        return timeMillis;
    }
}
