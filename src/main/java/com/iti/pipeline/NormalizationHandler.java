/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

import com.iti.models.MediationContext;

import java.time.Instant;

public class NormalizationHandler extends MediationHandler {

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        try {
            normalizeTimestamp(context);
            normalizeUnits(context);
            return new MediationStepResult(context, true, "Normalization successful");
        } catch (Exception e) {
            return new MediationStepResult(context, false, "Normalization failed: " + e.getMessage());
        }
    }

    private void normalizeTimestamp(MediationContext context) {
        // Ensure both timestamps are in ISO 8601 (assume input already is)
        String start = context.getAsString("start_time");
        String end = context.getAsString("end_time");
        if (start != null) context.setField("start_time", Instant.parse(start).toString());
        if (end != null) context.setField("end_time", Instant.parse(end).toString());
    }

    private void normalizeUnits(MediationContext context) {
        String unit = context.getAsString("unit");
        Double usage = context.getAsDouble("usage");

        if (usage != null && unit != null) {
            switch (unit.toLowerCase()) {
                case "seconds":
                    usage = usage / 60;
                    unit = "minutes";
                    break;
                case "kb":
                    usage = usage / 1024;
                    unit = "mb";
                    break;
                case "message":
                    unit = "msg";
                    break;
            }
            context.setField("usage", usage);
            context.setField("unit", unit);
        }
    }
}
