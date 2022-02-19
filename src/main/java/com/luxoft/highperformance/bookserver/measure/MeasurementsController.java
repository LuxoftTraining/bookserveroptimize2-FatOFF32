package com.luxoft.highperformance.bookserver.measure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.*;

@RestController
public class MeasurementsController {
    private final MeasureService measureService;

    public MeasurementsController(MeasureService measureService) {
        this.measureService = measureService;
    }

    @GetMapping("measurements")
    public Map<String, Measurement> getMeasurementsBaseline() {
        Measurement baseline = measureService.getBaseline();
        Map<String, Measurement> measurements = measureService.measurements;
        if (baseline !=null && baseline.getLatency() != 0) {
            for (Measurement measurement : measurements.values()) {
                measurement.setPercent(calculatePercent(baseline, measurement));
                measurement.setBoost(calculateBoost(baseline, measurement));
            }
        }
        return measurements;
    }

    private double calculatePercent(Measurement baseline, Measurement measurement) {
        double d = Math.round(10000d * measurement.getLatency() /
                baseline.getLatency());
        return d/100;
    }

    private String calculateBoost(Measurement baseline, Measurement measurement) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        double b = 1d * baseline.getLatency() / measurement.getLatency();
        return nf.format(b);
    }

    @GetMapping("measurements/clear")
    public String getMeasurements() {
        measureService.measurements.clear();
        return "succeed";
    }

    @GetMapping("measurements/{name}")
    public Measurement getMeasurements(@PathVariable String name) {
        return measureService.measurements.get(name);
    }
}
