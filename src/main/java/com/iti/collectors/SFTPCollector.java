/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.collectors;

import com.iti.models.ServerConfig;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author theda
 */
public class SFTPCollector implements CollectorStrategy {

    @Override
    public List<FileInputStream> collect(ServerConfig config) {
        List<FileInputStream> inputStreams = new ArrayList<>();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getIp(), config.getPort());
            session.setPassword(config.getPassword());

            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");
            session.setConfig(props);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            sftpChannel.cd(config.getCdr_target_path());

            File archiveDir = new File(config.getCdr_processed_path());
            if (!archiveDir.exists()) archiveDir.mkdirs();

            List<ChannelSftp.LsEntry> fileList = sftpChannel.ls("*.csv");
            for (ChannelSftp.LsEntry entry : fileList) {
                String fileName = entry.getFilename();
                File localFile = new File(archiveDir, fileName);

                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    sftpChannel.get(fileName, fos);
                }

                FileInputStream fis = new FileInputStream(localFile);
                inputStreams.add(fis);

                try {
                    sftpChannel.mkdir(config.getCdr_processed_path());
                } catch (SftpException ignored) {
                    // Folder already exists
                }

                sftpChannel.rename(config.getCdr_target_path() + "/" + fileName,
                                   config.getCdr_processed_path() + "/" + fileName);
            }

        } catch (JSchException | SftpException | IOException e) {
            System.err.println("Error during SFTP collection: " + e.getMessage());
        } finally {
            if (sftpChannel != null) sftpChannel.exit();
            if (session != null) session.disconnect();
        }

        return inputStreams;
    }
}
