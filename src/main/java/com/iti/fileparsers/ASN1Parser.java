package com.iti.parsers;

import java.io.*;
import java.util.*;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.util.ASN1Dump;

/**
 * Parses ASN.1 encoded CDR files into Map objects for mediation processing.
 */
public class ASN1Parser {

    private static final Logger LOGGER = Logger.getLogger(ASN1Parser.class.getName());

    /**
     * Parses an ASN.1 CDR file into a structured Map.
     * @param cdrFile The input CDR file (binary ASN.1 format)
     * @return Map representing the CDR fields, or empty Map if parsing fails
     */
    public Map<String, Object> parseCdrFile(File cdrFile) {
        Map<String, Object> cdrMap = new LinkedHashMap<>();
        
        try (InputStream is = new FileInputStream(cdrFile)) {
            ASN1InputStream asn1Input = new ASN1InputStream(is);
            ASN1Primitive asn1Object = asn1Input.readObject();
            
            if (asn1Object instanceof ASN1Sequence) {
                parseAsn1Sequence((ASN1Sequence) asn1Object, cdrMap);
            } else {
                LOGGER.warning("Unsupported ASN.1 type: " + asn1Object.getClass().getSimpleName());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse CDR file: " + cdrFile.getName(), e);
        }
        
        return cdrMap;
    }

    /**
     * Recursively processes ASN.1 sequence into key-value pairs.
     */
    private void parseAsn1Sequence(ASN1Sequence sequence, Map<String, Object> outputMap) {
        Enumeration<ASN1Encodable> elements = sequence.getObjects();
        int fieldIndex = 1; // Telecom CDRs often use positional fields
        
        while (elements.hasMoreElements()) {
            ASN1Encodable element = elements.nextElement();
            String fieldName = "field_" + fieldIndex++; // Default field naming
            
            if (element instanceof ASN1TaggedObject) {
                // Handle explicitly tagged fields (common in telecom CDRs)
                ASN1TaggedObject tagged = (ASN1TaggedObject) element;
                fieldName = "tag_" + tagged.getTagNo();
                element = tagged.getObject();
            }
            
            outputMap.put(fieldName, parseAsn1Element(element));
        }
    }

    /**
     * Converts ASN.1 elements to Java objects.
     */
    private Object parseAsn1Element(ASN1Encodable element) {
        if (element instanceof ASN1Integer) {
            return ((ASN1Integer) element).getValue();
        } else if (element instanceof ASN1OctetString) {
            return ((ASN1OctetString) element).getString();
        } else if (element instanceof ASN1UTCTime) {
            return ((ASN1UTCTime) element).getAdjustedDate();
        } else if (element instanceof ASN1Sequence) {
            Map<String, Object> nestedMap = new LinkedHashMap<>();
            parseAsn1Sequence((ASN1Sequence) element, nestedMap);
            return nestedMap;
        } else {
            // Fallback: Use ASN.1 dump for unsupported types
            return ASN1Dump.dumpAsString(element);
        }
    }
}