/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine;

import com.iti.engine.services.*;
import java.io.FileInputStream;
import java.util.List;

/**
 *
 * @author theda
 */
import com.iti.models.MediationContext;
import com.iti.pipeline.MediationStepResult;
import com.iti.pipeline.MediationHandler;
import com.iti.pipeline.PipelineBuilder;
import com.iti.fileparsers.ParserFactory;
import com.iti.fileparsers.ParserStrategy;
import com.iti.engine.services.CollectionService;
import com.iti.engine.services.PipelineProcessor;
import com.iti.engine.services.DistributionService;
import com.iti.engine.utils.FileTypeDetector;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MediationEngine {
    
    private static final Logger LOGGER = Logger.getLogger(MediationEngine.class.getName());
    
    private CollectionService collectionService;
    private PipelineProcessor pipelineProcessor;
    private DistributionService distributionService;
    
    public MediationEngine() {
        this.collectionService = new CollectionService();
        this.pipelineProcessor = new PipelineProcessor();
        this.distributionService = new DistributionService();
    }
    public void run() {
        try {
            List<FileInputStream> files = collectionService.collectFromUpstreamServers();
            for (FileInputStream file : files) {
                processFile(file);
            }          
            distributionService.distributeToDownstreamServers();            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Mediation cycle failed", e);
            throw new RuntimeException("Mediation engine failure", e);
        }
    }
    
   private void processFile(FileInputStream file) {
   try {
       String fileType = FileTypeDetector.detectFileType(file);
       List<Map<String, Object>> cdrRecords = parseFile(fileType, file);    
       if (!cdrRecords.isEmpty()) 
              pipelineProcessor.processCdrFile(cdrRecords);       
   } catch (Exception e) {
       System.out.println("Failed to detect file type - Error: " + e.getMessage());
   }
}

private List<Map<String, Object>> parseFile(String fileType, FileInputStream file) {
   try {
       ParserStrategy parser = ParserFactory.createParser(fileType);
       try {
           List<Map<String, Object>> cdrRecords = parser.parse(file);
           System.out.println("Parsed " + cdrRecords.size() + " CDR records from file");
           return cdrRecords;
           
       } catch (UnsupportedOperationException e) {
           System.out.println("Failed to parse file - Error: Parser "+ fileType +" not implemented yet");
           return new ArrayList<>();    
       } catch (Exception e) {
           System.out.println("Failed to parse file - Error: " + e.getMessage());
           return new ArrayList<>();
       }
       
   } catch (IllegalArgumentException e) {
       System.out.println("Failed to parse file - Error: Unsupported file type: " + fileType);
       return new ArrayList<>();
   }
}

}  
    