/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.iti.collectors;

/**
 *
 * @author theda
 */
import com.iti.models.ServerConfig;
import java.io.FileInputStream;
import java.util.List;
public interface  CollectorStrategy {
    List<FileInputStream> collect(ServerConfig config);
}