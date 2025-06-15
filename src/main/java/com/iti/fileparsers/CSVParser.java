package com.iti.fileparsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;  
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVParser implements ParserStrategy {

    @Override
public List<Map<String, Object>> parse(FileInputStream file) {
    List<Map<String, Object>> result = new ArrayList<>();
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
        String line;
        String[] headers = null;
        boolean firstLine = true;
        
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            String[] values = parseCsvLine(line);
            
            if (firstLine) {
                headers = values;
                firstLine = false;
            } else {
                if (headers != null) {
                    Map<String, Object> row = new HashMap<>();
                    
                    for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                        String header = headers[i].trim();
                        String value = values[i].trim();           
                        Object convertedValue = convertValue(value);
                        row.put(header, convertedValue);
                    }
                    
                    result.add(row);
                }
            }
        }
        
    } catch (IOException e) {
        throw new RuntimeException("Error parsing CSV file: " + e.getMessage(), e);
    }
    
    return result;
}

private String[] parseCsvLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder currentField = new StringBuilder();
    boolean inQuotes = false;
    
    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        
        if (c == '"') {
            inQuotes = !inQuotes;
        } else if (c == ',' && !inQuotes) {
            fields.add(currentField.toString());
            currentField = new StringBuilder();
        } else {
            currentField.append(c);
        }
    }
    
    fields.add(currentField.toString());
    
    return fields.toArray(new String[0]);
}


private Object convertValue(String value) {
    if (value == null || value.isEmpty()) {
        return null;
    }
    
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e2) {
            return value;
        }
    }
}
    
}