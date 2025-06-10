/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.distributors;

/**
 *
 * @author theda
 */
public class DistributorFactory {
    public static DistributorStrategy createDistributor(String method) {
        switch (method.toUpperCase()) {
            case "SFTP":
                return new SFTPDistributor();
            case "SCP":
                return new SCPDistributor();
            case "REST":
                return new RESTDistributor();
            default:
                throw new IllegalArgumentException("Unsupported distributor method: " + method);
        }
    }
}