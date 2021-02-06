package ru.myitschool.normalplayer.utils;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static String convertMs(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

}
