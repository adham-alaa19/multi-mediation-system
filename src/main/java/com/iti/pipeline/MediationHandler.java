/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

import com.iti.models.MediationContext;

/**
 *
 * @author theda
 */

public abstract class MediationHandler {
    protected MediationHandler nextHandler;

    public void setNextHandler(MediationHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public MediationStepResult handle(MediationContext context) {
        MediationStepResult result = handleContext(context);
        if (!result.isSuccess()) {
            return result;
        }
        return handleNext(context);
    }

    protected MediationStepResult handleNext(MediationContext context) {
        if (nextHandler != null) {
            return nextHandler.handle(context);
        }
        return new MediationStepResult(context,true,"Processing completed successfully.");
    }

    protected abstract MediationStepResult handleContext(MediationContext context);
}
