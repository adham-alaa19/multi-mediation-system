/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.collectors;

/**
 *
 * @author theda
 */
public class CollectorFactory {
    public static CollectorStrategy createCollector(String method) {
        switch (method.toUpperCase()) {
            case "SFTP" -> {
                return new SFTPCollector();
            }
            case "SCP" -> {
                return new SCPCollector();
            }
            case "REST" -> {
                return new RESTCollector() {};
            }
            default -> throw new IllegalArgumentException("Unsupported collector method: " + method);
        }
    }
}
