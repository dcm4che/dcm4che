package org.dcm4che.tool.dcm2xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.SAXWriter;

public class Dcm2Xml {

    private static final String USAGE = "dcm2xml [<options>] <dicom-file>";

    private static final String DESCRIPTION = 
        "\nConvert <dicom-file> (or the standard input if the filename " +
        "provided is - ) in XML presentation and optionally apply XSLT " +
        "stylesheet on it. Writes result to standard output." +
        "\n.\nOptions:";

    private static final String EXAMPLE = null;
    
    private URL xslt;
    private boolean indent = false;
    private boolean includeKeyword = true;
    private boolean includeBulkData = false;
    private boolean includeBulkDataLocator = true;
    private String tempFilePrefix = "blk";
    private String tempFileSuffix;
    private File tempDirectory;

    public final void setXSLT(URL xslt) {
        this.xslt = xslt;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public final void setIncludeBulkData(boolean includeBulkData) {
        this.includeBulkData = includeBulkData;
    }

    public final void setIncludeBulkDataLocator(boolean includeBulkDataLocator) {
        this.includeBulkDataLocator = includeBulkDataLocator;
    }

    public final void setTempFilePrefix(String tempFilePrefix) {
        this.tempFilePrefix = tempFilePrefix;
    }

    public final void setTempFileSuffix(String tempFileSuffix) {
        this.tempFileSuffix = tempFileSuffix;
    }

    public final void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options options = new Options();
        options.addOption(OptionBuilder
                .withLongOpt("xslt")
                .hasArg()
                .withArgName("xsl-file")
                .withDescription("apply specified XSLT stylesheet")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("indent")
                .withDescription("use additional whitespace in XML output")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("no-keyword")
                .withDescription("do not include keyword attribute of " +
                    "DicomAttribute element in XML output")
                .create());
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("no-bulkdata")
                .withDescription("do not include bulkdata in XML output; " +
                     "by default, references to bulkdata are included.")
                .create());
        group.addOption(OptionBuilder
                .withLongOpt("with-bulkdata")
                .withDescription("include bulkdata directly in XML output; " +
                    "by default, only references to bulkdata are included.")
                .create());
        options.addOptionGroup(group);
        options.addOption(OptionBuilder
                .withLongOpt("tmp-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription("directory were referenced bulkdata is " +
                     "filed if the DICOM object is read from standard input; " +
                     "if not specified, files are stored into the default " +
                     "temporary-file directory.")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("tmp-file-prefix")
                .hasArg()
                .withArgName("prefix")
                .withDescription("prefix for generating temporary-file names; " +
                    "'blk' by default.")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("tmp-file-suffix")
                .hasArg()
                .withArgName("suffix")
                .withDescription("suffix for generating temporary-file names; " +
                    "'.tmp' by default.")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("help")
                .withDescription("display this help and exit")
                .create());
        options.addOption(OptionBuilder
                .withLongOpt("version")
                .withDescription("output version information and exit")
                .create());
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, options, EXAMPLE);
            return null;
        }
        if (cl.hasOption("version")) {
            System.out.println("dcm2xml " + 
                    Dcm2Xml.class.getPackage().getImplementationVersion());
            return null;
        }
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            if (cl != null) {
                Dcm2Xml dcm2xml = new Dcm2Xml();
                if (cl.hasOption("xslt")) {
                    String s = cl.getOptionValue("xslt");
                    dcm2xml.setXSLT(new File(s).toURI().toURL());
                }
                dcm2xml.setIndent(cl.hasOption("indent"));
                dcm2xml.setIncludeKeyword(!cl.hasOption("no-keyword"));
                if (cl.hasOption("with-bulkdata")) {
                    dcm2xml.setIncludeBulkData(true);
                    dcm2xml.setIncludeBulkDataLocator(false);
                }
                if (cl.hasOption("no-bulkdata")) {
                    dcm2xml.setIncludeBulkData(false);
                    dcm2xml.setIncludeBulkDataLocator(false);
                }
                if (cl.hasOption("tmp-file-prefix")) {
                    dcm2xml.setTempFilePrefix(
                            cl.getOptionValue("tmp-file-prefix"));
                }
                if (cl.hasOption("tmp-file-suffix")) {
                    dcm2xml.setTempFileSuffix(
                            cl.getOptionValue("tmp-file-suffix"));
                }
                if (cl.hasOption("tmp-dir")) {
                    File tempDir = new File(cl.getOptionValue("tmp-dir"));
                    dcm2xml.setTempDirectory(tempDir);
                }
                String fname = fname(cl.getArgList());
                if (fname.equals("-")) {
                    dcm2xml.parse(new DicomInputStream(System.in));
                } else {
                    DicomInputStream dis =
                            new DicomInputStream(new File(fname));
                    try {
                        dcm2xml.parse(dis);
                    } finally {
                        dis.close();
                    }
                }
            }
        } catch (ParseException e) {
            System.err.println("dcm2xml: " + e.getMessage());
            System.err.println("Try `dcm2xml --help' for more information.");
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2xml: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException("Missing file operand");
        if (numArgs > 1)
            throw new ParseException("Too many arguments");
        return argList.get(0);
    }

    public void parse(DicomInputStream dis) throws IOException,
            TransformerConfigurationException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setIncludeBulkDataLocator(includeBulkDataLocator);
        dis.setTempDirectory(tempDirectory);
        dis.setTempFilePrefix(tempFilePrefix);
        dis.setTempFileSuffix(tempFileSuffix);
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, 
                indent ? "yes" : "no");
        th.setResult(new StreamResult(System.out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(includeKeyword);
        dis.setDicomInputHandler(saxWriter);
        dis.readDataset(-1, -1);
    }

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory)
                TransformerFactory.newInstance();
        if (xslt == null)
            return tf.newTransformerHandler();

        TransformerHandler th = tf.newTransformerHandler(
                new StreamSource(xslt.openStream(), xslt.toExternalForm()));
        return th;
    }
}
