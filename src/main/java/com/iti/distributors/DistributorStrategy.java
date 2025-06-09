/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.iti.distributors;

/**
 *
 * @author theda
 */
import com.iti.models.ServerConfig;
import java.util.List;
import java.util.Map;
import java.io.File;

// Strategy Interface
public interface DistributorStrategy {
    boolean distribute(ServerConfig config, List<File> processedFile);
}
