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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.xml.parsers.ParserConfigurationException;
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
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONReader.Callback;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * QIDO-RS client
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class QidoRS {
    
    private static final Logger LOG = LoggerFactory.getLogger(QidoRS.class);
    
    private static Options options;
    
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.qidors.messages");
    
    private SAXTransformerFactory saxTransformer;
    
    private Templates xsltTemplates;
    
    private final HashMap<String, String> query =new HashMap<String, String>();
    
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
    
    private Attributes queryAttrs;

    private Attributes returnAttrs;

    private boolean returnAll;
    
    private boolean runningModeTest;

    private final List<Attributes> responseAttrs = new ArrayList<Attributes>();

    private long timeFirst;

    private int numMatches = 0;

    public QidoRS() {}

    public QidoRS(boolean fuzzy, boolean timezone, boolean returnAll,
            String limit, String offset, Attributes queryAttrs, Attributes returnAttrs, String mediaType, String url) {
        this.returnAttrs = returnAttrs;
        this.url = url;
        this.queryAttrs = queryAttrs;
        this.isJSON = mediaType.equalsIgnoreCase("JSON");
        this.fuzzy = fuzzy;
        this.timezone = timezone;
        this.returnAll = returnAll;
        this.limit = limit;
        this.offset = offset;
    }
    
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
               response = qido(main,true);
            } catch (IOException e) {
                System.out.print("Error during request {}"+e);
            }
            
            System.out.print(response);
        } catch (Exception e) {
            LOG.error("qidors: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    public static String qido(QidoRS main, boolean cli) throws IOException {
        URL newUrl;
        if (cli)
            newUrl = new URL(addRequestParametersCLI(main, main.getUrl()));
        else
            newUrl = new URL(addRequestParameters(main, main.getUrl()));
        return sendRequest(newUrl, main);
    }

    private static String sendRequest(URL url, final QidoRS main) throws IOException {
        
        LOG.info("URL: {}", url);

        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        
        connection.setDoOutput(true);
        
        connection.setDoInput(true);
        
        connection.setInstanceFollowRedirects(false);
        
        connection.setRequestMethod("GET");
        
        connection.setRequestProperty("charset", "utf-8");
        
        if(main.isJSON) {
            connection.setRequestProperty("Accept", "application/json");
        }
        
        if(main.getRequestTimeOut()!=null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }
        
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

    private static String addRequestParametersCLI(final QidoRS main, String url) throws UnsupportedEncodingException {
        
        if(main.includeField!=null) {
            for(String field : main.getIncludeField())
            url=addParam(url,"includefield",field);
        }
        else {
            url=addParam(url,"includefield","all");
        }
        
        
        if(main.getQuery() != null) {
            for(String queryKey : main.getQuery().keySet())
                url=addParam(url,queryKey,main.getQuery().get(queryKey));
        }
        
        if(main.isFuzzy())
            url=addParam(url,"fuzzymatching","true");
        
        if(main.isTimezone())
            url=addParam(url,"timezoneadjustment","true");
        
        if(main.getLimit()!=null) {
            url=addParam(url,"limit",main.getLimit());
        }
        
        if(main.getOffset() != null) {
            url=addParam(url,"offset",main.getOffset());
        }
        return url;
    }

    private static String addRequestParameters(final QidoRS main, String url) throws UnsupportedEncodingException {
        
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();

        if (!main.returnAll) {
            if (main.returnAttrs != null) {
                for (int tag : main.returnAttrs.tags()) {
                    if (!TagUtils.isPrivateCreator(tag)) {
                        url = addParam(url, "includefield", ElementDictionary.keywordOf(tag, main.returnAttrs.getPrivateCreator(tag)));
                    }
                }
            }
        } else {
            url = addParam(url, "includefield", "all");
        }

        if(main.getQueryAttrs() != null) {
            for(int i=0; i< main.queryAttrs.tags().length;i++) {
                int tag = main.queryAttrs.tags()[i];
                String keyword ;
                keyword = keyWordOf(main, dict, tag, main.queryAttrs);
                if(main.queryAttrs.getSequence(tag) != null) {
                    //is a sequence
                    setSequenceQueryAttrs(main,url,main.queryAttrs.getSequence(tag), keyword);
                }
                else {
                        url=addParam(url,
                               keyword, (String) main.queryAttrs.getValue(tag));
                }
                
            }
        }
        
        if(main.isFuzzy())
            url=addParam(url,"fuzzymatching","true");
        
        if(main.isTimezone())
            url=addParam(url,"timezoneadjustment","true");
        
        if(main.getLimit()!=null) {
            url=addParam(url,"limit",main.getLimit());
        }
        
        if(main.getOffset() != null) {
            url=addParam(url,"offset",main.getOffset());
        }
        if(main.isJSON)
            main.parserType = ParserType.JSON;
        else
            main.parserType = ParserType.XML;
        return url;
    }

    protected static String keyWordOf(final QidoRS main,
            ElementDictionary dict, int tag, Attributes attrs) {
        String keyword;
        if(attrs.getPrivateCreator(tag) != null)  {
            keyword = ElementDictionary.keywordOf(tag, attrs.getPrivateCreator(tag));
        }
        else {
            keyword = dict.keywordOf(tag);
        }
        return keyword;
    }

    private static void setSequenceQueryAttrs(QidoRS main, String url, Sequence sequence, String seqKeyWork) {
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
        for(Attributes item : sequence) {
            for(int i=0; i < item.tags().length; i++) {
                int tag = item.tags()[i];
                if(item.getSequence(tag) == null) {
                    url+=(url.endsWith(".")?"":(url.contains("?")?"&":"?"))
                            +keyWordOf(main, dict, tag, main.queryAttrs)
                            +"="+(String) main.queryAttrs.getValue(tag);
                }
                else {
                    url+=(url.endsWith(".")?"":(url.contains("?")?"&":"?"))
                            +keyWordOf(main, dict, tag, main.queryAttrs)+".";
                    setSequenceQueryAttrs(main,url,main.queryAttrs.getSequence(tag)
                            , keyWordOf(main, dict, tag, main.queryAttrs));
                }
            }
            
        }
    }

    private static String addParam(String url, String key, String field) throws UnsupportedEncodingException {
        if (url.contains("?"))
            return url += "&" + key + "=" + URLEncoder.encode(field, "UTF-8");
        else
            return url += "?" + key + "=" + URLEncoder.encode(field, "UTF-8");
    }

    private enum ParserType {
        XML {
            @Override
            boolean readBody(QidoRS qidors, InputStream in) throws Exception {
                
                String full="";
                String str;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String boundary = reader.readLine();
                while((str = reader.readLine())!=null) {
                    full+=str;
                }
                
                String[] parts = full.split(boundary);
                
                for(int i=0;i<parts.length-1;i++) {
                    if(qidors.isRunningModeTest()) {
                        if(qidors.getTimeFirst() == 0)
                            qidors.setTimeFirst(System.currentTimeMillis());
                        qidors.responseAttrs.add(SAXReader.parse(new ByteArrayInputStream(removeHeader(parts[i]).getBytes("UTF-8"))));
                        qidors.numMatches++;
                    }
                    else {
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
            boolean readBody(final QidoRS qidors, InputStream in) 
                    throws IOException, ParserConfigurationException, SAXException {
                if(qidors.isRunningModeTest()) {
                    try {
                        JSONReader reader = new JSONReader(
                                Json.createParser(new InputStreamReader(in, "UTF-8")));
                        reader.readDatasets(new Callback() {
                            @Override
                            public void onDataset(Attributes fmi, Attributes dataset) {
                                if(qidors.getTimeFirst() == 0)
                                    qidors.setTimeFirst(System.currentTimeMillis());
                                qidors.responseAttrs.add(dataset);
                                qidors.numMatches++;
                            }
                        });

                    } finally {
                        if (in != System.in)
                            SafeClose.close(in);
                    }
                    
                }
                else {
                Files.copy(in, new File(qidors.outDir, qidors.outFileName).toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
                }
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

    public boolean isRunningModeTest() {
        return runningModeTest;
    }

    public void setRunningModeTest(boolean runningModeTest) {
        this.runningModeTest = runningModeTest;
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

    public Attributes getQueryAttrs() {
        return queryAttrs;
    }

    public void setQueryAttrs(Attributes queryAttrs) {
        this.queryAttrs = queryAttrs;
    }

    public long getTimeFirst() {
        return timeFirst;
    }

    public void setTimeFirst(long timeFirst) {
        this.timeFirst = timeFirst;
    }

    public int getNumMatches() {
        return numMatches;
    }

    public List<Attributes> getResponseAttrs() {
        return responseAttrs;
    }

    public Attributes getReturnAttrs() {
        return returnAttrs;
    }


}