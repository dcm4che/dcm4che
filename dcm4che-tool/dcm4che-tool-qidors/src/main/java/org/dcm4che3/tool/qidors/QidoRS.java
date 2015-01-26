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
package org.dcm4che3.tool.qidors;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Properties;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class QidoRS{
    
    private static final Logger LOG = LoggerFactory.getLogger(QidoRS.class);
    
    private static Options options;
    
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.qidors.messages");
    
    private SAXTransformerFactory saxTransformer;
    
    private Templates xsltTemplates;
    
    private HashMap<String, String> query =new HashMap<String, String>();
    
    private String[] includeField;
    
    private File outDir;
    
    private File xsltFile;
    
    private String outFileName;
    
    private String requestTimeOut;
    
    private boolean xmlIndent = false;
    
    private boolean xmlIncludeKeyword = true;
    
    private boolean isJSON;
    
    private boolean fuzzy=false;
    
    private boolean timezone=false;
    
    private String limit;
    
    private String offset="0";
    
    private String url;
    
    private ParserType parserType;
    
    public QidoRS() {}

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        
        options.addOption(OptionBuilder.hasArgs(2).withArgName("[seq.]attr=value")
                .withValueSeparator().withDescription(rb.getString("match"))
                .create("m"));
        
        options.addOption("i", "includefield", true, rb.getString("includefield"));
        
        options.addOption("J", "json", true, rb.getString("json"));
        
        options.addOption(null, "fuzzy", false, rb.getString("fuzzy"));
        
        options.addOption(null, "timezone", false, rb.getString("timezone"));
        
        options.addOption(null, "limit", true, rb.getString("limit"));
        
        options.addOption(null, "offset", true, rb.getString("offset"));
        
        options.addOption(null, "request-timeout", true, rb.getString("request-timeout"));
        
        addOutputOptions(options);
        
        CLIUtils.addCommonOptions(options);
        
        return CLIUtils.parseComandLine(args,options, rb, QidoRS.class);
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
        opts.addOption("J", "json", false, rb.getString("json"));
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
            QidoRS main = new QidoRS();
            cl = parseComandLine(args);
            main.setQuery(cl.getOptionProperties("m"));
            if (cl.hasOption("out-dir"))
                main.setOutDir(new File(cl.getOptionValue("out-dir")));
            
            if(cl.hasOption("out-file"))
                main.setOutFileName(cl.getOptionValue("out-file", cl.getOptionValue("out-file")));
            else
                main.setOutFileName(cl.getOptionValue("out-file", "qidoResponse"));
            
            if (cl.hasOption("x"))
                main.setXsltFile(new File(cl.getOptionValue("x")));
            
            if(cl.hasOption("J")) {
                main.setJSON(true);
                main.parserType = ParserType.JSON;
                main.setOutFileName(main.getOutFileName()+".json");
            }
            else{
                main.setJSON(false);
                main.parserType = ParserType.XML;
                main.setOutFileName(main.getOutFileName()+".xml");
            }
            
            if(cl.hasOption("fuzzy")) 
                main.setFuzzy(true);
            
            if(cl.hasOption("timezone")) 
                main.setTimezone(true);
            
            if(cl.hasOption("limit")) 
                main.setLimit(cl.getOptionValue("limit"));
            
            if(cl.hasOption("offset")) 
                main.setOffset(cl.getOptionValue("offset"));
            
            main.setXmlIndent(cl.hasOption("I"));
            
            main.setXmlIncludeKeyword(!cl.hasOption("K"));
            
            main.setIncludeField(cl.getOptionValues("includefield"));
            
            if(cl.hasOption("request-timeout"))
            main.setRequestTimeOut(cl.getOptionValue("request-timout"));
            
            main.setUrl(cl.getArgs()[0]);
            
            String response = null;
            try {
               response = sendRequest(main);
            } catch (IOException e) {
                System.out.print("Error during request {}"+e);
            }
            
            System.out.print(response);
        } catch (ParseException e) {
            LOG.error("qidors: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }


    private static String sendRequest(final QidoRS main) throws IOException {
        URL newUrl = new URL(main.getUrl());
        
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        
        connection.setDoOutput(true);
        
        connection.setDoInput(true);
        
        connection.setInstanceFollowRedirects(false);
        
        connection.setRequestMethod("GET");
        
        connection.setRequestProperty("charset", "utf-8");
        
        addRequestParameters(main, connection);
        
        connection.setUseCaches(false);
        
        String response = "Server responded with "
                +connection.getResponseCode() + " - " 
                +connection.getResponseMessage();
        
        InputStream in = connection.getInputStream();
        try {
            main.parserType.readBody(main, in);
        } catch (Exception e) {
            System.out.println("Error parsing Server response - "+e);
        }
        connection.disconnect();

        return response;

    }

    private static void addRequestParameters(final QidoRS main,
            HttpURLConnection connection) {
        
        if(main.includeField!=null) {
            for(String field : main.getIncludeField())
            connection.setRequestProperty("includefield", field);
        }
        else {
            connection.setRequestProperty("includefield", "all");
        }
        
        if(main.isJSON) {
            connection.setRequestProperty("Accept", "application/json");
        }
        
        if(main.getRequestTimeOut()!=null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }
        
        if(main.getQuery() != null) {
            for(String queryKey : main.getQuery().keySet())
                connection.setRequestProperty(queryKey,main.getQuery().get(queryKey));
        }
        
        if(main.isFuzzy())
            connection.setRequestProperty("fuzzymatching", "true");
        
        if(main.isTimezone())
            connection.setRequestProperty("timezoneadjustment", "true");
        
        if(main.getLimit()!=null) {
            connection.setRequestProperty("limit", main.getLimit());
        }
        
        if(main.getOffset() != null) {
            connection.setRequestProperty("offset", main.getOffset());
        }
        
    }
    private enum ParserType {
        XML {
            @Override
            boolean readBody(QidoRS qidors, InputStream in) throws Exception {
                
                String full="";
                String str;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String boundary = reader.readLine();
                while((str = reader.readLine())!=null) {
                    full+=str;
                }
                
                String[] parts = full.split(boundary);
                
                for(int i=0;i<parts.length-1;i++) {
                    File out = new File(qidors.outDir, "part - "+(i+1)+" - "+qidors.outFileName);
                    TransformerHandler th = getTransformerHandler(qidors);
                    th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                            qidors.xmlIndent ? "yes" : "no");
                    th.setResult(new StreamResult(out));
                    Attributes attrs= SAXReader.parse(new ByteArrayInputStream(removeHeader(parts[i]).getBytes()));
                    SAXWriter saxWriter = new SAXWriter(th);
                    saxWriter.setIncludeKeyword(qidors.xmlIncludeKeyword); 
                    saxWriter.write(attrs);
                }
                
                reader.close();
                return true;
                
            }
            private TransformerHandler getTransformerHandler(QidoRS main) throws Exception {
                
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

            private String removeHeader(String str) {
                String buff="";
                
                for(int i=0;i<str.length();i++)
                if(str.charAt(i) == '<') {
                    if(str.charAt(i+1)=='?') {
                        buff+=str.substring(i,str.length());
                        break;
                    }
                }
                return buff;
            }
        },
        JSON {
            @Override
            boolean readBody(QidoRS qidors, InputStream in) throws IOException {
                Files.copy(in, new File(qidors.outDir, qidors.outFileName).toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        };

        abstract boolean readBody(QidoRS qidors, InputStream in)
                        throws IOException, Exception;

    }

    public String[] getIncludeField() {
        return includeField;
    }

    public void setIncludeField(String[] includeField) {
        this.includeField = includeField;
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

    public boolean isJSON() {
        return isJSON;
    }

    public void setJSON(boolean isJSON) {
        this.isJSON = isJSON;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public boolean isTimezone() {
        return timezone;
    }

    public void setTimezone(boolean timezone) {
        this.timezone = timezone;
    }

    public HashMap<String, String> getQuery() {
        return query;
    }

    private void setQuery(Properties optionProperties) {
        for(Object key : optionProperties.keySet())
        this.query.put((String) key, (String) optionProperties.get(key));
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

}