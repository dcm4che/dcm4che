package org.dcm4che.tool.dcm2txt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputHandler;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.util.TagUtils;

public class Dcm2Txt implements DicomInputHandler {

    private static final String USAGE = "dcm2txt [options] [infile]";

    private static final String DESCRIPTION = 
        "\nWrites a text representation of DICOM infile to standard output. " +
        "With no infile read standard input.\n.\nOptions:";

    private static final String EXAMPLE = null;

    /** default number of characters per line */
    public static final int DEFAULT_WIDTH = 79;

    private int width = DEFAULT_WIDTH;
    private boolean first = true;

    public void parse(InputStream is) throws IOException {
        DicomInputStream dis = new DicomInputStream(is);
        dis.setDicomInputHandler(this);
        dis.readAttributes(-1);
    }

    @Override
    public boolean readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        if (first) {
            promptPreamble(dis.getPreamble());
            first = false;
        }
        int tag = dis.tag();
        StringBuilder line = new StringBuilder(width);
        appendHeader(line, dis);
        boolean appendValue = appendValue(line, dis, attrs);
        appendKeyword(line, tag, attrs.getPrivateCreator(tag));
        System.out.println(line);
        return appendValue || dis.readValue(dis, attrs);
    }

    private boolean appendValue(StringBuilder line, DicomInputStream dis,
            Attributes attrs) throws IOException {
        VR vr = dis.vr();
        int length = dis.length();
        if (vr == null || vr == VR.SQ || length == -1)
            return false;

        int tag = dis.tag();
        dis.readValue(dis, attrs);
        append(line, attrs.getStrings(tag, null));
        switch (tag) {
        case Tag.FileMetaInformationGroupLength:
        case Tag.TransferSyntaxUID:
        case Tag.SpecificCharacterSet:
            break;
        default:
            if (!TagUtils.isPrivateCreator(tag))
                attrs.remove(tag, null);
        }
        return true;
    }

    private void append(StringBuilder line, String[] values) {
        line.append(" [");
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                line.append('\\');
            line.append(values[i]);
            if (line.length() + 1 > width) {
                line.setLength(width - 3);
                line.append("..");
                break;
            }
        }
        line.append(']');
    }

    @Override
    public boolean readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        StringBuilder line = new StringBuilder(width);
        appendHeader(line, dis);
        appendKeyword(line, dis.tag(), null);
        System.out.println(line);
        return dis.readValue(dis, seq);
    }

    @Override
    public boolean readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        StringBuilder line = new StringBuilder(width);
        appendHeader(line, dis);
        boolean appendFragment = appendFragment(line, dis, frags.vr());
        appendKeyword(line, dis.tag(), null);
        System.out.println(line);
        return appendFragment || dis.readValue(dis, frags);
    }

    private boolean appendFragment(StringBuilder line, DicomInputStream dis,
            VR vr) throws IOException {
        if (dis.tag() != Tag.Item)
            return false;
        
        byte[] b = new byte[dis.length()];
        dis.readFully(b);
        append(line, vr.binaryValueAsStrings(b, dis.bigEndian()));
        return true;
    }

    private void promptPreamble(byte[] preamble) {
        if (preamble == null)
            return;
        
        StringBuilder line = new StringBuilder(width);
        line.append("0:");
        append(line, VR.OB.binaryValueAsStrings(preamble, false));
        System.out.println(line);
    }

    private StringBuilder appendHeader(StringBuilder line,
            DicomInputStream dis) {
        int tag = dis.tag();
        line.append(dis.getTagPosition()).append(": ");
        int level = dis.level();
        while (level-- > 0)
            line.append('>');
        line.append(TagUtils.toString(tag)).append(' ');
        VR vr = dis.vr();
        if (vr != null)
            line.append(vr).append(' ');

        line.append('#').append(dis.length());
        return line;
    }

    private StringBuilder appendKeyword(StringBuilder line, int tag,
            String privateCreator) {
        if (line.length() + 1 < width) {
            line.append(' ');
            line.append(ElementDictionary.keywordOf(tag, privateCreator));
            if (line.length() > width)
                line.setLength(width);
        }
        return line;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            if (cl != null) {
                Dcm2Txt dcm2txt = new Dcm2Txt();
                InputStream is = inputStream(cl.getArgList());
                try {
                    dcm2txt.parse(is);
                } finally {
                    if (is != System.in)
                        is.close();
                }
            }
        } catch (ParseException e) {
            System.err.println("dcm2txt: " + e.getMessage());
            System.err.println("Try `dcm2txt --help' for more information.");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("dcm2txt: " + e.getMessage());
            System.exit(2);
        }
    }

    private static InputStream inputStream(List<String> argList) 
            throws FileNotFoundException {
        return argList.isEmpty() ? System.in 
                                 : new FileInputStream(argList.get(0));
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options options = new Options();
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
            cl = null;
        }
        if (cl.hasOption("version")) {
            System.out.println("dcm2txt " + 
                    Dcm2Txt.class.getPackage().getImplementationVersion());
            cl = null;
        }
        return cl;
    }

}
