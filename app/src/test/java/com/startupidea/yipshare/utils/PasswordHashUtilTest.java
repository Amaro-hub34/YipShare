package com.startupidea.yipshare.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordHashUtilTest {

    @Test
    public void samePasswordMatches() {
        assertTrue(PasswordHashUtil.matches("Secreta1!", PasswordHashUtil.hash("Secreta1!")));
    }

    @Test
    public void otherPasswordDoesNotMatch() {
        assertFalse(PasswordHashUtil.matches("Secreta2!", PasswordHashUtil.hash("Secreta1!")));
    }

    @Test
    public void saltMakesEveryHashDifferent() {
        assertNotEquals(PasswordHashUtil.hash("Secreta1!"), PasswordHashUtil.hash("Secreta1!"));
    }

    @Test
    public void malformedStoredHashDoesNotMatch() {
        assertFalse(PasswordHashUtil.matches("Secreta1!", "garbage"));
    }
}
