package org.dcm4che.tool.dcm2txt;

import java.io.File;
import java.io.IOException;
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

    private static final String USAGE = "dcm2txt [<options>] <dicom-file>";

    private static final String DESCRIPTION = 
        "\nParse specified <dicom-file> (or the standard input if the " +
        "filename provided is - ) and writes a text representation to " +
        "standard output.\n.\nOptions:";

    private static final String EXAMPLE = null;

    /** default number of characters per line */
    private static final int DEFAULT_WIDTH = 78;

    private int width = DEFAULT_WIDTH;

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    public void parse(DicomInputStream dis) throws IOException {
        dis.setDicomInputHandler(this);
        dis.readDataset(-1, -1);
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        promptPreamble(dis.getPreamble());
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        StringBuilder line = new StringBuilder(width + 30);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        VR vr = dis.vr();
        int vallen = dis.length();
        boolean undeflen = vallen == -1;
        if (vr == VR.SQ || undeflen) {
            appendKeyword(dis, line);
            System.out.println(line);
            dis.readValue(dis, attrs);
            if (undeflen) {
                line.setLength(0);
                appendPrefix(dis, line);
                appendHeader(dis, line);
                appendKeyword(dis, line);
                System.out.println(line);
            }
            return;
        }
        int tag = dis.tag();
        byte[] b = dis.readValue();
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(),
                attrs.getSpecificCharacterSet(),
                width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, line);
        }
        System.out.println(line);
        if (tag == Tag.FileMetaInformationGroupLength)
            dis.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID
                || tag == Tag.SpecificCharacterSet
                || TagUtils.isPrivateCreator(tag))
            attrs.setBytes(tag, vr, b);
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        StringBuilder line = new StringBuilder(width);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        appendKeyword(dis, line);
        appendNumber(seq.size() + 1, line);
        System.out.println(line);
        boolean undeflen = dis.length() == -1;
        dis.readValue(dis, seq);
        if (undeflen) {
            line.setLength(0);
            appendPrefix(dis, line);
            appendHeader(dis, line);
            appendKeyword(dis, line);
            System.out.println(line);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        StringBuilder line = new StringBuilder(width + 20);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        appendFragment(line, dis, frags.vr());
        System.out.println(line);
    }

    private void appendPrefix(DicomInputStream dis, StringBuilder line) {
        line.append(dis.getTagPosition()).append(": ");
        int level = dis.level();
        while (level-- > 0)
            line.append('>');
    }

    private void appendHeader(DicomInputStream dis, StringBuilder line) {
        line.append(TagUtils.toString(dis.tag())).append(' ');
        VR vr = dis.vr();
        if (vr != null)
            line.append(vr).append(' ');
        line.append('#').append(dis.length());
    }

    private void appendKeyword(DicomInputStream dis, StringBuilder line) {
        if (line.length() < width) {
            line.append(" ");
            line.append(ElementDictionary.keywordOf(dis.tag(), null));
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void appendNumber(int number, StringBuilder line) {
        if (line.length() < width) {
            line.append(" #");
            line.append(number);
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void appendFragment(StringBuilder line, DicomInputStream dis,
            VR vr) throws IOException {
        byte[] b = dis.readValue();
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), null, 
                width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, line);
        }
    }

    private void promptPreamble(byte[] preamble) {
        if (preamble == null)
            return;
        
        StringBuilder line = new StringBuilder(width);
        line.append("0: [");
        if (VR.OB.prompt(preamble, false, null, width - 5, line))
            line.append(']');
        System.out.println(line);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Txt dcm2txt = new Dcm2Txt();
            if (cl.hasOption("w")) {
                String s = cl.getOptionValue("w");
                try {
                    dcm2txt.setWidth(Integer.parseInt(s));
                } catch (IllegalArgumentException e) {
                    throw new ParseException(
                            "Illegal line length: " + s);
                }
            }
            String fname = fname(cl.getArgList());
            if (fname.equals("-")) {
                dcm2txt.parse(new DicomInputStream(System.in));
            } else {
                DicomInputStream dis =
                        new DicomInputStream(new File(fname));
                try {
                    dcm2txt.parse(dis);
                } finally {
                    dis.close();
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

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException("Missing file operand");
        if (numArgs > 1)
            throw new ParseException("Too many arguments");
        return argList.get(0);
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
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
            System.out.println("dcm2txt " + 
                    Dcm2Txt.class.getPackage().getImplementationVersion());
            System.exit(0);
        }
        return cl;
    }

}
