package com.iti.fileparsers;

public class ParserFactory {
    public static ParserStrategy createParser(String fileType) {
        switch (fileType.toUpperCase()) {
            case "ASN1" -> {
                return (ParserStrategy) new ASN1Parser();
            }
            case "CSV" -> {
                return new CSVParser();
            }
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }
}