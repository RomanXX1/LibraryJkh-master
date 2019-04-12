package com.patternjkh;

import com.patternjkh.utils.DigitUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class DigitUnitTest {

    @Test
    public void testGettingIntegerValueFromDouble() {
        assertEquals("0", DigitUtils.getIntegerValueFromDoubleString(null));
        assertEquals("0", DigitUtils.getIntegerValueFromDoubleString(""));
        assertEquals("0", DigitUtils.getIntegerValueFromDoubleString("0"));
        assertEquals("123", DigitUtils.getIntegerValueFromDoubleString("123.0"));
        assertEquals("12", DigitUtils.getIntegerValueFromDoubleString("12.999999999999999999999999999999999"));
        assertEquals("0", DigitUtils.getIntegerValueFromDoubleString("0.99999"));
        assertEquals("99", DigitUtils.getIntegerValueFromDoubleString("99.5"));
    }

    @Test
    public void testGettingDecimalValueFromDouble() {
        assertEquals("0", DigitUtils.getDecimalValueFromDoubleString(null));
        assertEquals("0", DigitUtils.getDecimalValueFromDoubleString(""));
        assertEquals("0", DigitUtils.getDecimalValueFromDoubleString("0"));
        assertEquals("0", DigitUtils.getDecimalValueFromDoubleString("123.0"));
        assertEquals("999999999999999999999999999999999", DigitUtils.getDecimalValueFromDoubleString("12.999999999999999999999999999999999"));
        assertEquals("99999", DigitUtils.getDecimalValueFromDoubleString("0.99999"));
        assertEquals("0", DigitUtils.getDecimalValueFromDoubleString("11"));
    }
}
