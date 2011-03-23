package org.dcm4che.tool.dcmdir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.media.DicomDirReader;

public class DcmDir {

    private static final String USAGE =
        "dcmdir -{acdptz} <dicomdir> [Options] [<file>..][<directory>..]";

    private static final String DESCRIPTION = 
        "\nDump/Create/Update/Compact DICOM directory file.\n-\nOptions:";

    private static final String EXAMPLE = null;

    /** default number of characters per line */
    private static final int DEFAULT_WIDTH = 78;

    private int width = DEFAULT_WIDTH;
    private StringBuilder sb = new StringBuilder();

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmDir dcmdir = new DcmDir();
            if (cl.hasOption("w")) {
                String s = cl.getOptionValue("w");
                try {
                    dcmdir.setWidth(Integer.parseInt(s));
                } catch (IllegalArgumentException e) {
                    throw new ParseException(
                            "Illegal line length: " + s);
                }
            }
            if (cl.hasOption("t")) {
                dcmdir.dump(new File(cl.getOptionValue("t")));
            }
        } catch (ParseException e) {
            System.err.println("dcmdir: " + e.getMessage());
            System.err.println("Try `dcmdir --help' for more information.");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("dcmdir: " + e.getMessage());
            System.exit(2);
        }
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        opts.addOption(OptionBuilder
                .withLongOpt("dicomdir")
                .hasArg()
                .withArgName("col")
                .withDescription("read directory file <dicomdir> and dump content to stdout")
                .create("t"));
        opts.addOption(OptionBuilder
                .withLongOpt("width")
                .hasArg()
                .withArgName("col")
                .withDescription("set line length; default: 78")
                .create("w"));
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

    public void dump(File file) throws IOException {
        DicomDirReader r = new DicomDirReader(file);
        try {
            System.out.println(r.getFileMetaInformation()
                    .toString(Integer.MAX_VALUE, width));
            System.out.println(r.getFileSetInformation()
                    .toString(Integer.MAX_VALUE, width));
            dump(r, r.readFirstRootDirectoryRecord(), new int[0]);
        } finally {
            r.close();
        }
    }

    private void dump(DicomDirReader r, Attributes rec, int[] prefix)
    throws IOException {
        if (rec == null)
            return;

        int[] index = Arrays.copyOf(prefix, prefix.length + 1);
        do {
            index[prefix.length]++;
            System.out.println(toTitle(index,
                    rec.getString(Tag.DirectoryRecordType, null, 0, null)));
            System.out.println(rec.toString(Integer.MAX_VALUE, width));
            dump(r, r.readLowerDirectoryRecord(rec), index);
            rec = r.readNextDirectoryRecord(rec);
        } while (rec != null);
    }

    private String toTitle(int[] index, String s) {
        sb.setLength(0);
        for (int i : index)
            sb.append(i).append('.');
        sb.setCharAt(sb.length() - 1, ' ');
        sb.append(s);
        return sb.toString();
    }

}
