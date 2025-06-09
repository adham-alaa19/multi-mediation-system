/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.pipeline;

/**
 *
 * @author theda
 */
public class PipelineBuilder {
    public static MediationHandler build() {
        MediationHandler normalization = new NormalizationHandler();
        MediationHandler validation = new ValidationHandler();
        MediationHandler enrichment = new EnrichmentHandler();
        MediationHandler deduplication = new DeduplicationHandler();
        MediationHandler storage = new StorageHandler();

        normalization.setNextHandler(validation);
        validation.setNextHandler(enrichment);
        enrichment.setNextHandler(deduplication);
        deduplication.setNextHandler(storage);

        return normalization; 
    }
}
