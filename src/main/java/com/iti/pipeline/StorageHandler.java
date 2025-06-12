package com.iti.pipeline;

import com.iti.models.MediationContext;
import com.iti.models.CdrFileMetadata;
import com.iti.portalclient.PortalClient;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageHandler extends MediationHandler {
    private static final Logger LOGGER = Logger.getLogger(StorageHandler.class.getName());
    
    // Configurable paths (could be injected via constructor)
    private final Path stagingDir = Paths.get("/mediation/staging");
    private final Path archiveDir = Paths.get("/mediation/archive");
    private final PortalClient portalClient = new PortalClient();

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        CdrFileMetadata metadata = context.getCdrMetadata();
        Path sourceFile = stagingDir.resolve(metadata.getFileName());
        
        try {
            // 1. Create archive directory structure (year/month/day)
            Path archivePath = buildArchivePath(metadata);
            Files.createDirectories(archivePath.getParent());
            
            // 2. Move file to archive
            Path destination = archivePath.resolve(metadata.getFileName());
            Files.move(sourceFile, destination, StandardCopyOption.REPLACE_EXISTING);
            
            // 3. Update database status
            updateFileStatus(metadata, destination.toString());
            
            // 4. Update context with new location
            metadata.setArchivePath(destination.toString());
            metadata.setArchivedAt(Instant.now());
            
            return MediationStepResult.success(metadata);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to archive file: " + metadata.getFileName(), e);
            return MediationStepResult.failure("Archiving failed: " + e.getMessage());
        }
    }

    private Path buildArchivePath(CdrFileMetadata metadata) {
        // Organize by date for easier retrieval
        // Format: /archive/{year}/{month}/{day}/{filename}
        Instant fileDate = metadata.getProcessedAt() != null ? 
                          metadata.getProcessedAt() : Instant.now();
        
        return archiveDir.resolve(
            String.format("%d/%02d/%02d", 
                fileDate.getYear(),
                fileDate.getMonthValue(),
                fileDate.getDayOfMonth())
        );
    }

    private void updateFileStatus(CdrFileMetadata metadata, String newLocation) {
        try {
            portalClient.updateFileStatus(
                metadata.getFileName(),
                "ARCHIVED",
                newLocation
            );
            LOGGER.info("Updated status for: " + metadata.getFileName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, 
                "Failed to update DB status for: " + metadata.getFileName(), e);
        }
    }
}