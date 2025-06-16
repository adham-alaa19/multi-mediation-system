package com.iti.distributors;

import com.iti.models.ServerConfig;
import com.iti.engine.utils.ConfigUtil;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SFTPDistributor implements DistributorStrategy {

    private static final Logger LOGGER = Logger.getLogger(SFTPDistributor.class.getName());
    private static final String SENT_DIR = ConfigUtil.get("archive.sent.path");
    private static final String SENT_FAILED_DIR = ConfigUtil.get("archive.sent_failed.path");

    @Override
    public boolean distribute(ServerConfig config, List<File> processedFiles) {
        if (processedFiles == null || processedFiles.isEmpty()) {
            LOGGER.log(Level.INFO, "No files to distribute.");
            return true;
        }

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        boolean allSuccessful = true;

        try {
            // 1. Establish SSH session
            session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            LOGGER.log(Level.INFO, "SFTP session established with " + config.getHostname());

            // 2. Open SFTP channel
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // 3. Upload each file
            for (File file : processedFiles) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String remotePath = config.getCdr_target_path() + "/" + file.getName();
                    sftpChannel.put(fis, remotePath);
                    LOGGER.log(Level.INFO, "Uploaded file: " + file.getName());

                    // Archive to sent folder
                    Files.move(file.toPath(),
                            Paths.get(SENT_DIR, file.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to upload file: " + file.getName(), e);

                    // Move to sent_failed
                    try {
                        Files.move(file.toPath(),
                                Paths.get(SENT_FAILED_DIR, file.getName()),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException moveEx) {
                        LOGGER.log(Level.SEVERE, "Failed to move file to sent_failed: " + file.getName(), moveEx);
                    }

                    allSuccessful = false;
                }
            }

        } catch (JSchException e) {
            LOGGER.log(Level.SEVERE, "SFTP connection error", e);
            allSuccessful = false;
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            LOGGER.log(Level.INFO, "SFTP session closed.");
        }

        return allSuccessful;
    }
}
