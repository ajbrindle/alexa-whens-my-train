package com.sk7software.rail.util;

import org.joda.time.LocalTime;
import org.junit.Test;

import java.sql.Time;

import static org.junit.Assert.*;

public class TestTimeUtil {

    @Test
    public void testFormatTime() throws Exception {
        String timeStr = "19:53";
        LocalTime time = TimeUtil.convertToTime(timeStr);
        assertEquals(new LocalTime(19, 53), time);
    }

    @Test
    public void testFormatOnTime() throws Exception {
        String timeStr = "On time";
        LocalTime time = TimeUtil.convertToTime(timeStr);
        assertNull(time);
    }

    @Test
    public void testFormatDelayed() throws Exception {
        String timeStr = "Delayed";
        LocalTime time = TimeUtil.convertToTime(timeStr);
        assertNull(time);
    }

    @Test
    public void test2MinuteDelay() throws Exception {
        int delay = TimeUtil.calcDelayMinutes("19:59", "20:01");
        assertEquals(2, delay);
    }

    @Test
    public void testNegativeDelay() throws Exception {
        int delay = TimeUtil.calcDelayMinutes("19:59", "19:58");
        assertEquals(0, delay);
    }

    @Test
    public void testInvalidTime() throws Exception {
        int delay = TimeUtil.calcDelayMinutes("invalid", "on time");
        assertEquals(0, delay);
    }

    @Test
    public void testOnTime() throws Exception {
        boolean delayed = TimeUtil.isDelayed("10:30", "On Time");
        assertFalse(delayed);
    }

    @Test
    public void testDelayed() throws Exception {
        boolean delayed = TimeUtil.isDelayed("09:15", "Delayed");
        assertTrue(delayed);
    }

    @Test
    public void testTimeDelay() throws Exception {
        boolean delayed = TimeUtil.isDelayed("06:47", "07:01");
        assertTrue(delayed);
    }

    @Test
    public void testTimeEarly() throws Exception {
        boolean delayed = TimeUtil.isDelayed("20:09", "20:06");
        assertFalse(delayed);
    }

    @Test
    public void testEstTimeInvalid() throws Exception {
        boolean delayed = TimeUtil.isDelayed("19:49", "whatever");
        assertTrue(delayed);
    }

    @Test
    public void testSchedTimeInvalid() throws Exception {
        boolean delayed = TimeUtil.isDelayed("whatever", "20:20");
        assertTrue(delayed);
    }
}
