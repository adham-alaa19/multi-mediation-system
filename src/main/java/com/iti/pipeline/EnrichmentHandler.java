package com.iti.pipeline;

import com.iti.models.MediationContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class EnrichmentHandler extends MediationHandler {

    private static final List<String> ALLOWED_SERVICE_TYPES = Arrays.asList("DATA", "SMS", "CALL");

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        // Check required fields
        if (context.getAsString("msisdn") == null || context.getAsString("timestamp") == null) {
            return new MediationStepResult(context, false, "Missing required fields (msisdn or timestamp)");
        }

        // Check usage value
        Double usage = context.getAsDouble("usage");
        if (usage == null || usage <= 0) {
            return new MediationStepResult(context, false, "Invalid usage (zero or negative)");
        }

        // Validate timestamp format and range
        String timestampStr = context.getAsString("timestamp");
        try {
            Instant recordTime = Instant.parse(timestampStr);
            Instant now = Instant.now();

            if (recordTime.isAfter(now)) {
                return new MediationStepResult(context, false, "Timestamp is in the future");
            }

            Instant tooOld = now.minusSeconds(60 * 60 * 24 * 365); // 1 year old
            if (recordTime.isBefore(tooOld)) {
                return new MediationStepResult(context, false, "Timestamp is too old");
            }

        } catch (DateTimeParseException e) {
            return new MediationStepResult(context, false, "Invalid timestamp format");
        }

        // Validate service type
        String serviceType = context.getServiceType();
        if (serviceType == null) {
            serviceType = context.getAsString("serviceType");
        }
        if (serviceType == null || !ALLOWED_SERVICE_TYPES.contains(serviceType.toUpperCase())) {
            return new MediationStepResult(context, false, "Invalid or unsupported service type");
        }

        // Passed all filters
        return new MediationStepResult(context, true, "Record passed filtering");
    }
}
