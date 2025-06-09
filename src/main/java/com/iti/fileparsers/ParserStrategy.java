package com.iti.fileparsers;


import java.io.FileInputStream;
import java.util.List;
import java.util.Map;



public interface ParserStrategy {
        List<Map<String, Object>> parse(FileInputStream file);
}