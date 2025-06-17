package com.iti.pipeline;

import com.iti.models.MediationContext;

import java.util.logging.Logger;

public class EnrichmentHandler extends MediationHandler {

    private static final Logger LOGGER = Logger.getLogger(EnrichmentHandler.class.getName());

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        try {
            // مثال: تحديد نوع الشبكة بناءً على origin/destination أو serviceType
            String origin = context.getAsString("origin");
            String destination = context.getAsString("destination");
            String serviceType = context.getAsString("serviceType");

            // 1. network_type: LOCAL أو ROAMING
            if (origin != null && destination != null) {
                if (origin.equalsIgnoreCase(destination)) {
                    context.setField("network_type", "LOCAL");
                } else {
                    context.setField("network_type", "ROAMING");
                }
            } else {
                context.setField("network_type", "UNKNOWN");
            }

            // 2. vendor_id: مثال: بناءً على بادئة msisdn أو origin
            String msisdn = context.getAsString("msisdn");
            if (msisdn != null && msisdn.startsWith("44")) {
                context.setField("vendor_id", "VENDOR_UK");
            } else if (msisdn != null && msisdn.startsWith("1")) {
                context.setField("vendor_id", "VENDOR_US");
            } else {
                context.setField("vendor_id", "VENDOR_GENERIC");
            }

            // 3. service_category: بناءً على serviceType
            if (serviceType != null) {
                switch (serviceType.toUpperCase()) {
                    case "VOICE":
                        context.setField("service_category", "VOICE_CALL");
                        break;
                    case "SMS":
                        context.setField("service_category", "TEXT_MESSAGE");
                        break;
                    case "DATA":
                        context.setField("service_category", "DATA_USAGE");
                        break;
                    default:
                        context.setField("service_category", "UNKNOWN");
                }
            }

            // 4. network_tech: مثال: من بادئة target
            String target = context.getAsString("target");
            String networkTech = resolveNetworkTypeFromTarget(target);
            context.setField("network_tech", networkTech);

            // 5. region: مثال: من بادئة msisdn
            String region = resolveRegionFromPrefix(msisdn);
            context.setField("region", region);

            return new MediationStepResult(context, true, "Enrichment applied");
        } catch (Exception e) {
            LOGGER.warning("Enrichment failed for context: " + e.getMessage());
            return new MediationStepResult(context, false, "Enrichment error: " + e.getMessage());
        }
    }

    // أمثلة على دوال مساعدة يمكنك تعديلها حسب قواعدك:

    private String resolveNetworkTypeFromTarget(String target) {
        if (target == null) return "UNKNOWN";
        // مثال: بادئات لتحديد تكنولوجيا الشبكة
        if (target.startsWith("010")) return "4G";
        if (target.startsWith("011")) return "3G";
        if (target.startsWith("012")) return "2G";
        return "UNKNOWN";
    }

    private String resolveRegionFromPrefix(String msisdn) {
        if (msisdn == null) return "Unknown";
        // مثال: بادئات محلية
        if (msisdn.startsWith("010")) return "Cairo";
        if (msisdn.startsWith("011")) return "Alexandria";
        if (msisdn.startsWith("012")) return "Giza";
        return "Unknown";
    }
}
