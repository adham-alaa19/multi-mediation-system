/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine.services;

import java.io.FileInputStream;
import java.util.List;

/**
 *
 * @author theda
 */

import com.iti.models.ServerConfig;
import com.iti.collectors.CollectorStrategy;
import com.iti.collectors.CollectorFactory;
import com.iti.portalclient.PortalClient;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;

public class CollectionService {
   
   private PortalClient portalClient;
   
   public CollectionService() {
       this.portalClient = new PortalClient();
   }
   
   public List<FileInputStream> collectFromUpstreamServers() {
       List<ServerConfig> upstreamServers = portalClient.getAllUpstreamServers();
       List<FileInputStream> allFiles = new ArrayList<>();
       
       for (ServerConfig server : upstreamServers) {
           System.out.println("Attempting collection from server: " + server.getIp()+ " using protocol: " + server.getMethod());
           
           try {
               CollectorStrategy collector = CollectorFactory.createCollector(server.getMethod());
               List<FileInputStream> serverFiles = collector.collect(server);
               allFiles.addAll(serverFiles);
               
               System.out.println("Collected " + serverFiles.size() + " files from server: " + server.getIp()+ " using protocol: " + server.getMethod());
               
           } catch (Exception e) {
               System.out.println("Collected 0 files from server: "+ server.getIp()+ " using protocol: " + server.getMethod() + " - Error: " + e.getMessage());
           }
       }
       
       System.out.println("Total files collected: " + allFiles.size() + " from " + upstreamServers.size() + " servers");
       return allFiles;
   }
}
