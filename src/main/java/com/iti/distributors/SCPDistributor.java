/*
 * This class is part of a mediation system and is responsible for distributing
 * (uploading) processed files to a remote server using the SCP protocol.
 */
package com.iti.distributors;

// --- Maven Dependency ---
// To use this code, you need to add the JSch library to your project.
// If you are using Maven, add this to your pom.xml:
//
// <dependency>
//     <groupId>com.jcraft</groupId>
//     <artifactId>jsch</artifactId>
//     <version>0.1.55</version>
// </dependency>

import com.iti.models.ServerConfig;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the contract for any distributor in the mediation system.
 * This allows for different distribution strategies (e.g., FTP, SFTP, SCP)
 * to be used interchangeably.
 */
interface DistributorStrategy {
    /**
     * Distributes a list of processed files to a target server.
     * @param config The configuration of the server to distribute to.
     * @param processedFiles A list of local files to be uploaded.
     * @return true if the distribution of all files was successful, false otherwise.
     */
    boolean distribute(ServerConfig config, List<File> processedFiles);
}

/**
 * Implements the DistributorStrategy to distribute (upload) files to a remote
 * server using SCP. This uses JSch's 'exec' channel to run the 'scp -t' command.
 *
 * @author theda
 */
public class SCPDistributor implements DistributorStrategy {

    private static final Logger LOGGER = Logger.getLogger(SCPDistributor.class.getName());

    @Override
    public boolean distribute(ServerConfig config, List<File> processedFiles) {
        if (processedFiles == null || processedFiles.isEmpty()) {
            LOGGER.log(Level.INFO, "No processed files to distribute.");
            return true; // Nothing to do, so operation is considered successful.
        }

        JSch jsch = new JSch();
        Session session = null;
        boolean allSuccessful = true;

        try {
            // --- 1. Establish SSH Session ---
            LOGGER.log(Level.INFO, "Connecting to {0}@{1} for distribution...", new Object[]{config.getUsername(), config.getHostname()});
            session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no"); // For dev; use known_hosts in production
            session.connect();
            LOGGER.log(Level.INFO, "Session connected.");

            // --- 2. Upload Each File ---
            for (File localFile : processedFiles) {
                if (!uploadFile(session, localFile, config.getCdr_target_path())) {
                    allSuccessful = false; // If any file fails, mark the overall operation as failed.
                }
            }

        } catch (JSchException e) {
            LOGGER.log(Level.SEVERE, "An error occurred during SCP connection.", e);
            allSuccessful = false;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
                LOGGER.log(Level.INFO, "Session disconnected.");
            }
        }
        
        return allSuccessful;
    }

    /**
     * Uploads a single local file to a remote directory using SCP.
     */
    private boolean uploadFile(Session session, File localFile, String remoteDir) {
        if (!localFile.exists()) {
            LOGGER.log(Level.WARNING, "Local file does not exist, skipping distribution: {0}", localFile.getAbsolutePath());
            return false;
        }

        boolean preserveTimestamp = true; // Preserve file modification timestamps
        // The '-t' flag tells the remote scp to expect a file (to mode).
        String remoteFileName = localFile.getName();
        String command = "scp " + (preserveTimestamp ? "-p" : "") + " -t " + remoteDir + "/" + remoteFileName;
        ChannelExec channel = null;
        
        try (FileInputStream fis = new FileInputStream(localFile)) {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            try (OutputStream remoteOutStream = channel.getOutputStream();
                 InputStream remoteInStream = channel.getInputStream()) {

                channel.connect();
                
                if (checkAck(remoteInStream) != 0) {
                    return false;
                }
                
                // Send file modification times if ptimestamp is true
                if (preserveTimestamp) {
                    String timeCommand = "T" + (localFile.lastModified() / 1000) + " 0";
                    timeCommand += " " + (localFile.lastModified() / 1000) + " 0\n";
                    remoteOutStream.write(timeCommand.getBytes());
                    remoteOutStream.flush();
                    if (checkAck(remoteInStream) != 0) return false;
                }
                
                // Send "C0644 filesize filename\n", where C0644 is the file mode.
                long filesize = localFile.length();
                String header = "C0644 " + filesize + " " + remoteFileName + "\n";
                remoteOutStream.write(header.getBytes());
                remoteOutStream.flush();

                if (checkAck(remoteInStream) != 0) {
                    return false;
                }

                // Send the file content.
                byte[] buf = new byte[1024];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    remoteOutStream.write(buf, 0, len);
                }
                
                // Send '\0' to signal end of file transfer for this file.
                remoteOutStream.write(0);
                remoteOutStream.flush();

                if (checkAck(remoteInStream) != 0) {
                    return false;
                }
            }
        } catch (JSchException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to distribute file: " + localFile.getName(), e);
            return false;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        LOGGER.log(Level.INFO, "Successfully distributed file: {0}", localFile.getName());
        return true;
    }
    
    /**
     * Helper method to check acknowledgement from the input stream for the SCP protocol.
     */
    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success, 1 for error, 2 for fatal error, -1 for EOF
        if (b == 0 || b == -1) return b;
        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            LOGGER.log(Level.SEVERE, "SCP Protocol Error: {0}", sb.toString());
        }
        return b;
    }
}
