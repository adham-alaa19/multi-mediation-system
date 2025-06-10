/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine.utils;

/**
 *
 * @author theda
 */
import java.io.FileInputStream;
import java.io.IOException;

public class FileTypeDetector {
    
    private static final int BUFFER_SIZE = 1024;
    
    public static String detectFileType(FileInputStream stream) throws IOException {
        if (!stream.markSupported()) {
            throw new IOException("Stream does not support mark/reset - cannot detect file type");
        }
        
        stream.mark(BUFFER_SIZE);
        
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = stream.read(buffer);
            
            if (bytesRead == -1) {
                throw new IOException("Empty file - cannot determine type");
            }
            
            if (isASN1(buffer, bytesRead)) {
                return "ASN1";
            }
            
            if (isCSV(buffer, bytesRead)) {
                return "CSV";
            }
            
            throw new IllegalArgumentException("Unknown file type - not ASN.1 or CSV");
            
        } finally {
            stream.reset();
        }
    }
    
    private static boolean isASN1(byte[] buffer, int length) {
        if (length < 2) return false;
        
        byte firstByte = buffer[0];
        
        if ((firstByte & 0xFF) == 0x30) {
            return true;
        }
        
        if ((firstByte & 0xF0) == 0xA0) {
            return true;
        }
        
        if ((firstByte & 0xFF) == 0x04 || 
            (firstByte & 0xFF) == 0x02 || 
            (firstByte & 0xFF) == 0x01) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isCSV(byte[] buffer, int length) {
        if (length == 0) return false;
        
        String content = new String(buffer, 0, Math.min(length, 512));
        
        int printableCount = 0;
        int totalCount = content.length();
        
        for (char c : content.toCharArray()) {
            if (isPrintableASCII(c) || c == '\n' || c == '\r') {
                printableCount++;
            }
        }
        
        if ((double) printableCount / totalCount < 0.9) {
            return false;
        }
        
        return content.contains(",") || 
               content.contains(";") || 
               content.contains("\t") ||
               content.contains("\"");
    }
    
    private static boolean isPrintableASCII(char c) {
        return c >= 32 && c <= 126;
    }
}
