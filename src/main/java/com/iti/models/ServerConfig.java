/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.models;

/**
 *
 * @author theda
 */
public class ServerConfig {
    private int config_id;
    private int server_id;
    private String method;
    private String ip;
    private String hostname;
    private int port;
    private String username;
    private String password;
    private String cdr_target_path;
    private String cdr_processed_path;
    private String direction;
    
    // Default constructor
    public ServerConfig() {}
    
    // Full constructor
    public ServerConfig(int config_id, int server_id, String method, String ip, 
                       String hostname, int port, String username, String password,
                       String cdr_target_path, String cdr_processed_path, String direction) {
        this.config_id = config_id;
        this.server_id = server_id;
        this.method = method;
        this.ip = ip;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.cdr_target_path = cdr_target_path;
        this.cdr_processed_path = cdr_processed_path;
        this.direction = direction;
    }
    
    // Getters and Setters
    public int getConfig_id() {
        return config_id;
    }
    
    public void setConfig_id(int config_id) {
        this.config_id = config_id;
    }
    
    public int getServer_id() {
        return server_id;
    }
    
    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getCdr_target_path() {
        return cdr_target_path;
    }
    
    public void setCdr_target_path(String cdr_target_path) {
        this.cdr_target_path = cdr_target_path;
    }
    
    public String getCdr_processed_path() {
        return cdr_processed_path;
    }
    
    public void setCdr_processed_path(String cdr_processed_path) {
        this.cdr_processed_path = cdr_processed_path;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
}