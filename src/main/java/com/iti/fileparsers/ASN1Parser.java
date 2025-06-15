package com.iti.fileparsers;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.util.ASN1Dump;
import java.nio.charset.StandardCharsets;


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
            
            if (asn1Object instanceof ASN1Sequence aSN1Sequence) {
                parseAsn1Sequence(aSN1Sequence, cdrMap);
            } else {
                LOGGER.log(Level.WARNING, "Unsupported ASN.1 type: {0}", asn1Object.getClass().getSimpleName());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse CDR file: " + cdrFile.getName(), e);
        }
        
        return cdrMap;
    }

    /**
     * Recursively processes ASN.1 sequence into key-value pairs.
     */
    private void parseAsn1Sequence(ASN1Sequence sequence, Map<String, Object> outputMap) throws ParseException {
        Enumeration<ASN1Encodable> elements = sequence.getObjects();
        int fieldIndex = 1; // Telecom CDRs often use positional fields
        
        while (elements.hasMoreElements()) {
            ASN1Encodable element = elements.nextElement();
            String fieldName = "field_" + fieldIndex++; // Default field naming
            
            if (element instanceof ASN1TaggedObject tagged) {
// Handle explicitly tagged fields (common in telecom CDRs)
                                fieldName = "tag_" + tagged.getTagNo();
                element = tagged.getObject();
            }
            
            outputMap.put(fieldName, parseAsn1Element(element));
        }
    }

    
    private Object parseAsn1Element(ASN1Encodable element) throws ParseException {
        if (element instanceof ASN1Integer aSN1Integer) {
            return aSN1Integer.getValue();
        } else if (element instanceof ASN1OctetString aSN1OctetString) {
       return new String(aSN1OctetString.getOctets(), StandardCharsets.UTF_8);
        } else if (element instanceof ASN1UTCTime aSN1UTCTime) {
            return aSN1UTCTime.getAdjustedDate();
        } else if (element instanceof ASN1Sequence aSN1Sequence) {
            Map<String, Object> nestedMap = new LinkedHashMap<>();
            parseAsn1Sequence(aSN1Sequence, nestedMap);
            return nestedMap;
        } else {
            // Fallback: Use ASN.1 dump for unsupported types
            return ASN1Dump.dumpAsString(element);
        }
    }
}