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
package org.dcm4che3.tool.wadors;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
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
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.ws.rs.MediaTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class WadoRS{
    
    private static final Logger LOG = LoggerFactory.getLogger(WadoRS.class);
    
    private static byte[] copyHolder;
    
    private static Options options;
    
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.wadors.messages");
    
    private SAXTransformerFactory saxTransformer;
    
    private Templates xsltTemplates;
    
    private File outDir;
    
    private File xsltFile;
    
    private String outFileName;
    
    private String requestTimeOut;
    
    private boolean xmlIndent = false;
    
    private boolean xmlIncludeKeyword = true;
    
    private boolean isMetadata = false;
    
    private String[] acceptTypes;
    
    private String url;
    
    private ResponseWriter writerType;
    
    public WadoRS() {}

    @SuppressWarnings("static-access")
	private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        
        options.addOption(null, "request-timeout", true, rb.getString("request-timeout"));
        
        options.addOption(OptionBuilder
                .withArgName("mediaType;ts")
                .withLongOpt("accept-type")
                .withDescription(rb.getString("accept-type"))
                .hasArg(true)
                .create());
        
        addOutputOptions(options);
        
        CLIUtils.addCommonOptions(options);
        
        return CLIUtils.parseComandLine(args,options, rb, WadoRS.class);
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
            WadoRS main = new WadoRS();
            cl = parseComandLine(args);

            if (cl.hasOption("out-dir"))
                main.setOutDir(new File(cl.getOptionValue("out-dir")));
            
            if(cl.hasOption("out-file"))
                main.setOutFileName(cl.getOptionValue("out-file", cl.getOptionValue("out-file")));
            else
                main.setOutFileName(cl.getOptionValue("out-file", "qidoResponse"));
            
            if (cl.hasOption("x"))
                main.setXsltFile(new File(cl.getOptionValue("x")));
            
            main.setXmlIndent(cl.hasOption("I"));
            
            main.setXmlIncludeKeyword(!cl.hasOption("K"));

            
            if(cl.hasOption("request-timeout"))
            main.setRequestTimeOut(cl.getOptionValue("request-timout"));
            

            if(cl.hasOption("accept-type")) {
            	main.setAcceptType(cl.getOptionValues("accept-type"));
            }
            else {
            	System.out.println("wadors: missing required option accept-type");
                System.err.println(rb.getString("try"));
                System.exit(2);
            }
            main.setUrl(cl.getArgs()[0]);

            if(main.getUrl().contains("metadata"))
            	main.isMetadata = true;
            String response = null;
            try {
               response = sendRequest(main);
            } catch (IOException e) {
                System.out.print("Error sending request {}"+e);
            }
            
            System.out.print(response);
        } catch (ParseException e) {
            LOG.error("wadors: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }


	private static String sendRequest(final WadoRS main) throws IOException {
        URL newUrl = new URL(main.getUrl());
        
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        
        connection.setDoOutput(true);
        
        connection.setDoInput(true);
        
        connection.setInstanceFollowRedirects(false);
        
        connection.setRequestMethod("GET");
        
        connection.setRequestProperty("charset", "utf-8");

        String[] acceptHeaders = compileAcceptHeader(main.acceptTypes);

        for(String acceptStr : acceptHeaders)
        connection.addRequestProperty("Accept", acceptStr);
        
        if(main.getRequestTimeOut()!=null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }
        
        connection.setUseCaches(false);
        
        String response = "Server responded with "
                +connection.getResponseCode() + " - " 
                +connection.getResponseMessage();
        
        InputStream in = connection.getInputStream();
        if(connection.getHeaderField("content-type").contains("application/json")) {
        	       Files.copy(in, new File(main.outDir, main.outFileName +"- metadata [JSON].json").toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
        }
        else {
        try {
        	File spool = new File("Spool");
        	Files.copy(in, spool.toPath(), StandardCopyOption.REPLACE_EXISTING);
        	String boundary;
        	BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(spool)));
        		boundary = (rdr.readLine());    	  
        		boundary = boundary.substring(2,boundary.length());
        		rdr.close();
        		FileInputStream fin = new FileInputStream(spool);
        		  new MultipartParser(boundary).parse(fin, new MultipartParser.Handler() {
                      
                      @Override
                      public void bodyPart(int partNumber, MultipartInputStream in)
                              throws IOException {
                    	  String outFileName = main.outFileName;
                    	  String frame = null;
                          Map<String, List<String>> headerParams = in.readHeaderParams();
                          String mediaType;
                          String contentType = headerParams.get("content-type").get(0);
                          String contentLocation = headerParams.get("content-location")!=null
                        		  ?headerParams.get("content-location").get(0):null;
                          if(contentLocation!=null && contentLocation.contains("frames")) 
                          	frame=contentLocation.split("frames/")[1];
                          
                          if(contentType.contains("transfer-syntax"))
                        	  mediaType = contentType.split(";")[0];
                          else
                        	  mediaType = contentType;

                          //change file suffix
                          if(frame!=null)
                        	  outFileName+= " - Frame ["+frame+"]";
                          else if(main.isMetadata)
                        	  outFileName+= " - metadata ["+mediaType.replace('/', '-')+"]";
                          
                          //choose writer
                          if(main.isMetadata) {
                        		  main.writerType = ResponseWriter.XML;

                          }
                          else {
                          if(mediaType.equalsIgnoreCase("application/dicom")) {
                        	  main.writerType = ResponseWriter.DICOM;
                          }
                          else if(isBulkMediaType(mediaType)) {
                        	  main.writerType = ResponseWriter.BULK;
                          }
                          else {
                        	  throw new IllegalArgumentException("Unknown media type "
                        	  		+ "returned by server, media type = "+mediaType);
                          }

                          }
                          try {
							main.writerType.readBody(mediaType.split("/")[1].contains("+")?
									outFileName+"."+mediaType.split("/")[1].split("\\+")[1]
											:outFileName+"."+mediaType.split("/")[1], main, in);
						} catch (Exception e) {
							System.out.println("Error parsing media type to determine extension"+e);
						}
                      }

					private boolean isBulkMediaType(String mediaType) {
						for(Field field : MediaTypes.class.getFields()) {
							try {
								if(field.getType().equals(String.class)){
								String tmp = (String) field.get(field);
								if(tmp.equalsIgnoreCase(mediaType))
									return true;
								}
							} catch (Exception e) {
								System.out.println("Error deciding media type "+e);
							}
						}
						return false;
					}
                  });
        		  spool.delete();
        } catch (Exception e) {
            System.out.println("Error parsing Server response - "+e);
        }
        }
        connection.disconnect();

        return response;

    }

    private static String[] compileAcceptHeader(String[] acceptTypes) {
    	String[] acceptHeaders = new String[acceptTypes.length];
    	for(int i=0;i<acceptTypes.length;i++) {
    		String acceptType =acceptTypes[i];
    		if(acceptType.contains("application/json"))
    			acceptHeaders[i] = acceptType; 
    		else 
    		acceptHeaders[i] = "multipart/related; type="+acceptType.split(";")[0];
    		
    		if(acceptType.contains(";")) {
    			acceptHeaders[i]+="; transfer-syntax="+acceptType.split(";")[1];
    		}
    	}
    	return acceptHeaders;
	}
    
    private enum ResponseWriter {
        XML {
            @Override
            boolean readBody(String filename, WadoRS wadors, InputStream in) throws Exception {
                    File tmp = new File ("xml-part-spool");
                    TransformerHandler th = getTransformerHandler(wadors);
                    th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                            wadors.xmlIndent ? "yes" : "no");
                    th.setResult(new StreamResult(tmp));
                    //here the sax parser would actually close the input stream immediately
                    InputStream is = cloneStream(in);
                    Attributes attrs= SAXReader.parse(is);
                    SAXWriter saxWriter = new SAXWriter(th);
                    saxWriter.setIncludeKeyword(wadors.xmlIncludeKeyword); 
                    saxWriter.write(attrs);
                    File out = new File(wadors.outDir, "part - "+attrs.getString(Tag.SOPInstanceUID)+" - "+filename);
                    Files.copy(tmp.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    tmp.delete();
                return true;
                
            }
            private InputStream cloneStream(InputStream inputStream) {
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	try{
            	byte[] buf = new byte[4096];

            	// inputStream is your original stream. 
            	for (int n; 0 < (n = inputStream.read(buf));) {
            	    baos.write(buf, 0, n);
            	}
            	baos.close();
            	copyHolder = baos.toByteArray();
            	}
            	catch(Exception e) {
            		System.out.println("wadors : Error copying the stream "+ e);
            	}
            	//This is the new stream that you can pass it to other code and use its data.    
            	return new ByteArrayInputStream(copyHolder);
			}
			private TransformerHandler getTransformerHandler(WadoRS main) throws Exception {
                
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
        },
        DICOM {
            @Override
            boolean readBody(String filename, WadoRS wadors, InputStream in) throws IOException {
                Files.copy(in, new File(wadors.outDir, filename.replace("dicom", "dcm")).toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        },
        BULK {
        	@Override
        	boolean readBody(String filename, WadoRS wadors, InputStream in) throws IOException {
        		File f;
        		
        		if(wadors.getUrl().contains("instances"))
        			f = new File(wadors.outDir," Instance["+wadors.getUrl().split("instances/")[1]+"]");
        		else
        			f =new File(wadors.outDir," Instance[undefined]");
        		
        		if(!f.exists())
        			f.mkdir();
        		
        		Files.copy(in, new File(f,filename).toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
        		return true;
        	}
        };

        abstract boolean readBody(String filename, WadoRS wadors, InputStream in)
                        throws IOException, Exception;

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

	public String[] getAcceptType() {
		return acceptTypes;
	}

	public void setAcceptType(String[] acceptTypes) {
		this.acceptTypes = acceptTypes;
	}

}