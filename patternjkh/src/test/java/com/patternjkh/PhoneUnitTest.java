package com.patternjkh;

import com.patternjkh.utils.PhoneUtils;

import org.junit.Test;
import static org.junit.Assert.*;

public class PhoneUnitTest {

    @Test
    public void testPhoneStringStyleChecker() {
        String phoneCorrect_prefix7 = "+79536782222";
        String phoneCorrect_withDashes = "+7-953-678-2222";

        String phoneIncorrect_prefix6 = "69536782222";
        String phoneIncorrect_tooShort = "+7953678222";
        String phoneIncorrect_tooLong = "+795367822223";
        String phoneIncorrect_symbols = "test";

        assertTrue(PhoneUtils.checkValidPhone(phoneCorrect_prefix7));
        assertTrue(PhoneUtils.checkValidPhone(phoneCorrect_withDashes.replaceAll("-", "")));

        assertFalse(PhoneUtils.checkValidPhone(phoneIncorrect_prefix6));
        assertFalse(PhoneUtils.checkValidPhone(phoneIncorrect_tooLong));
        assertFalse(PhoneUtils.checkValidPhone(phoneIncorrect_tooShort));
        assertFalse(PhoneUtils.checkValidPhone(phoneIncorrect_symbols));
    }

    @Test
    public void testPhoneStringFormatter() {
        String test1 = "123";
        String test2 = "";
        String test3 = "89036607914";
        String test4 = "+79036607914";
        String test5 = "89012424";
        String test6 = "+79012424";

        assertEquals("123", PhoneUtils.formatPhoneToRightFormat(test1));
        assertEquals("", PhoneUtils.formatPhoneToRightFormat(test2));
        assertEquals("+7-903-660-7914", PhoneUtils.formatPhoneToRightFormat(test3));
        assertEquals("+7-903-660-7914", PhoneUtils.formatPhoneToRightFormat(test4));
        assertEquals("89012424", PhoneUtils.formatPhoneToRightFormat(test5));
        assertEquals("+79012424", PhoneUtils.formatPhoneToRightFormat(test6));
    }
}
