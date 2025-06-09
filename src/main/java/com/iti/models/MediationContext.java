/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.models;

/**
 *
 * @author theda
 */

import java.util.Map;

public class MediationContext {

    private Map<String, Object> fields;   
    private String serviceType;          

    public MediationContext() {
    }

    public MediationContext(Map<String, Object> fields, String serviceType) {
        this.fields = fields;
        this.serviceType = serviceType;
    }

    // Getters and Setters

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }


    public Object getField(String key) {
        if (fields != null) {
            return fields.get(key);
        }
        return null;
    }

    public void setField(String key, Object value) {
        if (fields != null) {
            fields.put(key, value);
        }
    }


    public String getAsString(String key) {
        Object val = getField(key);
        return val != null ? val.toString() : null;
    }

    public Integer getAsInt(String key) {
        Object val = getField(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return val != null ? Integer.parseInt(val.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getAsDouble(String key) {
        Object val = getField(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return val != null ? Double.parseDouble(val.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

