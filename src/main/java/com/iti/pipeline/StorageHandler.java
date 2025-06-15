package com.iti.pipeline;

import com.iti.models.MediationContext;
import com.iti.portalclient.PortalClient;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageHandler extends MediationHandler {
    private static final Logger LOGGER = Logger.getLogger(StorageHandler.class.getName());

    private final Path stagingDir = Paths.get("resources/staging");
    private final Path archiveDir = Paths.get("logs/archive/processed");
    private final PortalClient portalClient = new PortalClient();

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        try {
            String fileName = context.getAsString("fileName");
            String processedAtStr = context.getAsString("start_time");

            if (fileName == null) {
                return new MediationStepResult(context, false, "Missing file name");
            }

            Path sourceFile = stagingDir.resolve(fileName);

            Instant fileDate = processedAtStr != null
                    ? Instant.parse(processedAtStr)
                    : Instant.now();

            Path archivePath = buildArchivePath(fileDate, fileName);
            Files.createDirectories(archivePath.getParent());

            Files.move(sourceFile, archivePath, StandardCopyOption.REPLACE_EXISTING);

            portalClient.updateFileStatus(fileName, "ARCHIVED", archivePath.toString());

            context.setField("archivePath", archivePath.toString());
            context.setField("archivedAt", Instant.now().toString());

            return new MediationStepResult(context, true, "File archived successfully");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Archiving failed", e);
            return new MediationStepResult(context, false, "Archiving failed: " + e.getMessage());
        }
    }

    private Path buildArchivePath(Instant fileDate, String fileName) {
        LocalDateTime dt = LocalDateTime.ofInstant(fileDate, ZoneId.systemDefault());
        return archiveDir
                .resolve(String.valueOf(dt.getYear()))
                .resolve(String.format("%02d", dt.getMonthValue()))
                .resolve(String.format("%02d", dt.getDayOfMonth()))
                .resolve(fileName);
    }
}
