/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.portalclient;

/**
 *
 * @author theda
 */
import com.iti.models.ServerConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

public class PortalClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final Client client;
    
    public PortalClient() {
        this.client = ClientBuilder.newClient();
    }
    
    public List<ServerConfig> getAllUpstreamServers() {
        return client
                .target(BASE_URL)
                .path("/servers/upstream")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<ServerConfig>>() {});
    }
    
    public List<ServerConfig> getAllDownstreamServers() {
        return client
                .target(BASE_URL)
                .path("/servers/downstream")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<ServerConfig>>() {});
    }
    
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
