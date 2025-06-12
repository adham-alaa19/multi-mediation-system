/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.engine.utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 *
 * @author DeLL
 */
public class ConfigUtil {
     private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ config.properties not found in resources!");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
