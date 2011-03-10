package org.dcm4che.tool.dcm2xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private boolean indent;

    public final void setXSLT(URL xslt) {
        this.xslt = xslt;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
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
                InputStream is = inputStream(cl.getArgList());
                try {
                    dcm2xml.parse(is);
                } finally {
                    if (is != System.in)
                        is.close();
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

    private static InputStream inputStream(List<String> argList)
            throws FileNotFoundException, ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException("Missing file operand");
        if (numArgs > 1)
            throw new ParseException("Too many arguments");
        String fname = argList.get(0);
        return fname.equals("-")
                ? System.in
                : new FileInputStream(argList.get(0));
    }

    public void parse(InputStream is) throws IOException,
            TransformerConfigurationException {
        DicomInputStream dis = new DicomInputStream(is);
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, 
                indent ? "yes" : "no");
        th.setResult(new StreamResult(System.out));
        dis.setDicomInputHandler(new SAXWriter(th));
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
