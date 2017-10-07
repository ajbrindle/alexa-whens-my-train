package com.sk7software.rail.util;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtil {
    private static final Logger log = LoggerFactory.getLogger(TimeUtil.class);

    public static final String TIME_FORMAT = "HH:mm";

    private static final String ON_TIME = "ON TIME";
    private static final String DELAYED = "DELAYED";

    public static LocalTime convertToTime(String timeStr) throws InvalidTimeFormatException {
        try {
            if (ON_TIME.equalsIgnoreCase(timeStr.toUpperCase())) {
                return null;
            } else if (DELAYED.equalsIgnoreCase(timeStr.toUpperCase())) {
                return null;
            }
            else {
                String[] times = timeStr.split(":");
                int hours = Integer.parseInt(times[0]);
                int minutes = Integer.parseInt(times[1]);

                return new LocalTime(hours, minutes);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidTimeFormatException("Invalid time format for this service");
        }
    }


    public static final boolean isDelayed(String sched, String est) {
        if (ON_TIME.equalsIgnoreCase(est)) {
            return false;
        } else if (DELAYED.equalsIgnoreCase(est)) {
            return true;
        } else if (est.indexOf(":") > 0){
            try {
                LocalTime scheduledTime = convertToTime(sched);
                LocalTime estimatedTime = convertToTime(est);
                return estimatedTime.isAfter(scheduledTime);
            } catch (InvalidTimeFormatException e) {
                log.error("Unrecognised time: " + sched + " or " + est);
                return true;
            }
        }
        return true;
    }

    public static final int calcDelayMinutes(String sched, String est) {
        try {
            LocalTime scheduledTime = convertToTime(sched);
            LocalTime estimatedTime = convertToTime(est);
            int delay = Minutes.minutesBetween(scheduledTime, estimatedTime).getMinutes();
            if (delay < 0) delay = 0;
            return delay;
        } catch (InvalidTimeFormatException e) {
            log.error("Unrecognised time: " + sched + " or " + est);
            return 0;
        }
    }
}
