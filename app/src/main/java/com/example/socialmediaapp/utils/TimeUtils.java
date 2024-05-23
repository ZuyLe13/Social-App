package com.example.socialmediaapp.utils;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(Date pastDate) {
        long diffInMillis = new Date().getTime() - pastDate.getTime();

        long diffInYears = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 365;
        if (diffInYears >= 1) {
            return diffInYears + (diffInYears > 1 ? " years ago" : " year ago");
        }

        long diffInMonths = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 30;
        if (diffInMonths >= 1) {
            return diffInMonths + (diffInMonths > 1 ? " months ago" : " month ago");
        }

        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        if (diffInDays >= 1) {
            return diffInDays + (diffInDays > 1 ? " days ago" : " day ago");
        }

        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        if (diffInHours >= 1) {
            return diffInHours + (diffInHours > 1 ? " hours ago" : " hour ago");
        }

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        if (diffInMinutes >= 1) {
            return diffInMinutes + (diffInMinutes > 1 ? " minutes ago" : " minute ago");
        }

        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
        return diffInSeconds + (diffInSeconds > 1 ? " seconds ago" : " second ago");
    }
}
