package org.dcm4che.tool.dcmdir;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.media.DicomDirWriter;
import org.dcm4che.util.UIDUtils;

public class DcmDir {

    private static final String USAGE =
        "dcmdir -{acdlpz} <dicomdir> [Options] [<file>..][<directory>..]";

    private static final String DESCRIPTION = 
        "\nList/Create/Update/Compact DICOM directory file.\n-\nOptions:";

    private static final String EXAMPLE = null;

    /** default number of characters per line */
    private static final int DEFAULT_WIDTH = 78;

    private boolean inUse;
    private String uid;
    private String id;
    private File descFile;
    private String descFileCharset;
    private int width = DEFAULT_WIDTH;

    private DicomDirReader in;
    private DicomDirWriter out;

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        OptionGroup cmdGroup = new OptionGroup();
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("read directory file <dicomdir> and list " +
                    "content into standard out")
                .create("l"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("create new directory file <dicomdir> for " +
                     "DICOM File-set specified by file.. or directory.. " +
                     "arguments")
                .create("c"));
        opts.addOptionGroup(cmdGroup);
        opts.addOption(OptionBuilder
                .withLongOpt("desc")
                .hasArg()
                .withArgName("txtfile")
                .withDescription("specify File-set Descriptor File")
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("desc-charset")
                .hasArg()
                .withArgName("code")
                .withDescription("Character Set used in File-set Descriptor " +
                     "File (\"ISO_IR 100\" = ISO Latin 1).")
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-id")
                .hasArg()
                .withArgName("id")
                .withDescription("specify File-set ID")
                .create("i"));
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-uid")
                .hasArg()
                .withArgName("uid")
                .withDescription("specify File-set UID")
                .create("u"));
        opts.addOption(OptionBuilder
                .withLongOpt("width")
                .hasArg()
                .withArgName("col")
                .withDescription("set line length; default: 78")
                .create("w"));
        opts.addOption(null, "in-use", false, "only list directory records " +
                "with Record In-use Flag != 0");
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
            System.out.println("dcmdir " + 
                    DcmDir.class.getPackage().getImplementationVersion());
            System.exit(0);
        }
        return cl;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmDir dcmdir = new DcmDir();
            dcmdir.setInUse(cl.hasOption("in-use"));
            if (cl.hasOption("u"))
                dcmdir.setFilesetUID(cl.getOptionValue("u"));
            if (cl.hasOption("i"))
                dcmdir.setFilesetID(cl.getOptionValue("i"));
            if (cl.hasOption("desc"))
                dcmdir.setDescriptorFile(new File(cl.getOptionValue("desc")));
            if (cl.hasOption("desc-charset"))
                dcmdir.setDescriptorFileCharset(
                        cl.getOptionValue("desc-charset"));
            if (cl.hasOption("w")) {
                String s = cl.getOptionValue("w");
                try {
                    dcmdir.setWidth(Integer.parseInt(s));
                } catch (IllegalArgumentException e) {
                    throw new ParseException(
                            "Illegal line length: " + s);
                }
            }
            try {
                if (cl.hasOption("l")) {
                    dcmdir.openForReadOnly(new File(cl.getOptionValue("l")));
                    dcmdir.list();
                } else if (cl.hasOption("c")) {
                    dcmdir.create(new File(cl.getOptionValue("c")));
                } 
            } finally {
                dcmdir.close();
            }
        } catch (ParseException e) {
            System.err.println("dcmdir: " + e.getMessage());
            System.err.println("Try `dcmdir --help' for more information.");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("dcmdir: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public final void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public final void setFilesetUID(String uid) {
        this.uid = uid;
    }

    public final void setFilesetID(String id) {
        this.id = id;
    }

    public final void setDescriptorFile(File descFile) {
        this.descFile = descFile;
    }

    public final void setDescriptorFileCharset(String descFileCharset) {
        this.descFileCharset = descFileCharset;
    }

    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    public void close() {
        if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
            in = null;
            out = null;
        } 
    }

    public void openForReadOnly(File file) throws IOException {
        in = new DicomDirReader(file);
    }

    public void create(File file) throws IOException {
        in = out = DicomDirWriter
                .create(file, uid(), id, descFile, descFileCharset);
    }

    private String uid() {
        return uid == null ? UIDUtils.createUID() : uid;
    }

    public void list() throws IOException {
        list("File Meta Information:", in.getFileMetaInformation());
        list("File-set Information:", in.getFileSetInformation());
        list(inUse
                ? in.findFirstRootDirectoryRecordInUse()
                : in.readFirstRootDirectoryRecord(),
             new StringBuilder());
    }

    private void list(final String header, final Attributes attrs) {
        System.out.println(header);
        System.out.println(attrs.toString(Integer.MAX_VALUE, width));
    }

    private void list(Attributes rec, StringBuilder index)
            throws IOException {
        if (rec == null)
            return;

        int indexLen = index.length();
        int i = 1;
        do {
            index.append(i++).append('.');
            list(heading(rec, index), rec);
            list(inUse
                    ? in.findLowerDirectoryRecordInUse(rec)
                    : in.readLowerDirectoryRecord(rec),
                    index);
            rec = inUse
                    ? in.findNextDirectoryRecordInUse(rec)
                    : in.readNextDirectoryRecord(rec);
            index.setLength(indexLen);
        } while (rec != null);
    }

    private String heading(Attributes rec, StringBuilder index) {
        int prefixLen = index.length();
        try {
            return index.append(' ')
                .append(rec.getString(Tag.DirectoryRecordType, ""))
                .append(':').toString();
        } finally {
            index.setLength(prefixLen);
        }
    }
}
