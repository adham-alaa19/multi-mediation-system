package com.iti.collectors;

import com.iti.models.ServerConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


// --- Maven Dependency ---
// To use this code, you need to add the JSch library to your project.
// If you are using Maven, add this to your pom.xml:
//
// <dependency>
//     <groupId>com.jcraft</groupId>
//     <artifactId>jsch</artifactId>
//     <version>0.1.55</version>
// </dependency>


import com.jcraft.jsch.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the contract for any collector in the mediation system.
 * This interface allows for different collection strategies (e.g., FTP, SFTP, SCP)
 * to be used interchangeably.
 */
/*interface CollectorStrategy {
    /**
     * Collects files based on the provided server configuration.
     * @param config The configuration of the server to collect from.
     * @return A list of File objects representing the locally downloaded files.
     */
 /*List<FileInputStream> collect(ServerConfig config);
}
*/
/**
 * Implements the CollectorStrategy to collect files from a remote server using SCP.
 * This implementation uses a combination of 'exec' and 'scp' channels:
 * 1. An 'exec' channel runs 'ls' to list files in the target directory.
 * 2. An 'scp' channel downloads each file.
 * 3. Another 'exec' channel runs 'mv' to move the file on the remote server after download.
 *
 * @author theda
 */
public class SCPCollector implements CollectorStrategy {

    private static final Logger LOGGER = Logger.getLogger(SCPCollector.class.getName());
    private static final String LOCAL_TEMP_DIR = "temp/collected/";

    @Override
    public List<FileInputStream> collect(ServerConfig config) {
        List<FileInputStream> collectedStreams = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>(); // To track files for cleanup
        JSch jsch = new JSch();
        Session session = null;

        try {
            // 1. Establish SSH Session
            session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 2. Prepare Local Directory
            Files.createDirectories(Path.of(LOCAL_TEMP_DIR));

            // 3. List Remote Files
            List<String> fileNames = listRemoteFiles(session, config.getCdr_target_path());
            if (fileNames.isEmpty()) {
                return collectedStreams;
            }

            // 4. Process Each File
            for (String fileName : fileNames) {
                File localFile = new File(LOCAL_TEMP_DIR + fileName);
                tempFiles.add(localFile); // Track for cleanup

                if (downloadFile(session, config.getCdr_target_path() + "/" + fileName, localFile)) {
                    if (moveRemoteFile(session, config.getCdr_target_path(), 
                                      config.getCdr_processed_path(), fileName)) {
                        collectedStreams.add(new FileInputStream(localFile));
                    } else {
                        localFile.delete();
                        tempFiles.remove(localFile);
                    }
                }
            }
        } catch (JSchException | IOException | InterruptedException e) {
            // Cleanup on error
            closeAllStreams(collectedStreams);
            deleteTempFiles(tempFiles);
            throw new RuntimeException("Collection failed", e);
        } finally {
            if (session != null) session.disconnect();
        }
        return collectedStreams;
    }

    private void closeAllStreams(List<FileInputStream> streams) {
        streams.forEach(stream -> {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to close stream", e);
            }
        });
    }

    private void deleteTempFiles(List<File> files) {
        files.forEach(file -> {
            if (!file.delete()) {
                LOGGER.log(Level.WARNING, "Failed to delete temp file: {0}", file.getPath());
            }
        });
    }
    /**
     * Executes 'ls -p' on the remote server to list files in a directory.
     * The '-p' flag is used to append a '/' to directory names, allowing us to easily filter them out.
     */
    private List<String> listRemoteFiles(Session session, String remotePath) throws JSchException, IOException {
        List<String> fileNames = new ArrayList<>();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("ls -p " + remotePath);
        channel.setInputStream(null); // We are only reading from the output stream of the command.
        
        try (InputStream in = channel.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            channel.connect();
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore directories (which end with '/') and empty lines.
                if (!line.trim().isEmpty() && !line.trim().endsWith("/")) {
                    fileNames.add(line.trim());
                }
            }
        } finally {
            channel.disconnect();
        }
        return fileNames;
    }

    /**
     * Downloads a single file from the remote server using the SCP protocol via an 'exec' channel.
     */
    private boolean downloadFile(Session session, String remoteFilePath, File localFile) throws JSchException, IOException {
        // The command 'scp -f' tells the remote server to send a file (from mode).
        String command = "scp -f " + remoteFilePath;
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        try (OutputStream localOutStream = new FileOutputStream(localFile);
             InputStream remoteInStream = channel.getInputStream();
             OutputStream remoteOutStream = channel.getOutputStream()) {
            
            channel.connect();

            // The SCP protocol requires a specific handshake of sending and receiving
            // null bytes as acknowledgements.
            remoteOutStream.write(0); // Send initial '\0'
            remoteOutStream.flush();
            
            while (true) {
                int ack = checkAck(remoteInStream);
                if (ack != 'C') { // 'C' indicates the start of a file transfer header
                    break;
                }

                // Read file information (mode, size, name) sent by the server
                remoteInStream.read(new byte[5]); // Read and discard mode (e.g., C0644)
                
                long filesize = 0L;
                byte[] buf = new byte[1024];
                while (true) {
                    if (remoteInStream.read(buf, 0, 1) < 0) break;
                    if (buf[0] == ' ') break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                // Read and discard the rest of the file info line (filename)
                String fileInfo;
                for (int i = 0; ; i++) {
                    remoteInStream.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        fileInfo = new String(buf, 0, i);
                        break;
                    }
                }
                LOGGER.log(Level.INFO, "Downloading file: {0} ({1} bytes) to {2}", new Object[]{fileInfo, filesize, localFile.getName()});

                // Send acknowledgement to start the actual file content transfer
                remoteOutStream.write(0);
                remoteOutStream.flush();

                // Read the file content from the remote stream and write it to the local file
                int bytesRead;
                while (filesize > 0) {
                    bytesRead = remoteInStream.read(buf, 0, (int) Math.min(filesize, buf.length));
                    if (bytesRead < 0) break;
                    localOutStream.write(buf, 0, bytesRead);
                    filesize -= bytesRead;
                }

                if (checkAck(remoteInStream) != 0) { // Check for the acknowledgement after file content
                    return false;
                }

                // Send final acknowledgement
                remoteOutStream.write(0);
                remoteOutStream.flush();
            }
        } finally {
            channel.disconnect();
        }
        return true;
    }

    /**
     * Executes the 'mv' command on the remote server to move a file from the source
     * directory to the processed directory.
     */
    private boolean moveRemoteFile(Session session, String fromPath, String toPath, String fileName) throws JSchException, InterruptedException {
        String fromFile = fromPath.endsWith("/") ? fromPath + fileName : fromPath + "/" + fileName;
        String toFile = toPath.endsWith("/") ? toPath + fileName : toPath + "/" + fileName;
        String command = "mv " + fromFile + " " + toFile;
        
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        
        // Wait for the command to finish. A successful 'mv' usually returns exit code 0.
        // Polling isClosed() is a common way to wait for completion.
        while(!channel.isClosed()){
            Thread.sleep(100);
        }
        
        int exitStatus = channel.getExitStatus();
        channel.disconnect();
        
        if (exitStatus == 0) {
            LOGGER.log(Level.INFO, "Successfully moved remote file: {0}", fileName);
            return true;
        } else {
            LOGGER.log(Level.WARNING, "Remote move command for ''{0}'' failed with exit status {1}", new Object[]{fileName, exitStatus});
            return false;
        }
    }
    
    /**
     * Helper method to check the acknowledgement byte from the remote server for the SCP protocol.
     * @return 0 for success, -1 for EOF, or a specific error code.
     */
    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success, 1 for error, 2 for fatal error, -1 for EOF.
        if (b == 0 || b == -1) return b;
        if (b == 1 || b == 2) {
            // If there's an error, read the rest of the error message line.
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            LOGGER.log(Level.SEVERE, "SCP Protocol Error: {0}", sb.toString());
        }
        return b;
    }}

