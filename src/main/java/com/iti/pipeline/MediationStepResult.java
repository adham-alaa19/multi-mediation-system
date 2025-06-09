/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

/**
 *
 * @author theda
 */

import com.iti.models.MediationContext;

public class MediationStepResult {
    private final MediationContext context;
    private final boolean success;
    private final String message;

    // Constructor with all fields
    public MediationStepResult(MediationContext context, boolean success, String message) {
        this.context = context;
        this.success = success;
        this.message = message;
    }

    // Convenience constructor without message
    public MediationStepResult(MediationContext context, boolean success) {
        this(context, success, null);
    }

    public MediationContext getContext() {
        return context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
