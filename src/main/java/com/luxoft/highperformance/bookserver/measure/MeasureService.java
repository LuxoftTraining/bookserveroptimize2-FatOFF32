package com.luxoft.highperformance.bookserver.measure;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MeasureService {

    Map<String, Measurement> measurements = Collections.synchronizedMap(new TreeMap<>());
    String baselineName;

    public void addMeasurement(String name, Long time, long warmup, boolean baseline) {
        if (baseline) baselineName = name;
        addMeasurement(name, time, warmup);
    }

    public Measurement getBaseline() {
        if (baselineName != null) {
            return measurements.get(baselineName);
        }
        return null;
    }

    public void addMeasurement(String name, Long time, long warmup) {
        Measurement measurement = measurements.get(name);
        if (measurement == null) {
            measurement = new Measurement();
            measurement.setName(name);
            measurement.setCallsCount(1L);
            measurements.put(name, measurement);
            if (warmup == 0) {
                measurement.setTime(time);
                measurement.setCallsCountMeasured(1L);
                measurement.setLatency(time);
                measurement.getTimes().add(time);
            }
        } else {
            measurement.setCallsCount(measurement.getCallsCount()+1);
            if (measurement.getCallsCount() > warmup) {
                measurement.setCallsCountMeasured(
                        measurement.getCallsCountMeasured()+1);
                measurement.setTime(measurement.getTime() + time);
                measurement.setLatency(measurement.getTime() /
                        measurement.getCallsCountMeasured());
                measurement.getTimes().add(time);
            }
        }
    }

}
