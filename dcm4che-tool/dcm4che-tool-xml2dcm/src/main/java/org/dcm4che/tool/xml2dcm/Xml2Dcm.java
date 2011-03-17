package org.dcm4che.tool.xml2dcm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.EncodeOptions;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.io.ContentHandlerAdapter;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;

public class Xml2Dcm {

    private static final String USAGE = "xml2dcm [<options>] " +
        "[-i <dicom-file>] -x <xml-file> [-o <dicom-file>]";

    private static final String DESCRIPTION = 
        "\nConvert <xml-file> (or the standard input if <xml-file> = '-') " +
        "into a DICOM stream written to -o <dicom-file> or standard output " +
        "if no -o option is specified. Optionally load DICOM file specified " +
        "by -i <dicom-file> and merge attributes parsed from <xml-file> " +
        "with it." +
        "\n-\nOptions:";

    private static final String EXAMPLE = null;

    private boolean includeBulkData = false;
    private boolean includeBulkDataLocator = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private String tsuid;
    private boolean withfmi;
    private boolean nofmi;
    private EncodeOptions encOpts = new EncodeOptions();

    private List<File> bulkDataFiles;
    private Attributes fmi;
    private Attributes dataset;

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

    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
    }

    public final void setWithFileMetaInformation(boolean withfmi) {
        this.withfmi = withfmi;
    }

    public final void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setGroupLength(boolean groupLength) {
        encOpts.setGroupLength(groupLength);
    }

    public final void setUndefSequenceLength(boolean undefLength) {
        encOpts.setUndefSequenceLength(undefLength);
    }

    public final void setUndefEmptySequenceLength(boolean undefLength) {
        encOpts.setUndefEmptySequenceLength(undefLength);
    }

    public final void setUndefItemLength(boolean undefLength) {
        encOpts.setUndefItemLength(undefLength);
    }

    public final void setUndefEmptyItemLength(boolean undefLength) {
        encOpts.setUndefEmptyItemLength(undefLength);
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("xml-file")
                .withDescription("XML file to convert to DICOM stream; " +
                     "set <xml-file> = '-' to read XML from standard input.")
                .create("x"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicom-file")
                .withDescription("Load DICOM file to be merged with attributes " +
                     "parsed from <xml-file>; set <dicom-file> = '-' to read " +
                     "DICOM stream from standard input.")
                .create("i"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicom-file")
                .withDescription("Store result into <dicom-file>. By default " +
                     "write DICOM stream to standard output.")
                .create("o"));
        OptionGroup blkGroup = new OptionGroup();
        blkGroup.addOption(OptionBuilder
                .withLongOpt("no-bulkdata")
                .withDescription("do not read bulkdata from -i <dicom-file>")
                .create("B"));
        blkGroup.addOption(OptionBuilder
                .withLongOpt("alloc-bulkdata")
                .withDescription("load bulkdata from -i <dicom-file> into " +
                    "memory. At default, bulkdata from -i <dicom-file> is " +
                    "streamed to -o <dicom-file> and not hold in memory.")
                .create("b"));
        opts.addOptionGroup(blkGroup);
        opts.addOption(OptionBuilder
                .withLongOpt("blk-file-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription("directory were files with extracted " +
                     "bulkdata are stored if the DICOM stream to be merged is " +
                     "read from standard input; if not specified, files are " +
                     "stored into the default temporary-file directory.")
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
                .withLongOpt("keep-blk-files")
                .withDescription("Do not delete extracted bulkdata after it " +
                    "was written into the generated DICOM stream.")
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("transfer-syntax")
                .hasArg()
                .withArgName("uid")
                .withDescription("Store result with specified Transfer " +
                    "Syntax. At default use the Transfer Syntax of the " +
                    "input DICOM file or Explicit VR Little Endian if no " +
                    "input DICOM file was specified for generated DICOM " +
                    "Part 10 files, and Implicit VR Little Endian if no " +
                    "File Meta Information is included in the stored result.")
                .create("t"));
        OptionGroup fmiGroup = new OptionGroup();
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("no-fmi")
                .withDescription("Store result always without File Meta " +
                    "Information. At default, the result is stored with " +
                    "File Meta Information if the XML file or the input " +
                    "DICOM file specified by -i <dicom-file> contains a " +
                    "File Meta Information.")
                .create("F"));
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("fmi")
                .withDescription("Store result always as DICOM Part 10 File " +
                    "with File Meta Information. At default, the result is " +
                    "only stored with File Meta Information if the XML file " +
                    "or the input DICOM file specified by -i <dicom-file> " +
                    "contains a File Meta Information.")
                .create("f"));
        opts.addOptionGroup(fmiGroup);
        opts.addOption("g", "group-len", false, 
                "Include (gggg,0000) Group Length attributes. At default, " +
                "optional Group Length attributes are excluded.");
        OptionGroup sqlenGroup = new OptionGroup();
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-seq-len")
                .withDescription("Encode sequences with explicit length. At " +
                    "default, non-empty sequences are encoded with undefined " +
                    "length.")
                .create("E"));
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-seq-len")
                .withDescription("Encode all sequences with undefined length. " +
                    "At default, only non-empty sequences are encoded with " +
                    "undefined length.")
                .create("U"));
        opts.addOptionGroup(sqlenGroup);
        OptionGroup itemlenGroup = new OptionGroup();
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-item-len")
                .withDescription("Encode sequence items with explicit length. " +
                    "At default, non-empty sequence items are encoded with " +
                    "undefined length.")
                .create("e"));
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-item-len")
                .withDescription("Encode all sequence items with undefined " +
                    "length. At default, only non-empty sequence items are " +
                    "encoded with undefined length.")
                .create("u"));
        opts.addOptionGroup(itemlenGroup);
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
            System.out.println("xml2dcm " + 
                    Xml2Dcm.class.getPackage().getImplementationVersion());
            System.exit(0);
        }
        if (!cl.hasOption("x") && !cl.hasOption("i")) {
            throw new ParseException("Missing required option: x");
        }
        return cl;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Xml2Dcm xml2dcm = new Xml2Dcm();
            if (cl.hasOption("b")) {
                xml2dcm.setIncludeBulkData(true);
                xml2dcm.setIncludeBulkDataLocator(false);
            }
            if (cl.hasOption("B")) {
                xml2dcm.setIncludeBulkData(false);
                xml2dcm.setIncludeBulkDataLocator(false);
            }
            if (cl.hasOption("blk-file-prefix")) {
                xml2dcm.setBulkDataFilePrefix(
                        cl.getOptionValue("blk-file-prefix"));
            }
            if (cl.hasOption("blk-file-suffix")) {
                xml2dcm.setBulkDataFileSuffix(
                        cl.getOptionValue("blk-file-suffix"));
            }
            if (cl.hasOption("d")) {
                File tempDir = new File(cl.getOptionValue("d"));
                xml2dcm.setBulkDataDirectory(tempDir);
            }
            if (cl.hasOption("t")) {
                xml2dcm.setTransferSyntax(cl.getOptionValue("t"));
            }
            xml2dcm.setWithFileMetaInformation(cl.hasOption("f"));
            xml2dcm.setNoFileMetaInformation(cl.hasOption("F"));
            xml2dcm.setGroupLength(cl.hasOption("g"));
            xml2dcm.setUndefItemLength(!cl.hasOption("e"));
            xml2dcm.setUndefSequenceLength(!cl.hasOption("E"));
            xml2dcm.setUndefEmptyItemLength(cl.hasOption("u"));
            xml2dcm.setUndefEmptySequenceLength(cl.hasOption("U"));
            try {
                if (cl.hasOption("i")) {
                    String fname = cl.getOptionValue("i");
                    if (fname.equals("-")) {
                        xml2dcm.parse(new DicomInputStream(System.in));
                    } else {
                        DicomInputStream dis = 
                                new DicomInputStream(new File(fname));
                        try {
                            xml2dcm.parse(dis);
                        } finally {
                            dis.close();
                        }
                    }
                }

                if (cl.hasOption("x"))
                    xml2dcm.parseXML(cl.getOptionValue("x"));

                OutputStream out = cl.hasOption("o") 
                        ? new FileOutputStream(cl.getOptionValue("o"))
                        : new FileOutputStream(FileDescriptor.out);
                try {
                    xml2dcm.writeTo(out);
                } finally {
                    out.close();
                }
            } finally {
                if (!cl.hasOption("keep-blk-files"))
                    xml2dcm.delBulkDataFiles();
            }
        } catch (ParseException e) {
            System.err.println("xml2dcm: " + e.getMessage());
            System.err.println("Try `xml2dcm --help' for more information.");
            System.exit(2);
        } catch (Exception e) {
            System.err.println("xml2dcm: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        if (nofmi)
            fmi = null;
        else if (fmi == null
                ? withfmi
                : tsuid != null && !tsuid.equals(
                        fmi.getString(Tag.TransferSyntaxUID, null, 0, null))) {
            fmi = dataset.createFileMetaInformation(tsuid);
        }
        DicomOutputStream dos = new DicomOutputStream(
                new BufferedOutputStream(out),
                fmi != null
                        ? UID.ExplicitVRLittleEndian
                        : tsuid != null 
                                ? tsuid
                                : UID.ImplicitVRLittleEndian);
        dos.setEncodeOptions(encOpts);
        dos.writeDataset(fmi, dataset);
        dos.flush();
    }

    public void delBulkDataFiles() {
        if (bulkDataFiles != null)
            for (File f : bulkDataFiles)
                f.delete();
    }

    public void parse(DicomInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setIncludeBulkDataLocator(includeBulkDataLocator);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dataset = dis.readDataset(-1, -1);
        fmi = dis.getFileMetaInformation();
        bulkDataFiles = dis.getBulkDataFiles();
    }

    private void parseXML(String fname) throws Exception {
        if (dataset == null)
            dataset = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset);
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        if (fname.equals("-")) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null)
            fmi = fmi2;
    }

}
