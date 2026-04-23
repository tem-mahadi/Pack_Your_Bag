package com.temmahadi.packyourbag.BackEnd;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegisterActivityPasswordValidationTest {

    @Test
    public void password_isValid_whenItHasLetterDigitAndSymbol() {
        assertTrue(RegisterActivity.isValid("Travel@123"));
    }

    @Test
    public void password_isInvalid_whenShortOrMissingSymbol() {
        assertFalse(RegisterActivity.isValid("Travel12"));
    }
}
