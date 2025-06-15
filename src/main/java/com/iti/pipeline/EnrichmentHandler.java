package com.iti.pipeline;

import com.iti.models.MediationContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class EnrichmentHandler extends MediationHandler {

    private static final List<String> ALLOWED_SERVICE_TYPES = Arrays.asList("DATA", "SMS", "VOICE");

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        // Check required fields
        if (context.getAsString("msisdn") == null || context.getAsString("start_time") == null) {
            return new MediationStepResult(context, false, "Missing msisdn or start_time");
        }

        // Validate usage
        Double usage = context.getAsDouble("usage");
        if (usage == null || usage <= 0) {
            return new MediationStepResult(context, false, "Invalid or missing usage");
        }

        // Validate timestamp
        try {
            Instant.parse(context.getAsString("start_time"));
            Instant.parse(context.getAsString("end_time"));
        } catch (DateTimeParseException e) {
            return new MediationStepResult(context, false, "Invalid date format in timestamps");
        }

        // Validate service type
        String serviceType = context.getAsString("serviceType");
        if (serviceType == null || !ALLOWED_SERVICE_TYPES.contains(serviceType.toUpperCase())) {
            return new MediationStepResult(context, false, "Unsupported service type");
        }

        return new MediationStepResult(context, true, "Enrichment passed");
    }
}
