/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

import com.iti.models.MediationContext;
import com.iti.portalclient.PortalClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class DeduplicationHandler extends MediationHandler {

    private final PortalClient portalClient = new PortalClient();

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        try {
            String rawKey = context.getAsString("msisdn") + "_" + context.getAsString("timestamp");

            if (rawKey == null || rawKey.trim().isEmpty()) {
                return new MediationStepResult(context, false, " Missing key fields");
            }

            String hash = computeSHA256(rawKey);

            if (portalClient.checkHashExists(hash)) {
                System.out.println("Duplicate record: " + hash);
                return new MediationStepResult(context, false, " Duplicate record");
            }

            portalClient.saveHash(hash);
            return new MediationStepResult(context, true, "Unique record");

        } catch (Exception e) {
            e.printStackTrace();
            return new MediationStepResult(context, false, "Exception in deduplication: " + e.getMessage());
        }
    }

    private String computeSHA256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
