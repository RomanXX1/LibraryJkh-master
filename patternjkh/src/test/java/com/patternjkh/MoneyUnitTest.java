package com.patternjkh;

import com.patternjkh.utils.MoneyUtils;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoneyUnitTest {

    @Test
    public void testConversionFromDoubleRublesToCops() {
        double val1 = 112;
        long expected1 = 11200;

        double val2 = 1.1;
        long expected2 = 110;

        double val3 = 5412.99;
        long expected3 = 541299;

        double val4 = 0.0;
        long expected4 = 0;

        double val5 = 999.99;
        long expected5 = 99999;

        assertEquals(expected1, MoneyUtils.convertRublesToCops(val1));
        assertEquals(expected2, MoneyUtils.convertRublesToCops(val2));
        assertEquals(expected3, MoneyUtils.convertRublesToCops(val3));
        assertEquals(expected4, MoneyUtils.convertRublesToCops(val4));
        assertEquals(expected5, MoneyUtils.convertRublesToCops(val5));
    }

    @Test
    public void testConversionFromStringRublesToCops() {
        String val1 = "112";
        long expected1 = 11200;

        String val2 = "1.1";
        long expected2 = 110;

        String val3 = "5412.99";
        long expected3 = 541299;

        String val4 = "";
        long expected4 = 0;

        String val5 = "999,99";
        long expected5 = 99999;

        String val6 = null;
        long expected6 = 0;

        assertEquals(expected1, MoneyUtils.convertRublesToCops(val1));
        assertEquals(expected2, MoneyUtils.convertRublesToCops(val2));
        assertEquals(expected3, MoneyUtils.convertRublesToCops(val3));
        assertEquals(expected4, MoneyUtils.convertRublesToCops(val4));
        assertEquals(expected5, MoneyUtils.convertRublesToCops(val5));
        assertEquals(expected6, MoneyUtils.convertRublesToCops(val6));
    }

    @Test
    public void testFromCopsToRublesMoneyConversionInFormattedString() {
        long sum1 = 1;
        long sum2 = 10;
        long sum3 = 111;
        long sum4 = 523421;
        long sum5 = 999;
        long sum6 = 0;
        assertEquals("0.01", MoneyUtils.convertCopsToStringRublesFormatted(sum1));
        assertEquals("0.10", MoneyUtils.convertCopsToStringRublesFormatted(sum2));
        assertEquals("1.11", MoneyUtils.convertCopsToStringRublesFormatted(sum3));
        assertEquals("5234.21", MoneyUtils.convertCopsToStringRublesFormatted(sum4));
        assertEquals("9.99", MoneyUtils.convertCopsToStringRublesFormatted(sum5));
        assertEquals("0.00", MoneyUtils.convertCopsToStringRublesFormatted(sum6));
    }
}
