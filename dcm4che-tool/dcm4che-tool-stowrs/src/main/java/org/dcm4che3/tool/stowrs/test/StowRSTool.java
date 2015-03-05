package org.dcm4che3.tool.stowrs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StowRSTool implements TestTool{

    private TestResult result;

    private String metadataType;
    
    private File metadataFile;
    
    public static void main(String[] args) throws IOException {
        
        JsonObject object = loadJSON(new File("E:\\compressionData\\work\\compressed-multiframe.json"));
        ArrayList<String> URIs = new ArrayList<String>();
        for (Entry<String, JsonValue> entry : object.entrySet()) {
            if(entry.getValue().getValueType().equals(ValueType.OBJECT)){
                JsonObject entryObject = (JsonObject) entry.getValue();
                if(entryObject.containsKey("BulkDataURI")) {
                    URIs.add(entryObject.getString("BulkDataURI"));
                }
                else if(entryObject.containsKey("DataFragment")) {
                    JsonArray entryArray = (JsonArray) entryObject.get("DataFragment");
                    for(JsonValue value : entryArray) {
                        JsonObject valueObject = (JsonObject) value;
                        if(valueObject.containsKey("BulkDataURI")) {
                            URIs.add(valueObject.getString("BulkDataURI"));
                        }
                    }
                }
            }
        }
        for(String uri : URIs) {
            System.out.println(uri);
        }
    }
    private static boolean childOfPixelData(JsonObject pixelData, JsonObject item) {
        if(pixelData.containsKey("DataFragment")) {
            if(pixelData.getJsonArray("DataFragment").contains(item))
                return true;
        }
        else if(pixelData.containsValue(item)) {
            return true;
        }
        return false;
    }
    private static JsonObject loadJSON(File metadataFile) throws FileNotFoundException {
        Map<String, Object> config = new HashMap<String, Object>();
        //if you need pretty printing
        config.put("javax.json.stream.JsonGenerator.prettyPrinting", Boolean.valueOf(true));
        JsonReaderFactory readerFactory = Json.createReaderFactory(config);
        JsonReader reader = readerFactory.createReader(new FileInputStream(metadataFile));
        JsonObject doc = reader.readObject();
        return doc;
    }
    private static Document loadXml(String fileName) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(fileName));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Error parsing the XML", e);
        } catch (SAXException e) {
            throw new IllegalStateException("Error parsing the XML", e);
        } catch (IOException e) {
            throw new IllegalStateException("Error accessing the XML", e);
        }
    }
    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
     return this.result;
    }

    
}
