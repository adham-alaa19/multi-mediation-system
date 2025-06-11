package com.iti.pipeline;

import com.iti.models.MediationContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class ValidationHandler extends MediationHandler {

    private static final List<String> ALLOWED_SERVICE_TYPES = Arrays.asList("DATA", "SMS", "CALL");

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        // 1. Check required fields
        if (context.getAsString("msisdn") == null || context.getAsString("timestamp") == null) {
            return new MediationStepResult(context, false, "Missing required fields (msisdn or timestamp)");
        }

        // 2. Check usage validity
        Double usage = context.getAsDouble("usage");
        if (usage == null || usage <= 0) {
            return new MediationStepResult(context, false, "Invalid usage (zero or negative)");
        }

        // 3. Check timestamp format and range
        String timestampStr = context.getAsString("timestamp");
        try {
            Instant recordTime = Instant.parse(timestampStr);
            Instant now = Instant.now();

            if (recordTime.isAfter(now)) {
                return new MediationStepResult(context, false, "Timestamp is in the future");
            }

            Instant tooOld = now.minusSeconds(60 * 60 * 24 * 365); // 1 year ago
            if (recordTime.isBefore(tooOld)) {
                return new MediationStepResult(context, false, "Timestamp is too old");
            }

        } catch (DateTimeParseException e) {
            return new MediationStepResult(context, false, "Invalid timestamp format");
        }

        // 4. Check service type
        String serviceType = context.getServiceType();
        if (serviceType == null) {
            serviceType = context.getAsString("serviceType");
        }

        if (serviceType == null || !ALLOWED_SERVICE_TYPES.contains(serviceType.toUpperCase())) {
            return new MediationStepResult(context, false, "Unsupported or missing service type");
        }

        // 5. Passed all checks
        return new MediationStepResult(context, true, "Valid record");
    }
}
