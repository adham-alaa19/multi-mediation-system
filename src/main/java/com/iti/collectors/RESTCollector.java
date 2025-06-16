/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.collectors;

import com.iti.models.ServerConfig;
import com.iti.engine.utils.ConfigUtil;  
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public abstract class RESTCollector implements CollectorStrategy {

   
    private static final String ARCHIVE_DIR = ConfigUtil.get("archive.processed.path");
    private static final String STAGING_DIR = ConfigUtil.get("staging.dir");

    @Override
    public List<FileInputStream> collect(ServerConfig config) {
        List<FileInputStream> fileStreams = new ArrayList<>();

        
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        try {
            
            String vendorUrl = "http://" + config.getIp() + ":" + config.getPort() + config.getCdr_target_path();

            
            Invocation.Builder request = client
                    .target(vendorUrl)
                    .request()
                    .header("Authorization", "Bearer " + config.getPassword()) 
                    .accept("multipart/mixed");


        
            Response response = request.get();

            if (response.getStatus() != 200) {
                System.err.println("Failed to collect files. Status: " + response.getStatus());
                return fileStreams;
            }

         
            MultiPart multipart = response.readEntity(MultiPart.class);

           for (BodyPart part : multipart.getBodyParts()) {
    String fileName = extractFileName(part.getHeaders().getFirst("Content-Disposition"));
    if (fileName == null) continue;

    InputStream inputStream = part.getEntityAs(InputStream.class);

    Path archivePath = Paths.get(ARCHIVE_DIR, fileName);
    Path stagingPath = Paths.get(STAGING_DIR, fileName);

   
    Files.createDirectories(Paths.get(ARCHIVE_DIR));
    Files.createDirectories(Paths.get(STAGING_DIR));

  
    Files.copy(inputStream, archivePath, StandardCopyOption.REPLACE_EXISTING);

   
    Files.copy(archivePath, stagingPath, StandardCopyOption.REPLACE_EXISTING);

    fileStreams.add(new FileInputStream(archivePath.toFile()));

    System.out.println("Fetched and stored file: " + fileName);
}


            multipart.close();
            response.close();

        } catch (IOException e) {
        } finally {
            client.close();
        }

        return fileStreams;
    }

    
    private String extractFileName(String contentDisposition) {
        if (contentDisposition == null) return null;
        for (String part : contentDisposition.split(";")) {
            if (part.trim().startsWith("filename")) {
                return part.split("=")[1].replace("\"", "").trim();
            }
        }
        return null;
    }
}
