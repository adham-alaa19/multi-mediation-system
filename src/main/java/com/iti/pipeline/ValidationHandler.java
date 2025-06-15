package com.iti.pipeline;

import com.iti.models.MediationContext;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class ValidationHandler extends MediationHandler {

    private static final List<String> ALLOWED_SERVICE_TYPES = Arrays.asList("VOICE", "SMS", "DATA");

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        if (context.getAsString("msisdn") == null || context.getAsString("start_time") == null) {
            return new MediationStepResult(context, false, "Missing required fields (msisdn or start_time)");
        }

        Double usage = context.getAsDouble("usage");
        if (usage == null || usage <= 0) {
            return new MediationStepResult(context, false, "Invalid usage (zero or negative)");
        }

        try {
            Instant.parse(context.getAsString("start_time"));
            Instant.parse(context.getAsString("end_time"));
        } catch (DateTimeParseException e) {
            return new MediationStepResult(context, false, "Invalid timestamp format");
        }

        String serviceType = context.getAsString("serviceType");
        if (serviceType == null || !ALLOWED_SERVICE_TYPES.contains(serviceType.toUpperCase())) {
            return new MediationStepResult(context, false, "Invalid or unsupported service type");
        }

        return new MediationStepResult(context, true, "Record is valid");
    }
}
