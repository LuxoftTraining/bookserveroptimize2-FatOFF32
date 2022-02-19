package com.luxoft.highperformance.bookserver.measure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Measurement {
    private String name;
    private long time;
    private long callsCount = 0L;
    private long callsCountMeasured;
    private long latency;
    private double percent;
    private String boost;

    @JsonIgnore
    private Set<Long> times = Collections.synchronizedSet(new HashSet<>());

    public Long getPercentile50() {
        return getPercentile(50);
    }

    public Long getPercentile90() {
        return getPercentile(90);
    }

    public Long getPercentile99() {
        return getPercentile(99);
    }

    @JsonIgnore
    public Long getPercentile(int percentile) {
        if (times.size() == 0) return null;
        int indexAtPercentile = (int) Math.floor(times.size() * percentile / 100d);
        Long[] timesArr = times.toArray(new Long[0]);
        if (indexAtPercentile>=timesArr.length) return 0L; // TODO: investigate why it happens
        Long percentileValue = timesArr[indexAtPercentile];
        return percentileValue;
    }

}
