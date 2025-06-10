/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.models;

import java.sql.Timestamp;

/**
 *
 * @author theda
 */
public class Dedup_Hash {
    private int hash_id;
    private String hash_value;
    private Timestamp createdAt;

    // No-argument constructor
    public Dedup_Hash() {
    }

    // Parameterized constructor
    public Dedup_Hash(int hashId, String hashValue, Timestamp createdAt) {
        this.hash_id = hashId;
        this.hash_value = hashValue;
        this.createdAt = createdAt;
    }

    // Getter and Setter for hashId
    public int getHashId() {
        return hash_id;
    }

    public void setHashId(int hashId) {
        this.hash_id = hashId;
    }

    // Getter and Setter for hashValue
    public String getHashValue() {
        return hash_value;
    }

    public void setHashValue(String hashValue) {
        this.hash_value = hashValue;
    }

    // Getter and Setter for createdAt
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
