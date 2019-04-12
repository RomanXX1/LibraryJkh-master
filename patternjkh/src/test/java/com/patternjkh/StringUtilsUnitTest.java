package com.patternjkh;

import android.util.Pair;

import com.patternjkh.utils.StringUtils;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsUnitTest {

    @Test
    public void testFirstUpperCaseChecker() {
        String test1 = "word";
        String test2 = "words with spaces";
        String test3 = "w";
        String test4 = "123";
        String test5 = "";
        String test6 = null;

        assertEquals("Word", StringUtils.firstUpperCase(test1));
        assertEquals("Words with spaces", StringUtils.firstUpperCase(test2));
        assertEquals("W", StringUtils.firstUpperCase(test3));
        assertEquals("123", StringUtils.firstUpperCase(test4));
        assertEquals("", StringUtils.firstUpperCase(test5));
        assertEquals("", StringUtils.firstUpperCase(test6));
    }

    @Test
    public void testStringToIntConverterChecker() {
        String test1 = "1";
        String test2 = "1245235413";
        String test3 = "-1";
        String test4 = "akja";
        String test5 = null;
        String test6 = "---";
        String test7 = "000";

        assertEquals(1, StringUtils.convertStringToInteger(test1));
        assertEquals(1245235413, StringUtils.convertStringToInteger(test2));
        assertEquals(-1, StringUtils.convertStringToInteger(test3));
        assertEquals(0, StringUtils.convertStringToInteger(test4));
        assertEquals(0, StringUtils.convertStringToInteger(test5));
        assertEquals(0, StringUtils.convertStringToInteger(test6));
        assertEquals(0, StringUtils.convertStringToInteger(test7));
    }

    @Test
    public void testStringToLongConverterChecker() {
        String test1 = "1";
        String test2 = "124523541342143";
        String test3 = "-1";
        String test4 = "akja";
        String test5 = null;

        assertEquals(1, StringUtils.convertStringToLong(test1));
        assertEquals(124523541342143L, StringUtils.convertStringToLong(test2));
        assertEquals(-1, StringUtils.convertStringToLong(test3));
        assertEquals(0, StringUtils.convertStringToLong(test4));
        assertEquals(0, StringUtils.convertStringToLong(test5));
    }

    @Test
    public void testStringToDoubleConverterChecker() {
        String test1 = "1";
        String test2 = "12342134";
        String test3 = "-1";
        String test4 = "akja";
        String test5 = null;
        String test6 = "1.99";
        String test7 = "22.2222";
        String test8 = "1,99";
        String test9 = "1,9000000000000";
        String test10 = "00001,100";
        String test11 = "00012,001";
        String test12 = "00000,000";
        String test13 = "00010.000";

        assertEquals(1.0, StringUtils.convertStringToDouble(test1), 0.0);
        assertEquals(12342134.0, StringUtils.convertStringToDouble(test2), 0.0);
        assertEquals(-1.0, StringUtils.convertStringToDouble(test3), 0.0);
        assertEquals(0.0, StringUtils.convertStringToDouble(test4), 0.0);
        assertEquals(0.0, StringUtils.convertStringToDouble(test5), 0.0);
        assertEquals(1.99, StringUtils.convertStringToDouble(test6), 0.0);
        assertEquals(22.2222, StringUtils.convertStringToDouble(test7), 0.0);
        assertEquals(1.99, StringUtils.convertStringToDouble(test8), 0.0);
        assertEquals(1.9, StringUtils.convertStringToDouble(test9), 0.0);
        assertEquals(1.1, StringUtils.convertStringToDouble(test10), 0.0);
        assertEquals(12.001, StringUtils.convertStringToDouble(test11), 0.0);
        assertEquals(0.0, StringUtils.convertStringToDouble(test12), 0.0);
        assertEquals(10.0, StringUtils.convertStringToDouble(test13), 0.0);
    }

    @Test
    public void testStringToFloatConverterChecker() {
        String test1 = "1";
        String test2 = "12342134";
        String test3 = "-1";
        String test4 = "akja";
        String test5 = null;
        String test6 = "1.99";
        String test7 = "22.2222";
        String test8 = "1,99";
        String test9 = "1,9000000000000";

        assertEquals(1.0, StringUtils.convertStringToFloat(test1), 0.0);
        assertEquals(12342134.0, StringUtils.convertStringToFloat(test2), 0.0);
        assertEquals(-1.0, StringUtils.convertStringToFloat(test3), 0.0);
        assertEquals(0.0, StringUtils.convertStringToFloat(test4), 0.0);
        assertEquals(0.0, StringUtils.convertStringToFloat(test5), 0.0);
        assertEquals(1.99, StringUtils.convertStringToDouble(test6), 0.0);
        assertEquals(22.2222, StringUtils.convertStringToDouble(test7), 0.0);
        assertEquals(1.99, StringUtils.convertStringToDouble(test8), 0.0);
        assertEquals(1.9, StringUtils.convertStringToDouble(test9), 0.0);
    }

    @Test
    public void testStringToBooleanConverterChecker() {
        String test1 = "true";
        String test2 = "false";
        String test3 = "True";
        String test4 = "False";
        String test5 = "123";
        String test6 = "truu";
        String test7 = "";
        String test8 = null;

        assertTrue(StringUtils.convertStringToBoolean(test1));
        assertFalse(StringUtils.convertStringToBoolean(test2));
        assertTrue(StringUtils.convertStringToBoolean(test3));
        assertFalse(StringUtils.convertStringToBoolean(test4));
        assertFalse(StringUtils.convertStringToBoolean(test5));
        assertFalse(StringUtils.convertStringToBoolean(test6));
        assertFalse(StringUtils.convertStringToBoolean(test7));
        assertFalse(StringUtils.convertStringToBoolean(test8));
    }

    @Test
    public void testStringIntegerToFiveDigits() {
        assertEquals("00001", StringUtils.formatIntegersToFiveNumbers("1"));
        assertEquals("00012", StringUtils.formatIntegersToFiveNumbers("12"));
        assertEquals("00123", StringUtils.formatIntegersToFiveNumbers("123"));
        assertEquals("01234", StringUtils.formatIntegersToFiveNumbers("1234"));
        assertEquals("12345", StringUtils.formatIntegersToFiveNumbers("12345"));
        assertEquals("12345", StringUtils.formatIntegersToFiveNumbers("123456"));
    }

    @Test
    public void testStringDecimalsToThreeDigits() {
        assertEquals("100", StringUtils.formatDecimalsToThreeNumbers("1"));
        assertEquals("120", StringUtils.formatDecimalsToThreeNumbers("12"));
        assertEquals("123", StringUtils.formatDecimalsToThreeNumbers("123"));
        assertEquals("123", StringUtils.formatDecimalsToThreeNumbers("1234"));
        assertEquals("001", StringUtils.formatDecimalsToThreeNumbers("001"));
    }

    @Test
    public void testStringValueToCards() {
    }
}
