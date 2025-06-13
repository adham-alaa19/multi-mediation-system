/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

import com.iti.models.MediationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class NormalizationHandler extends MediationHandler {

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        try {
            // 1. Normalize date format
            normalizeTimestamp(context, "start_time");
            normalizeTimestamp(context, "end_time");

            // 2. Convert usage units
            normalizeUsage(context);

            // 3. Standardize field names (example: ensure all lowercase)
            standardizeFieldNames(context);

            return new MediationStepResult(context, true, "Normalization successful");
        } catch (Exception e) {
            return new MediationStepResult(context, false, "Normalization failed: " + e.getMessage());
        }
    }

    private void normalizeTimestamp(MediationContext context, String key) {
        String original = context.getAsString(key);
        if (original != null) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(original, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Instant utcInstant = parsed.toInstant(ZoneOffset.UTC);
                context.setField(key, utcInstant.toString());
            } catch (Exception e) {
              
                System.err.println("Failed to parse timestamp: " + original);
            }
        }
    }

    private void normalizeUsage(MediationContext context) {
        String unit = context.getAsString("unit");
        Double usage = context.getAsDouble("usage");

        if (usage != null && unit != null) {
            switch (unit.toUpperCase()) {
                case "KB":
                    usage = usage / 1024;
                    unit = "MB";
                    break;
                case "SEC":
                    usage = usage / 60;
                    unit = "MIN";
                    break;
                
            }
            context.setField("usage", usage);
            context.setField("unit", unit);
        }
    }

    private void standardizeFieldNames(MediationContext context) {
        // Example: convert some known aliases to standard names
        Object msisdn = context.getField("MSISDN");
        if (msisdn != null) {
            context.setField("msisdn", msisdn.toString());
            context.getFields().remove("MSISDN");
        }
    }
}
