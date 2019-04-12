package com.patternjkh.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtils {

    public static String getDate() {
        Locale locale = new Locale("ru", "RU");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", locale);
        return dateFormat.format(new GregorianCalendar().getTimeInMillis());
    }

    public static int getCurrentDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static int getCurrentMonth() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.MONTH) + 1;
    }

    public static int getCurrentYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR);
    }

    public static int getDayFromDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonthFromDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH);
    }

    public static int getYearFromDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    public static String parseDateToStringWithoutHours(String date) {
        if (date == null || date.equals("")) {
            return getDate();
        } else {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyy");
            String result = "";
            try {
                result = String.valueOf(myFormat.format(df.parse(date)));
            } catch (ParseException e) {
                result = getDate();
            }
            return result;
        }
    }

    public static String parseDateToStringWithHours(String date) {
        if (date == null || date.equals("")) {
            return getDate();
        } else {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String result = "";
            try {
                result = String.valueOf(myFormat.format(df.parse(date)));
            } catch (ParseException e) {
                result = getDate();
            }
            return result;
        }
    }

    public static String parseDateToStringWithHoursAndYearLiteral(String date) {
        if (date == null || date.equals("")) {
            return getDate();
        } else {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyyг HH:mm");
            String result = "";
            try {
                result = String.valueOf(myFormat.format(df.parse(date)));
            } catch (ParseException e) {
                result = getDate();
            }
            return result;
        }
    }

    public static long getNumberOfDaysFromTodayToDate(Date dateEnd) {
        Date today = DateUtils.convertStringToDate(getDate(), "dd.MM.yyyy HH:mm:ss");
        return getNumberOfDaysBetweenDates(today, dateEnd);
    }

    public static long getNumberOfDaysBetweenDates(Date dateStart, Date dateEnd) {
        return Math.abs((int)( (dateEnd.getTime() - dateStart.getTime()) / (1000 * 60 * 60 * 24)));
    }

    public static String getMonthNameByNumber(int num_month) {
        String rez = "";
        switch (num_month) {
            case 1:
                rez = "Январь";
                break;
            case 2:
                rez = "Февраль";
                break;
            case 3:
                rez = "Март";
                break;
            case 4:
                rez = "Апрель";
                break;
            case 5:
                rez = "Май";
                break;
            case 6:
                rez = "Июнь";
                break;
            case 7:
                rez = "Июль";
                break;
            case 8:
                rez = "Август";
                break;
            case 9:
                rez = "Сентябрь";
                break;
            case 10:
                rez = "Октябрь";
                break;
            case 11:
                rez = "Ноябрь";
                break;
            case 12:
                rez = "Декабрь";
                break;
        }
        return rez;
    }

    public static String getMonthNameInRightCaseByNumber(int num_month) {
        String rez = "";
        switch (num_month) {
            case 1:
                rez = "Января";
                break;
            case 2:
                rez = "Февраля";
                break;
            case 3:
                rez = "Марта";
                break;
            case 4:
                rez = "Апреля";
                break;
            case 5:
                rez = "Мая";
                break;
            case 6:
                rez = "Июня";
                break;
            case 7:
                rez = "Июля";
                break;
            case 8:
                rez = "Августа";
                break;
            case 9:
                rez = "Сентября";
                break;
            case 10:
                rez = "Октября";
                break;
            case 11:
                rez = "Ноября";
                break;
            case 12:
                rez = "Декабря";
                break;
        }
        return rez;
    }

    public static Date convertStringToDate(String date, String mask) {
        DateFormat format = new SimpleDateFormat(mask, Locale.ENGLISH);
        Date result = null;
        try {
            result = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}
