/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.distributors;

import com.iti.models.ServerConfig;
import com.iti.engine.utils.ConfigUtil;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;


import java.io.File;
import java.util.List;
import java.nio.file.*;

public class RESTDistributor implements DistributorStrategy {

    private static final String SENT_DIR = ConfigUtil.get("archive.sent.path");
    private static final String SENT_FAILED_DIR = ConfigUtil.get("archive.sent_failed.path");

    @Override
    public boolean distribute(ServerConfig config, List<File> processedFiles) {
        boolean allSuccess = true;

        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        for (File file : processedFiles) {
            try {
                String targetUrl = "http://" + config.getIp() + ":" + config.getPort() + config.getCdr_target_path();

               
                FormDataMultiPart multipart = (FormDataMultiPart) new FormDataMultiPart()
                        .field("vendorId", String.valueOf((char) config.getServer_id()))
                        .field("fileName", file.getName())
                        .field("timestamp", String.valueOf(file.lastModified()))
                        .bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));

              
                Invocation.Builder request = client
                        .target(targetUrl)
                        .request()
                        .header("Authorization", "Bearer " + config.getPassword()) 
                        .accept(MediaType.APPLICATION_JSON);

                Response response = request.post(Entity.entity(multipart, multipart.getMediaType()));

                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    System.out.println("File sent successfully: " + file.getName());

                
                    Files.move(file.toPath(),
                               Paths.get(SENT_DIR, file.getName()),
                               StandardCopyOption.REPLACE_EXISTING);
                } else {
                    System.err.println("Failed to send file: " + file.getName() + " - Status: " + response.getStatus());

                
                    Files.move(file.toPath(),
                               Paths.get(SENT_FAILED_DIR, file.getName()),
                               StandardCopyOption.REPLACE_EXISTING);
                    allSuccess = false;
                }

                response.close();
                multipart.close();

            } catch (Exception e) {
                System.err.println("Exception sending file: " + file.getName());
                e.printStackTrace();

                try {
                    Files.move(file.toPath(),
                               Paths.get(SENT_FAILED_DIR, file.getName()),
                               StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception moveEx) {
                    System.err.println("Error moving failed file: " + moveEx.getMessage());
                }

                allSuccess = false;
            }
        }

        client.close();
        return allSuccess;
    }
}

