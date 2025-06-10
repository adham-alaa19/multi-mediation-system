package com.iti.portalclient;

import com.iti.models.ServerConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author theda
 */
public class PortalClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    
    public List<ServerConfig> getAllUpstreamServers() {
        Client client = ClientBuilder.newClient();
        try {
            return client
                    .target(BASE_URL)
                    .path("/servers/upstream")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ServerConfig>>() {});
        } finally {
            client.close();
        }
    }
    
    public List<ServerConfig> getAllDownstreamServers() {
        Client client = ClientBuilder.newClient();
        try {
            return client
                    .target(BASE_URL)
                    .path("/servers/downstream")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ServerConfig>>() {});
        } finally {
            client.close();
        }
    }
    
    public void updateFileStatus(String fileName, String status, String currentDirectory) {
        Client client = ClientBuilder.newClient();
        try {
            Map<String, String> updateData = new HashMap<>();
            updateData.put("status", status);
            updateData.put("currentDirectory", currentDirectory);
            
            Response response = client
                    .target(BASE_URL)
                    .path("/files/update/{fileName}")
                    .resolveTemplate("fileName", fileName)
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(updateData));
            
            try {
                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    System.out.println("Successfully updated status for file: " + fileName);
                } else {
                    System.out.println("Failed to update status for file: " + fileName + 
                                     " - HTTP " + response.getStatus());
                }
            } finally {
                response.close();
            }
            
        } catch (Exception e) {
            System.out.println("Error updating file status for: " + fileName + 
                             " - " + e.getMessage());
        } finally {
            client.close();
        }
    }
}