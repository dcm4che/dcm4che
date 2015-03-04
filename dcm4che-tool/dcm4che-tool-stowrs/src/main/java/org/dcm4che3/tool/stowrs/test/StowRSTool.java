package org.dcm4che3.tool.stowrs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
        Document doc = loadXml("E:\\compressionData\\work\\compressed-multiframe.xml");
        NodeList list = doc.getElementsByTagName("BulkData");
        System.out.println("test has multiple fragments : " + list.getLength());
        for(int i = 0 ; i<list.getLength();i++) {
            System.out.println(list.item(i).getAttributes().item(0).getNodeValue().replaceAll(".*\\?", "").replaceAll(".*\\&", ""));
        }
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
