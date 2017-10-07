package com.sk7software.rail.model;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

public class StationStop {
    private String scheduled;
    private String estimated;
    private String name;

    public StationStop() {}

    public StationStop(String name, String schedStr, String estStr) throws InvalidTimeFormatException {
        this.name = name;
        this.scheduled = schedStr;
        this.estimated = estStr;
    }

    public String getScheduled() {
        return scheduled;
    }

    public void setScheduled(String scheduled) {
        this.scheduled = scheduled;
    }

    public String getEstimated() {
        return estimated;
    }

    public void setEstimated(String estimated) {
        this.estimated = estimated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
