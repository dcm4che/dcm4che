/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.tool.wadouri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.wadouri.test.WadoURIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class WadoURI{
    
    private static final Logger LOG = LoggerFactory.getLogger(WadoURI.class);
    
    private static Options options;
    
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.wadouri.messages");
    
    private SAXTransformerFactory saxTransformer;
    
    private Templates xsltTemplates;
    
    private File outDir;
    
    private File xsltFile;
    
    private String outFileName;
    
    private String requestTimeOut;
    
    private boolean xmlIndent = false;
    
    private boolean xmlIncludeKeyword = true;
    
    private String url;
    
    private String objectID;
    
    private String contentType;
    
    private String charset;
    
    private boolean anonymize;
    
    private String annotation;
    
    private int rows=Integer.MAX_VALUE;
    
    private int columns=Integer.MAX_VALUE;
    
    private String regionCoordinates;
    
    private String windowParams;
    
    private int frameNumber=Integer.MAX_VALUE;
    
    private int imageQuality=Integer.MAX_VALUE;
    
    private String presentationStateID;
    
    private String transferSyntax; 
    
    private WadoURIResponse response;
    
    public WadoURI() {}

    public WadoURI(String url, String studyUID, String seriesUID, String objectUID,
            String contentType, String charset, boolean anonymize,
            String annotation, int rows, int columns, String regionCoordinates,
            String windowCenter, String windowWidth, int frameNumber,
            int imageQuality, String presentationSeriesUID,
            String presentationUID, String transferSyntax) {
        setUrl(url);
        setObjectID(studyUID+":"+seriesUID+":"+objectUID);
        setContentType(contentType);
        setCharset(charset);
        setAnonymize(anonymize);
        setAnnotation(annotation);
        setRows(rows);
        setColumns(columns);
        setRegionCoordinates(regionCoordinates);
        setWindowParams(windowCenter+":"+windowWidth);
        setFrameNumber(frameNumber);
        setImageQuality(imageQuality);
        setPresentationStateID(presentationSeriesUID+":"+presentationUID);
        setTransferSyntax(transferSyntax);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        
        options.addOption(null, "request-timeout", true, rb.getString("request-timeout"));

        addWadoOptions(options);
        
        addOutputOptions(options);
        
        CLIUtils.addCommonOptions(options);
        
        return CLIUtils.parseComandLine(args,options, rb, WadoURI.class);
    }
    
    @SuppressWarnings("static-access")
    private static void addWadoOptions(Options opts) {
           
        //add objectID;
            opts.addOption(OptionBuilder.withArgName("studyUID:seriesUID:objectUID")
                    .withLongOpt("object-id").withDescription(rb.getString("object-id"))
                    .hasArg(true).create());
            
        //add contentType;
            opts.addOption(OptionBuilder.withArgName("mediaType")
                    .withLongOpt("content-type").withDescription(rb.getString("content-type"))
                    .hasArg(true).create());
            
        //add charset;
            opts.addOption(OptionBuilder.withArgName("charset")
                    .withLongOpt("charset").withDescription(rb.getString("charset"))
                    .hasArg(true).create());
            
           //add anonymize;
            opts.addOption(OptionBuilder.hasArg(false).withLongOpt("anonymize")
                    .withDescription(rb.getString("anonymize")).create());
            
        //add annotation;
            opts.addOption(OptionBuilder.withArgName("item")
                    .withLongOpt("annotation").withDescription(rb.getString("annotation"))
                    .hasArg(true).create());
            
        //add rows;
            opts.addOption(OptionBuilder.withArgName("numberOfRows")
                    .withLongOpt("rows").withDescription(rb.getString("rows"))
                    .hasArg(true).create());
            
        //add columns;
            opts.addOption(OptionBuilder.withArgName("numberOfColumns")
                    .withLongOpt("columns").withDescription(rb.getString("columns"))
                    .hasArg(true).create());
            
        //add regionCoordinates;
            opts.addOption(OptionBuilder.withArgName("xLeft,yLeft,xRight,yRight")
                    .withLongOpt("region").withDescription(rb.getString("region"))
                    .hasArg(true).create());
            
        //add windowParams;
            opts.addOption(OptionBuilder.withArgName("center:width")
                    .withLongOpt("window-params").withDescription(rb.getString("window-params"))
                    .hasArg(true).create());
            
        //add frameNumber;
            opts.addOption(OptionBuilder.withArgName("number")
                    .withLongOpt("frame-number").withDescription(rb.getString("frame-number"))
                    .hasArg(true).create());
            
        //add imageQuality;
            opts.addOption(OptionBuilder.withArgName("number")
                    .withLongOpt("image-quality").withDescription(rb.getString("image-quality"))
                    .hasArg(true).create());
            
        //add presentationStateID;
            opts.addOption(OptionBuilder.withArgName("SeriesUID:SOPUID")
                    .withLongOpt("presentation-id").withDescription(rb.getString("presentation-id"))
                    .hasArg(true).create());
        //add transferSyntax; 
            opts.addOption(OptionBuilder.withArgName("tsUID")
                    .withLongOpt("transfer-syntax").withDescription(rb.getString("transfer-syntax"))
                    .hasArg(true).create());
    }

    @SuppressWarnings("static-access")
    private static void addOutputOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("out-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("out-dir"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("out-file")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("out-file"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("xsl")
                .hasArg()
                .withArgName("xsl-file")
                .withDescription(rb.getString("xsl"))
                .create("x"));
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption("K", "no-keyword", false, rb.getString("no-keyword"));
    }

    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            WadoURI main = new WadoURI();
            cl = parseComandLine(args);
            
            verifyandSetOptions(cl, main);

            String response = null;
            try {
               response = sendRequest(main);
            } catch (Exception e) {
                System.out.print("Error sending request {}"+e);
            }
            
            System.out.print(response);
        } catch (ParseException e) {
            System.out.println("wadouri: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }


    private static void verifyandSetOptions(CommandLine cl, WadoURI main) {

        if (cl.hasOption("out-dir"))
            main.setOutDir(new File(cl.getOptionValue("out-dir")));
        else
            main.setOutDir(new File("./"));
        
        if(cl.hasOption("out-file"))
            main.setOutFileName(cl.getOptionValue("out-file", cl.getOptionValue("out-file")));
        else
            main.setOutFileName(cl.getOptionValue("out-file", "wadoResponse"));
        
        if (cl.hasOption("x"))
            main.setXsltFile(new File(cl.getOptionValue("x")));
        
        main.setXmlIndent(cl.hasOption("I"));
        
        main.setXmlIncludeKeyword(!cl.hasOption("K"));

        
        if(cl.hasOption("request-timeout"))
        main.setRequestTimeOut(cl.getOptionValue("request-timout"));
        
        if(cl.hasOption("object-id") 
                && cl.getOptionValue("object-id").matches(".+:.+:.+"))
            main.setObjectID(cl.getOptionValue("object-id"));
        else 
            throw new IllegalArgumentException("wadouri: studyUID, SeriesUID and objectUID "
                    + "must be present in the request parameters.");
        
        if(cl.hasOption("content-type"))
            main.setContentType(cl.getOptionValue("content-type"));
        else 
            main.setContentType("application/dicom");
        
        if(cl.hasOption("charset"))
            main.setCharset(Charset.isSupported(cl.getOptionValue("charset"))?cl.getOptionValue("charset")
                    :"utf-8");
        
        main.setAnonymize(cl.hasOption("anonymize"));
        
        if(cl.hasOption("annotation"))
        main.setAnnotation(cl.getOptionValue("annotation"));
        
        if(cl.hasOption("rows"))
            main.setRows(Integer.valueOf(cl.getOptionValue("rows")));
        
        if(cl.hasOption("columns"))
            main.setColumns(Integer.valueOf(cl.getOptionValue("columns")));
        
        if(cl.hasOption("region")) 
            if( cl.getOptionValue("region").matches(".+:.+:.+:.+") )
                main.setRegionCoordinates(cl.getOptionValue("region"));
            else 
                throw new IllegalArgumentException("wadouri: region Option incorrectly specified");
        
        if(cl.hasOption("window-params"))
            if(cl.getOptionValue("window-params").matches("[0-9]+:[0-9]+"))
                main.setWindowParams(cl.getOptionValue("window-params"));
            else
                throw new IllegalArgumentException("wadouri: window params Option incorrectly specified");
        
        if(cl.hasOption("frame-number") 
                && (main.getContentType() != null 
                && !main.getContentType().equalsIgnoreCase("application/dicom")))
            main.setFrameNumber(Integer.valueOf(cl.getOptionValue("frame-number")));
        
        if(cl.hasOption("image-quality")
                && ((main.getContentType() != null 
                && !main.getContentType().equalsIgnoreCase("application/dicom"))
                || (main.getTransferSyntax() != null
                && (main.getTransferSyntax().equalsIgnoreCase("1.2.840.10008.1.2.4.81")
                        || main.getTransferSyntax().equalsIgnoreCase("1.2.840.10008.1.2.4.51")
                        || main.getTransferSyntax().equalsIgnoreCase("1.2.840.10008.1.2.4.52")))))
            main.setImageQuality(Integer.valueOf(cl.getOptionValue("image-quality")));
            
        if(cl.hasOption("presentation-id"))
            if(cl.getOptionValue("presentation-id").matches(".+:.+"))
                main.setPresentationStateID("pre");
            else
                throw new IllegalArgumentException("wadouri: presentation-id Option incorrectly specified");
        
        if(cl.hasOption("transfer-syntax"))
            main.setTransferSyntax(cl.getOptionValue("transfer-syntax"));
        
        if(cl.getArgs().length == 0 || cl.getArgs().length > 1)
            throw new IllegalArgumentException("wadouri: Can not decide URL,"
                    + " too many or too few arguments specified");
        else
            main.setUrl(cl.getArgs()[0]);
        
        if(!main.getOutDir().exists())
            main.getOutDir().mkdirs();
    }

    private static String addParam(String url, String key, String field) {

        if(url.contains("?"))
                return url+="&"+key+"="+field;
        else
            return url+="?"+key+"="+field;
        
    }

    public void wado(WadoURI main) throws Exception {
        URL newUrl = new URL(setWadoRequestQueryParams(main,main.getUrl()));
        System.out.println(newUrl.toString());
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        
        connection.setDoOutput(true);
        
        connection.setDoInput(true);
        
        connection.setInstanceFollowRedirects(true);
        
        connection.setRequestMethod("GET");
        
        if(main.getRequestTimeOut() != null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }
        
        connection.setUseCaches(false);
        int rspCode = connection.getResponseCode();
        String rspMessage = connection.getResponseMessage();
        InputStream in = connection.getInputStream();
        
        String contentType = connection.getHeaderField("Content-Type");
        File f = null;
        if(contentType.contains("application")) {
            if(contentType.contains("application/dicom+xml"))
                f=writeXML(in, main);
            else if(contentType.contains("application/pdf"))
                f=writeFile(in, main, ".pdf");
            else //dicom 
                f=writeFile(in, main, ".dcm");
        }
        else if(contentType.contains("image")) {
            if(contentType.contains("image/jpeg"))
                f=writeFile(in, main, ".jpeg");
            else if(contentType.contains("image/png"))
                f=writeFile(in, main, ".png");
            else //gif
                f=writeFile(in, main, ".gif");
        }
        else if(contentType.contains("text")) {
            if(contentType.contains("text/html")) {
                f=writeFile(in, main, ".html");
            }
            else if(contentType.contains("text/rtf")) {
                f=writeFile(in, main, ".rtf");
            }
            else // text/plain
                f=writeFile(in, main, ".txt");
        }
        this.response = new WadoURIResponse(rspCode, rspMessage, f);
        connection.disconnect();
    }

    private static String sendRequest(final WadoURI main) throws Exception {
        URL newUrl = new URL(setWadoRequestQueryParams(main,main.getUrl()));
        System.out.println(newUrl.toString());
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        
        connection.setDoOutput(true);
        
        connection.setDoInput(true);
        
        connection.setInstanceFollowRedirects(true);
        
        connection.setRequestMethod("GET");
        
        if(main.getRequestTimeOut() != null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }
        
        connection.setUseCaches(false);
        String response = "Server responded with "
                +connection.getResponseCode() + " - " 
                +connection.getResponseMessage();
        InputStream in = connection.getInputStream();
        
        String contentType = connection.getHeaderField("Content-Type");
        
        if(contentType.contains("application")) {
            if(contentType.contains("application/dicom+xml"))
                writeXML(in, main);
            else if(contentType.contains("application/pdf"))
                writeFile(in, main, ".pdf");
            else //dicom 
                writeFile(in, main, ".dcm");
        }
        else if(contentType.contains("image")) {
            if(contentType.contains("image/jpeg"))
                writeFile(in, main, ".jpeg");
            else if(contentType.contains("image/png"))
                writeFile(in, main, ".png");
            else //gif
                writeFile(in, main, ".gif");
        }
        else if(contentType.contains("text")) {
            if(contentType.contains("text/html")) {
                writeFile(in, main, ".html");
            }
            else if(contentType.contains("text/rtf")) {
                writeFile(in, main, ".rtf");
            }
            else // text/plain
                writeFile(in, main, ".txt");
        }
        connection.disconnect();

        return response;

    }
    private static TransformerHandler getTransformerHandler(WadoURI main) throws Exception {
        
        SAXTransformerFactory stf = main.saxTransformer;
        
        if (stf == null)
            main.saxTransformer = stf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        
        if (main.getXsltFile() == null)
            return stf.newTransformerHandler();

        Templates templates = main.xsltTemplates;
        
        if (templates == null){
            templates = stf.newTemplates(new StreamSource(main.xsltFile));
            main.xsltTemplates = templates;
        }
        
        return stf.newTransformerHandler(templates);
    }
    
    private static File writeXML(InputStream in, WadoURI main) throws Exception {
        
        File out = new File(main.getOutDir(), main.getOutFileName()+".xml");
        TransformerHandler th = getTransformerHandler(main);
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                main.xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        Attributes attrs= SAXReader.parse(in);
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(main.xmlIncludeKeyword); 
        saxWriter.write(attrs);
        return out;
    }

    private static File writeFile(InputStream in, WadoURI main, String extension) {
        File file = new File(main.getOutDir(), main.getOutFileName()+extension);
        try {
            Files.copy(in,file
            .toPath(),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("wadouri: Error writing results to file "+ e.getMessage());
        }
        return file;
    }


    private static String setWadoRequestQueryParams(WadoURI main,String url) {
        
        url = addParam(url, "requestType", "WADO");
        String[] objectID = main.getObjectID().split(":");
        url = addParam(url, "studyUID",objectID[0]);
        url = addParam(url, "seriesUID",objectID[1]);
        url = addParam(url, "objectUID",objectID[2]);
        
        if(main.getContentType() != null)
            url = addParam(url, "contentType", main.getContentType());
        
        if(main.getCharset() != null)
            url = addParam(url, "charset", main.getCharset());
        
        if(main.anonymize)
            url = addParam(url, "anonymize", "yes");
        
        if(main.getAnnotation() != null)
            url = addParam(url, "annotation", main.getAnnotation());
        
        if(main.getRows() != Integer.MAX_VALUE)
            url = addParam(url, "rows", Integer.toString(main.getRows()));
        
        if(main.getColumns() != Integer.MAX_VALUE)
            url = addParam(url, "columns", Integer.toString(main.getColumns()));
        
        if(main.getRegionCoordinates() != null)
            url = addParam(url, "region", main.getRegionCoordinates());
        
        if(main.getWindowParams() != null) {
            String[] windowParams = main.getWindowParams().split(":");
            url = addParam(url, "windowCenter", windowParams[0]);
            url = addParam(url, "windowWidth", windowParams[1]);
        }
        
        if(main.getFrameNumber() != Integer.MAX_VALUE)
            url = addParam(url, "frameNumber", Integer.toString(main.getFrameNumber()));
        
        if(main.getImageQuality() != Integer.MAX_VALUE)
            url = addParam(url, "imageQuality", Integer.toString(main.getImageQuality()));
        
        if(main.getPresentationStateID() != null) {
            String[] presentationUID = main.getPresentationStateID().split(":");
            url = addParam(url, "presentationSeriesUID", presentationUID[0]);
            url = addParam(url, "presentationUID", presentationUID[1]);
        }
        
        if(main.getContentType().matches("application/dicom")
                && main.getTransferSyntax() != null)
            url = addParam(url, "transferSyntax", main.getTransferSyntax());
            
        return url;
    }


    public File getOutDir() {
        return outDir;
    }

    public void setOutDir(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public File getXsltFile() {
        return xsltFile;
    }

    public void setXsltFile(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public String getRequestTimeOut() {
        return requestTimeOut;
    }

    public void setRequestTimeOut(String requestTimeOut) {
        this.requestTimeOut = requestTimeOut;
    }

    public boolean isXmlIndent() {
        return xmlIndent;
    }

    public void setXmlIndent(boolean xmlIndent) {
        this.xmlIndent = xmlIndent;
    }

    public boolean isXmlIncludeKeyword() {
        return xmlIncludeKeyword;
    }

    public void setXmlIncludeKeyword(boolean xmlIncludeKeyword) {
        this.xmlIncludeKeyword = xmlIncludeKeyword;
    }

    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getObjectID() {
        return objectID;
    }


    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public String getCharset() {
        return charset;
    }


    public void setCharset(String charset) {
        this.charset = charset;
    }


    public boolean isAnonymize() {
        return anonymize;
    }


    public void setAnonymize(boolean anonymize) {
        this.anonymize = anonymize;
    }


    public String getAnnotation() {
        return annotation;
    }


    public void setAnnotation(String annotation) {
        if(!annotation.contains("NULL"))
        this.annotation = annotation;
    }


    public int getRows() {
        return rows;
    }


    public void setRows(int rows) {
        if(rows > 0)
        this.rows = rows;
    }


    public int getColumns() {
        return columns;
    }


    public void setColumns(int columns) {
        if(columns > 0)
        this.columns = columns;
    }


    public String getRegionCoordinates() {
        return regionCoordinates;
    }


    public void setRegionCoordinates(String regionCoordinates) {
        if(!regionCoordinates.contains("NULL"))
        this.regionCoordinates = regionCoordinates;
    }


    public String getWindowParams() {
        return windowParams;
    }


    public void setWindowParams(String windowParams) {
        if(!windowParams.contains("NULL"))
        this.windowParams = windowParams;
    }


    public int getFrameNumber() {
        return frameNumber;
    }


    public void setFrameNumber(int frameNumber) {
        if(frameNumber > 0)
        this.frameNumber = frameNumber;
    }


    public int getImageQuality() {
        return imageQuality;
    }


    public void setImageQuality(int imageQuality) {
        if(imageQuality > 0)
        this.imageQuality = imageQuality;
    }


    public String getPresentationStateID() {
        return presentationStateID;
    }


    public void setPresentationStateID(String presentationStateID) {
        if(!presentationStateID.contains("NULL"))
        this.presentationStateID = presentationStateID;
    }


    public String getTransferSyntax() {
        return transferSyntax;
    }


    public void setTransferSyntax(String transferSyntax) {
        if(!transferSyntax.contains("NULL"))
        this.transferSyntax = transferSyntax;
    }

    public WadoURIResponse getResponse() {
        return response;
    }

}