/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine.services;

import com.iti.models.MediationContext;
import com.iti.pipeline.MediationHandler;
import com.iti.pipeline.MediationStepResult;
import com.iti.pipeline.PipelineBuilder;
import java.util.List;
import java.util.Map;

/**
 *
 * @author theda
 */
public class PipelineProcessor {
    
    public void processCdrFile(List<Map<String , Object>> cdrRecords){
                            MediationHandler pipeline = PipelineBuilder.build();
           
           for (Map<String, Object> record : cdrRecords) {
               MediationContext context = new MediationContext(record);
               MediationStepResult result = pipeline.handle(context);          
               if (!result.isSuccess()) {
                   System.out.println("Failed to process CDR record: " + result.getMessage());
               }
           }
    }
    
}
