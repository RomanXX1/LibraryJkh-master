package com.patternjkh.utils;

import com.patternjkh.enums.TimeOfDay;

import java.util.Calendar;

public class TimeUtils {

    public static TimeOfDay identifyTimeOfDay(){
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);

        TimeOfDay timeOfDay = TimeOfDay.MORNING;

        if (hourOfDay >= 0 && hourOfDay < 12){
            timeOfDay = TimeOfDay.MORNING;
        } else if(hourOfDay >= 12 && hourOfDay < 16){
            timeOfDay = TimeOfDay.AFTERNOON;
        } else if(hourOfDay >= 16 && hourOfDay < 21){
            timeOfDay = TimeOfDay.EVENING;
        } else if(hourOfDay >= 21 && hourOfDay < 24){
            timeOfDay = TimeOfDay.NIGHT;
        }
        return timeOfDay;
    }
}