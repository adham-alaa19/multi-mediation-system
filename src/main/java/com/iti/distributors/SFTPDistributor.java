/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.distributors;

import com.iti.models.ServerConfig;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class SFTPDistributor implements DistributorStrategy {

    @Override
    public boolean distribute(ServerConfig config, List<File> processedFiles) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            // 1. Connect to SFTP server
            JSch jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getIp(), config.getPort());
            session.setPassword(config.getPassword());

            Properties configProps = new Properties();
            configProps.put("StrictHostKeyChecking", "no");
            session.setConfig(configProps);
            session.connect(10000); // 10 seconds timeout

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // 2. Navigate to target path or create it
            try {
                sftpChannel.cd(config.getCdr_target_path());
            } catch (SftpException e) {
                sftpChannel.mkdir(config.getCdr_target_path());
                sftpChannel.cd(config.getCdr_target_path());
            }

            // 3. Upload files
            for (File file : processedFiles) {
                if (!file.isFile()) continue;

                try (FileInputStream fis = new FileInputStream(file)) {
                    sftpChannel.put(fis, file.getName());
                }

                // 4. Optional: Confirm file exists remotely
                try {
                    sftpChannel.lstat(file.getName());
                } catch (SftpException e) {
                    System.err.println("Upload failed or file missing remotely: " + file.getName());
                    return false;
                }

                // 5. Move file to local delivered/ folder
                File deliveredDir = new File(file.getParentFile(), "delivered");
                if (!deliveredDir.exists()) {
                    if (!deliveredDir.mkdir()) {
                        System.err.println("Failed to create 'delivered' directory");
                        return false;
                    }
                }
                File deliveredFile = new File(deliveredDir, file.getName());
                if (!file.renameTo(deliveredFile)) {
                    System.err.println("Failed to move file to delivered folder: " + file.getName());
                    return false;
                }
            }

            return true;

        } catch (JSchException | SftpException | IOException ex) {
            System.err.println("SFTP Distribution Error: " + ex.getMessage());
            return false;

        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) sftpChannel.exit();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }
}
