/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine.services;

/**
 *
 * @author theda
 */

import com.iti.models.ServerConfig;
import com.iti.distributors.DistributorStrategy;
import com.iti.distributors.DistributorFactory;
import com.iti.portalclient.PortalClient;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

public class DistributionService {
    
    private PortalClient portalClient;
    private Properties config;
    
    public DistributionService() {
        this.portalClient = new PortalClient();
        this.config = new Properties();
        try {
            config.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            System.out.println("Failed to load config.properties: " + e.getMessage());
        }
    }
    
    public void distributeToDownstreamServers() {
        String processedPath = config.getProperty("archive.processed.path");
        File processedDir = new File(processedPath);
        File[] processedFiles = processedDir.listFiles();
        
        if (processedFiles == null || processedFiles.length == 0) {
            System.out.println("No processed files found for distribution");
            return;
        }
        
        List<ServerConfig> downstreamServers = portalClient.getAllDownstreamServers();
        
        for (File file : processedFiles) {
            System.out.println("Processing file: " + file.getName() + " for distribution");
            
            List<File> fileList = new ArrayList<>();
            fileList.add(file);
            
            int successCount = 0;
            int totalServers = downstreamServers.size();
            
            for (ServerConfig server : downstreamServers) {
                System.out.println("Attempting distribution to server: " + server.getIp() + " using protocol: " + server.getMethod());
                
                try {
                    DistributorStrategy distributor = DistributorFactory.createDistributor(server.getMethod());
                    boolean success = distributor.distribute(server, fileList);
                    
                    if (success) {
                        successCount++;
                        System.out.println("Successfully distributed to server: " + server.getIp());
                    } else {
                        System.out.println("Failed to distribute to server: " +   server.getIp() + " using protocol: " + server.getMethod());
                    }
                    
                } catch (Exception e) {
                    System.out.println("Failed to distribute to server: " + server.getIp() + " - Error: " + e.getMessage());
                }
            }
            
            // Move file based on distribution results and get new file location
            File movedFile = moveFileBasedOnResults(file, successCount, totalServers);
            
            // Update database with final status and directory location
            updateFileStatus(file.getName(), successCount, totalServers, movedFile.getParent());
        }
    }
    
    private File moveFileBasedOnResults(File file, int successCount, int totalServers) {
        String destinationPath;
        String status;
        
        if (successCount == totalServers) {
            destinationPath = config.getProperty("archive.sent.path");
            status = "SENT";
        } else if (successCount > 0) {
            destinationPath = config.getProperty("archive.sent_incomplete.path");
            status = "SENT_INCOMPLETE";
        } else {
            destinationPath = config.getProperty("archive.sent_failed.path");
            status = "SENT_FAILED";
        }
        
        File destinationDir = new File(destinationPath);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        
        File destinationFile = new File(destinationDir, file.getName());
        if (file.renameTo(destinationFile)) {
            System.out.println("Moved file " + file.getName() + " to " + destinationPath + " with status: " + status);
            return destinationFile;
        } else {
            System.out.println("Failed to move file " + file.getName() + " to " + destinationPath);
            return file; // Return original file if move failed
        }
    }
    
    private void updateFileStatus(String fileName, int successCount, int totalServers, String currentDirectory) {
        String status;
        if (successCount == totalServers) {
            status = "SENT";
        } else if (successCount > 0) {
            status = "SENT_INCOMPLETE";
        } else {
            status = "SENT_FAILED";
        }
        
        portalClient.updateFileStatus(fileName, status, currentDirectory);
    }
}
