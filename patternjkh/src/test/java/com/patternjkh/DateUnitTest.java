package com.patternjkh;

import com.patternjkh.utils.DateUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class DateUnitTest {

    @Test
    public void testParsingDate() {
        assertEquals("01.04.2019 00:00", DateUtils.parseDateToStringWithHours("01.04.2019 00:00:00"));
        assertEquals("01.04.2019", DateUtils.parseDateToStringWithoutHours("01.04.2019 00:00:00"));
    }

    @Test
    public void testCalculatingDaysBetweenDates() {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        cal1.set(2019, 4, 3);
        cal2.set(2019, 4, 13);

        assertEquals(10, DateUtils.getNumberOfDaysBetweenDates(cal1.getTime(), cal2.getTime()));
    }
}
