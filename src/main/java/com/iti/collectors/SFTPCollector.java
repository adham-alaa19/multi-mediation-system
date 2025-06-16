package com.iti.collectors;

import com.iti.models.ServerConfig;
import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SFTPCollector implements CollectorStrategy {

    private static final Logger LOGGER = Logger.getLogger(SFTPCollector.class.getName());
    private static final String LOCAL_TEMP_DIR = "temp/collected/";

    @Override
    public List<FileInputStream> collect(ServerConfig config) {
        List<FileInputStream> collectedStreams = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>();
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 1. Establish SFTP Session
            session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 2. Open SFTP Channel
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // 3. Prepare Local Directory
            Files.createDirectories(Path.of(LOCAL_TEMP_DIR));

            // 4. List Remote Files
            Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(config.getCdr_target_path());
            for (ChannelSftp.LsEntry entry : fileList) {
                String fileName = entry.getFilename();
                if (entry.getAttrs().isDir() || fileName.equals(".") || fileName.equals("..")) {
                    continue;
                }

                // 5. Download File
                File localFile = new File(LOCAL_TEMP_DIR + fileName);
                try (OutputStream outputStream = new FileOutputStream(localFile)) {
                    channelSftp.get(config.getCdr_target_path() + "/" + fileName, outputStream);
                }

                // 6. Move File on Remote Server
                String from = config.getCdr_target_path() + "/" + fileName;
                String to = config.getCdr_processed_path() + "/" + fileName;
                channelSftp.rename(from, to);

                tempFiles.add(localFile);
                collectedStreams.add(new FileInputStream(localFile));
                LOGGER.log(Level.INFO, "Downloaded and moved file: {0}", fileName);
            }

        } catch (JSchException | SftpException | IOException e) {
            closeAllStreams(collectedStreams);
            deleteTempFiles(tempFiles);
            LOGGER.log(Level.SEVERE, "SFTP collection failed", e);
            throw new RuntimeException("SFTP collection failed", e);
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }

        return collectedStreams;
    }

    private void closeAllStreams(List<FileInputStream> streams) {
        for (FileInputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to close stream", e);
            }
        }
    }

    private void deleteTempFiles(List<File> files) {
        for (File file : files) {
            if (!file.delete()) {
                LOGGER.log(Level.WARNING, "Failed to delete temp file: {0}", file.getPath());
            }
        }
    }
}
