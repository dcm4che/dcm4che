package org.dcm4che.tool.dcm2xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
import org.dcm4che.data.Attributes;
import org.dcm4che.io.ContentHandlerAdapter;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.SAXWriter;

public class Dcm2Xml {

    private static final String USAGE = "dcm2xml [<options>] <dicom-file>";

    private static final String DESCRIPTION = 
        "\nConvert <dicom-file> (or the standard input if <dicom-file> = '-') " +
        "in XML presentation and optionally apply XSLT stylesheet on it. " +
        "Writes result to standard output." +
        "\n-\nOptions:";

    private static final String EXAMPLE = null;
    
    private URL xslt;
    private boolean indent = false;
    private boolean includeKeyword = true;
    private boolean includeBulkData = false;
    private boolean includeBulkDataLocator = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private Attributes blkAttrs;

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

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public final void setBulkDataAttributes(Attributes blkAttrs) {
        this.blkAttrs = blkAttrs;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        opts.addOption(OptionBuilder
                .withLongOpt("xslt")
                .hasArg()
                .withArgName("xsl-file")
                .withDescription("apply specified XSLT stylesheet")
                .create("X"));
        opts.addOption("I", "indent", false,
                "use additional whitespace in XML output");
        opts.addOption("K", "no-keyword", false,
                "do not include keyword attribute of DicomAttribute element " +
                "in XML output");
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder
                .withLongOpt("no-bulkdata")
                .withDescription("do not include bulkdata in XML output; " +
                     "by default, references to bulkdata are included.")
                .create("B"));
        group.addOption(OptionBuilder
                .withLongOpt("with-bulkdata")
                .withDescription("include bulkdata directly in XML output; " +
                    "by default, only references to bulkdata are included.")
                .create("b"));
        opts.addOptionGroup(group);
        opts.addOption(OptionBuilder
                .withLongOpt("blk-file-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription("directory were files with extracted " +
                     "bulkdata are stored if the DICOM object is read from " +
                     "standard input; if not specified, files are stored into " +
                     "the default temporary-file directory.")
                .create("d"));
        opts.addOption(OptionBuilder
                .withLongOpt("blk-file-prefix")
                .hasArg()
                .withArgName("prefix")
                .withDescription("prefix for generating file names for " +
                     "extracted bulkdata; 'blk' by default.")
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("blk-file-suffix")
                .hasArg()
                .withArgName("suffix")
                .withDescription("suffix for generating file names for " +
                     "extracted bulkdata; '.tmp' by default.")
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("blk-spec")
                .hasArg()
                .withArgName("xml-file")
                .withDescription("specify bulkdata attributes explicitly by " +
                     "XML presentation in <xml-file>.")
                .create("x"));
        opts.addOption("h", "help", false, "display this help and exit");
        opts.addOption("V", "version", false,
                "output version information and exit");
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(opts, args);
        if (cl.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        if (cl.hasOption("V")) {
            System.out.println("dcm2xml " + 
                    Dcm2Xml.class.getPackage().getImplementationVersion());
            System.exit(0);
        }
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Xml dcm2xml = new Dcm2Xml();
            if (cl.hasOption("X")) {
                String s = cl.getOptionValue("X");
                dcm2xml.setXSLT(new File(s).toURI().toURL());
            }
            dcm2xml.setIndent(cl.hasOption("I"));
            dcm2xml.setIncludeKeyword(!cl.hasOption("K"));
            if (cl.hasOption("b")) {
                dcm2xml.setIncludeBulkData(true);
                dcm2xml.setIncludeBulkDataLocator(false);
            }
            if (cl.hasOption("B")) {
                dcm2xml.setIncludeBulkData(false);
                dcm2xml.setIncludeBulkDataLocator(false);
            }
            if (cl.hasOption("blk-file-prefix")) {
                dcm2xml.setBulkDataFilePrefix(
                        cl.getOptionValue("blk-file-prefix"));
            }
            if (cl.hasOption("blk-file-suffix")) {
                dcm2xml.setBulkDataFileSuffix(
                        cl.getOptionValue("blk-file-suffix"));
            }
            if (cl.hasOption("d")) {
                File tempDir = new File(cl.getOptionValue("d"));
                dcm2xml.setBulkDataDirectory(tempDir);
            }
            if (cl.hasOption("x")) {
                dcm2xml.setBulkDataAttributes(
                        parseXML(cl.getOptionValue("x")));
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

    private static Attributes parseXML(String fname) throws Exception {
        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new File(fname), ch);
        return attrs;
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
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setBulkDataAttributes(blkAttrs);
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
