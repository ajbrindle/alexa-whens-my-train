package com.sk7software.rail.model;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class TrainService {
    private static final Logger log = LoggerFactory.getLogger(TrainService.class);

    private String operator;
    private String serviceId;
    private String origin;
    private String destination;
    private String platform;
    private StationStop station;
    private List<StationStop> stops;
    private String delayReason;
    private boolean cancelled;
    private String cancelReason;

    public TrainService() {
        stops = new ArrayList<>();
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<StationStop> getStops() {
        return stops;
    }

    public void setStops(List<StationStop> stops) {
        this.stops = stops;
    }

    public void addStop(StationStop stop) {
        stops.add(stop);
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public StationStop getStation() {
        return station;
    }

    public void setStation(StationStop station) {
        this.station = station;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDepartureString() {
        StringBuilder output = new StringBuilder();

        output.append(operator);
        output.append(" service from ");
        output.append(origin);
        output.append(" to ");
        output.append(destination);
        output.append(", due to leave ");
        output.append(station.getName());

        if (platform != null && !"".equals(platform)) {
            output.append(" from platform ");
            output.append(platform);
        }

        output.append(" at ");
        output.append(station.getScheduled());

        if (cancelled) {
            output.append(", is cancelled. ");
            if (cancelReason != null && cancelReason.length() > 0) {
                output.append(cancelReason);
                output.append(". ");
            }

            return output.toString();
        }

        if (TimeUtil.isDelayed(station.getScheduled(), station.getEstimated())) {
            output.append(", is delayed by ");
            int delay = TimeUtil.calcDelayMinutes(station.getScheduled(), station.getEstimated());
            output.append(delay);
            output.append(" minute");
            output.append(delay == 1 ? "" : "s");
            output.append(", and is expected to leave ");
            output.append(station.getName());
            output.append(" at ");
            output.append(station.getEstimated());
            output.append(". ");
            if (delayReason != null && delayReason.length() > 0) {
                output.append(delayReason);
                output.append(". ");
            }

            return output.toString();
        }

        output.append(" is on time. ");
        return output.toString();
    }

    public String getStoppingAtString() {
        StringBuilder output = new StringBuilder("Stopping at: ");
        for (StationStop s : stops) {
            output.append(s.getName());

            if (TimeUtil.isDelayed(s.getScheduled(), s.getEstimated())) {
                output.append(" at the delayed time of ");
                output.append(s.getEstimated());
            } else {
                output.append(" on time at ");
                output.append(s.getScheduled());
            }

            output.append(", ");
        }

        return output.toString();
    }
}
