package com.iti.collectors;

import com.iti.collectors.SCPCollector;
import com.iti.models.ServerConfig;
import java.io.File;
import java.util.List;

/**
 *
 * @author user
 */

public class Main {
    public static void main(String[] args) {
        // 1. Configure the connection to your VM
        ServerConfig vmConfig = new ServerConfig();
        vmConfig.setHostname("192.168.1.227"); // <-- Replace with your VM's IP address
        vmConfig.setPort(22);                   // Default SSH port
        vmConfig.setUsername("mediation_user");   // The user you created on the VM
        vmConfig.setPassword("123"); // The password for that user

        // The full paths you created on the VM
        vmConfig.setCdr_target_path("/home/mediation_user/cdrs/new");
        vmConfig.setCdr_processed_path("/home/mediation_user/cdrs/processed");

        // 2. Create an instance of the collector and run it
        SCPCollector collector = new SCPCollector();
        System.out.println("Starting collection process...");
        List<File> collectedFiles = collector.collect(vmConfig);

        // 3. Verify the result
        if (collectedFiles.isEmpty()) {
            System.out.println("Collection finished. No files were collected.");
        } else {
            System.out.println("Collection finished. Collected files:");
            for (File file : collectedFiles) {
                System.out.println(" - " + file.getAbsolutePath());
                // In a real system, you would now pass these files to the next mediation stage.
                // For this test, we can just delete them.
                file.delete();
            }
        }
    }
}