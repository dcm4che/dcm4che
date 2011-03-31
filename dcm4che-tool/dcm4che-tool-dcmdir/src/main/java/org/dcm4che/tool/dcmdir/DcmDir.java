package org.dcm4che.tool.dcmdir;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.media.DicomDirWriter;
import org.dcm4che.media.RecordFactory;
import org.dcm4che.media.RecordType;
import org.dcm4che.util.UIDUtils;

public class DcmDir {

    private static final String USAGE =
        "dcmdir -{cdlpuz} <dicomdir> [Options] [<file>..][<directory>..]";

    private static final String DESCRIPTION = 
        "\nUtility to read, create and update DICOM directory files." +
        "\n-\nPrompts:" +
        "\n'.' - add record(s) referring regular DICOM Part 10 file" +
        "\n'F' - add record(s) referring file without File Meta Information" +
        "\n'p' - add record(s) referring instance without Patient ID, using" +
        " the Study Instance UID as Patient ID in the PATIENT record" +
        "\n'P' - add record(s) referring file without File Meta Information" +
        " with instance without Patient ID, using the Study Instance UID" +
        " as Patient ID in the PATIENT record" +
        "\n'r' - add root record referring instance without Study Instance UID" +
        "\n'R' - add root record referring file without File Meta Information" +
        " with instance without Study Instance UID" +
        "\n'-' - do not add any record for already referenced file" +
        "\n'x' - delete record referring one file" +
        "\n-\nOptions:";

    private static final String EXAMPLE = 
        "--\nExample 1: list content of DICOMDIR to stdout:" +
        "\n$ dicomdir -l /media/cdrom/DICOMDIR" +
        "\n--\nExample 2: create a new directory file with specified " +
        "File-set ID and Descriptor File, referencing all DICOM Files in " +
        "directory disk99/DICOM:" +
        "\n$ dicomdir -c disk99/DICOMDIR -I DISK99 -D disk99/README" +
        " disk99/DICOM\n" +
        "\n--\nExample 3: add directory records referencing all DICOM files " +
        "in directory disk99/DICOM/CT1 to existing directory file:" +
        "\n$ dicomdir -u disk99/DICOMDIR disk99/DICOM/CT1" +
        "\n--\nExample 4: delete/deactivate directory records referencing " +
        "DICOM files in directory disk99/DICOM/CT2:" +
        "\n$ dicomdir -d disk99/DICOMDIR disk99/DICOM/CT2" +
        "\n--\nExample 5: delete/deactivate directory records without child " +
        "records referencing any DICOM file:" +
        "\n$ dicomdir -p disk99/DICOMDIR" +
        "\n--\nExample 6: compact DICOMDIR by removing inactive records:" +
        "\n$ dicomdir -z disk99/DICOMDIR";

    /** default number of characters per line */
    private static final int DEFAULT_WIDTH = 78;

    private boolean inUse;
    private String uid;
    private String id;
    private File descFile;
    private String descFileCharset;
    private int width = DEFAULT_WIDTH;
    private boolean groupLength;
    private boolean undefSeqLength;
    private boolean undefEmptySeqLength;
    private boolean undefItemLength;
    private boolean undefEmptyItemLength;
    private boolean origSeqLength;
    private boolean checkDuplicate;

    private File file;
    private DicomDirReader in;
    private DicomDirWriter out;
    private RecordFactory recFact;

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        OptionGroup cmdGroup = new OptionGroup();
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("list content of directory file <dicomdir> " +
                    "to standard out")
                .create("l"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("create new directory file <dicomdir> with " +
                    "references to DICOM files specified by file.. or " +
                    "directory.. arguments")
                .create("c"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("update existing directory file <dicomdir> " +
                    "with references to DICOM files specified by file.. or " +
                    "directory.. arguments")
                .create("u"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("delete records referring DICOM files" +
                    "specified by file.. or directory.. arguments from " +
                    "existing directory file <dicomdir> by setting its " +
                    "Record In-use Flag = 0")
                .create("d"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("purge records without file references from " +
                    "directory file <dicomdir> by setting its Record In-use " +
                    "Flag = 0")
                .create("p"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription("compact existing directory file <dicomdir> " +
                    "by removing records with Record In-use Flag != 0")
                .create("z"));
        opts.addOptionGroup(cmdGroup);
        opts.addOption(OptionBuilder
                .withLongOpt("desc")
                .hasArg()
                .withArgName("txtfile")
                .withDescription("specify File-set Descriptor File")
                .create("D"));
        opts.addOption(OptionBuilder
                .withLongOpt("desc-charset")
                .hasArg()
                .withArgName("code")
                .withDescription("Character Set used in File-set Descriptor " +
                     "File (\"ISO_IR 100\" = ISO Latin 1).")
                .create("C"));
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-id")
                .hasArg()
                .withArgName("id")
                .withDescription("specify File-set ID")
                .create("I"));
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-uid")
                .hasArg()
                .withArgName("uid")
                .withDescription("specify File-set UID")
                .create("U"));
        opts.addOption(OptionBuilder
                .withLongOpt("width")
                .hasArg()
                .withArgName("col")
                .withDescription("set line length; default: 78")
                .create("w"));
        opts.addOption(null, "in-use", false, "only list directory records " +
                "with Record In-use Flag != 0");
        opts.addOption(null, "group-len", false, 
                "Include (gggg,0000) Group Length attributes. At default, " +
                "optional Group Length attributes are excluded.");
        OptionGroup sqlenGroup = new OptionGroup();
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-seq-len")
                .withDescription("Encode sequences with explicit length. " +
                    "At default, non-empty sequences are encoded with " +
                    "undefined length.")
                .create());
        sqlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-seq-len")
                .withDescription("Encode all sequences with undefined length. " +
                    "At default, only non-empty sequences are encoded with " +
                    "undefined length.")
                .create());
        opts.addOptionGroup(sqlenGroup);
        OptionGroup itemlenGroup = new OptionGroup();
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("expl-item-len")
                .withDescription("Encode sequence items with explicit length. " +
                    "At default, non-empty sequence items are encoded with " +
                    "undefined length.")
                .create());
        itemlenGroup.addOption(OptionBuilder
                .withLongOpt("undef-item-len")
                .withDescription("Encode all sequence items with undefined " +
                    "length. At default, only non-empty sequence items are " +
                    "encoded with undefined length.")
                .create());
        opts.addOptionGroup(itemlenGroup);
        opts.addOption(null, "orig-seq-len", false, 
                "Preserve encoding of sequence length from the original file");
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
        if (cmdGroup.getSelected() == null)
            throw new ParseException(
                    "You must specify one of the -crudpz options");
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmDir dcmdir = new DcmDir();
            dcmdir.setInUse(cl.hasOption("in-use"));
            dcmdir.setEncodeGroupLength(cl.hasOption("group-len"));
            dcmdir.setUndefItemLength(!cl.hasOption("expl-item-len"));
            dcmdir.setUndefSequenceLength(!cl.hasOption("expl-seq-len"));
            dcmdir.setUndefEmptyItemLength(cl.hasOption("undef-item-len"));
            dcmdir.setUndefEmptySequenceLength(cl.hasOption("undef-seq-len"));
            dcmdir.setOriginalSequenceLength(cl.hasOption("orig-seq-len"));
            if (cl.hasOption("U"))
                dcmdir.setFilesetUID(cl.getOptionValue("U"));
            if (cl.hasOption("I"))
                dcmdir.setFilesetID(cl.getOptionValue("I"));
            if (cl.hasOption("D"))
                dcmdir.setDescriptorFile(new File(cl.getOptionValue("D")));
            if (cl.hasOption("C"))
                dcmdir.setDescriptorFileCharset(cl.getOptionValue("C"));
            if (cl.hasOption("w")) {
                String s = cl.getOptionValue("w");
                try {
                    dcmdir.setWidth(Integer.parseInt(s));
                } catch (IllegalArgumentException e) {
                    throw new ParseException("Illegal line length: " + s);
                }
            }
            try {
                List<String> argList = cl.getArgList();
                long start = System.currentTimeMillis();
                if (cl.hasOption("l")) {
                    dcmdir.openForReadOnly(new File(cl.getOptionValue("l")));
                    dcmdir.list();
                } else if (cl.hasOption("d")) {
                    dcmdir.open(new File(cl.getOptionValue("d")));
                    int num = 0;
                    for (String arg : argList)
                        num += dcmdir.removeReferenceTo(new File(arg));
                    dcmdir.close();
                    long end = System.currentTimeMillis();
                    System.out.println();
                    System.out.println("Delete " + num 
                            + " directory record(s) from existing directory file "
                            + dcmdir.getFile() + " in " + (end - start) + "ms.");
                } else if (cl.hasOption("p")) {
                    dcmdir.open(new File(cl.getOptionValue("p")));
                    int num = dcmdir.purge();
                    dcmdir.close();
                    long end = System.currentTimeMillis();
                    System.out.println("Purge " + num 
                            + " directory record(s) from existing directory file "
                            + dcmdir.getFile() + " in " + (end - start) + "ms.");
                } else if (cl.hasOption("z")) {
                    String fpath = cl.getOptionValue("z");
                    File f = new File(fpath);
                    File bak = new File(fpath + "~");
                    dcmdir.compact(f, bak);
                    long end = System.currentTimeMillis();
                    System.out.println("Compact " + f + " from " + bak.length() 
                            + " to " + f.length() + " bytes in " + (end - start) 
                            + "ms.");
                } else {
                    if (cl.hasOption("c")) {
                        dcmdir.create(new File(cl.getOptionValue("c")));
                    } else if (cl.hasOption("u")) {
                        dcmdir.open(new File(cl.getOptionValue("u")));
                    }
                    dcmdir.setRecordFactory(new RecordFactory());
                    int num = 0;
                    for (String arg : argList)
                        num += dcmdir.addReferenceTo(new File(arg));
                    dcmdir.close();
                    long end = System.currentTimeMillis();
                    System.out.println();
                    System.out.println("Add " + num 
                            + " directory records to directory file "
                            + dcmdir.getFile() + " in " + (end - start) + "ms.");
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

    public void compact(File f, File bak) throws IOException {
        File tmp = File.createTempFile("DICOMDIR", null, f.getParentFile());
        DicomDirReader r = new DicomDirReader(f);
        try {
            setFilesetUID(r.getFileSetUID());
            setFilesetID(r.getFileSetID());
            setDescriptorFile(
                    r.getDescriptorFile());
            setDescriptorFileCharset(
                    r.getDescriptorFileCharacterSet());
            create(tmp);
            copyFrom(r);
        } finally {
            close();
            try { r.close(); } catch (IOException ignore) {}
        }
        bak.delete();
        if (!f.renameTo(bak)) {
            throw new IOException("Failed to rename " + f +
                    " to " + bak);
        }
        if (!tmp.renameTo(f)) {
            throw new IOException("Failed to rename " + tmp +
                    " to " + f);
        }
    }

    public void copyFrom(DicomDirReader r) throws IOException {
        Attributes rec = r.findFirstRootDirectoryRecordInUse();
        while (rec != null) {
            copyChildsFrom(r, rec,
                    out.addRootDirectoryRecord(new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec);
        }
    }

    private void copyChildsFrom(DicomDirReader r, Attributes src,
            Attributes dst) throws IOException {
        Attributes rec = r.findLowerDirectoryRecordInUse(src);
        while (rec != null) {
            copyChildsFrom(r, rec,
                    out.addLowerDirectoryRecord(dst, new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec);
        }
    }

    public final File getFile() {
        return file;
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

    public final void setEncodeGroupLength(boolean groupLength) {
        this.groupLength = groupLength;
    }

    public final void setUndefSequenceLength(boolean undefLength) {
        this.undefSeqLength = undefLength;
    }

    public final void setUndefEmptySequenceLength(boolean undefLength) {
        this.undefEmptySeqLength = undefLength;
    }

    public final void setUndefItemLength(boolean undefLength) {
        this.undefItemLength = undefLength;
    }

    public final void setUndefEmptyItemLength(boolean undefLength) {
        this.undefEmptyItemLength = undefLength;
    }

    public final void setOriginalSequenceLength(boolean origSeqLength) {
        this.origSeqLength = origSeqLength;
    }

    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    public final void setCheckDuplicate(boolean checkDuplicate) {
        this.checkDuplicate = checkDuplicate;
    }

    public final void setRecordFactory(RecordFactory recFact) {
        this.recFact = recFact;
    }

    public void close() {
        if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
            in = null;
            out = null;
        } 
    }

    public void openForReadOnly(File file) throws IOException {
        this.file = file;
        in = new DicomDirReader(file);
    }

    public void create(File file) throws IOException {
        this.file = file;
        in = out = DicomDirWriter
                .create(file, uid(), id, descFile, descFileCharset);
        setEncodeOptions();
        setCheckDuplicate(false);
    }

    public void open(File file) throws IOException {
        this.file = file;
        in = out = DicomDirWriter.open(file);
        setEncodeOptions();
        setCheckDuplicate(true);
    }

    private void setEncodeOptions() {
        out.setEncodeGroupLength(groupLength);
        if (!origSeqLength) {
            out.setUndefSequenceLength(undefSeqLength);
            out.setUndefEmptySequenceLength(undefEmptySeqLength);
            out.setUndefItemLength(undefItemLength);
            out.setUndefEmptyItemLength(undefEmptyItemLength);
        }
    }

    private String uid() {
        return uid == null ? UIDUtils.createUID() : uid;
    }

    public void list() throws IOException {
        checkIn();
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
        int indexLen = index.length();
        int i = 1;
        while (rec != null) {
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
        };
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

    public int addReferenceTo(File f) throws IOException {
        checkOut();
        checkRecordFactory();
        int n = 0;
        if (f.isDirectory()) {
            for (String s : f.list())
                n += addReferenceTo(new File(f, s));
            return n;
        }
        // do not add reference to DICOMDIR
        if (f.equals(file))
            return 0;

        Attributes fmi;
        Attributes dataset;
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(f);
            din.setIncludeBulkData(false);
            fmi = din.readFileMetaInformation();
            dataset = din.readDataset(-1, Tag.PixelData);
        } catch (IOException e) {
            System.out.println();
            System.out.println("Failed to parse file " + f
                    + ": " + e.getMessage());
            return 0;
        } finally {
            if (din != null)
                try { din.close(); } catch (Exception ignore) {}
        }
        char prompt = '.';
        if (fmi == null) {
            fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
            prompt = 'F';
        }
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (iuid == null) {
            System.out.println();
            System.out.println("Skip DICOM file " + f
                    + " without SOP Instance UID (0008, 0018)");
            return 0;
        }
        String pid = dataset.getString(Tag.PatientID, null);
        String styuid = dataset.getString(Tag.StudyInstanceUID, null);
        String seruid = dataset.getString(Tag.SeriesInstanceUID, null);
        if (styuid != null && seruid != null) {
            if (pid == null) {
                dataset.setString(Tag.PatientID, VR.LO, pid = styuid);
                prompt = prompt == 'F' ? 'P' : 'p';
            }
            Attributes patRec = in.findPatientRecord(pid);
            if (patRec == null) {
                patRec = recFact.createRecord(RecordType.PATIENT, null,
                        dataset, null, null);
                out.addRootDirectoryRecord(patRec);
                n++;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (studyRec == null) {
                studyRec = recFact.createRecord(RecordType.STUDY, null,
                        dataset, null, null);
                out.addLowerDirectoryRecord(patRec, studyRec);
                n++;
            }
            Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
            if (seriesRec == null) {
                seriesRec = recFact.createRecord(RecordType.SERIES, null,
                        dataset, null, null);
                out.addLowerDirectoryRecord(studyRec, seriesRec);
                n++;
            }
            Attributes instRec;
            if (checkDuplicate) {
                instRec = in.findInstanceRecord(seriesRec, iuid);
                if (instRec != null) {
                    System.out.print('-');
                    return 0;
                }
            }
            instRec = recFact.createRecord(dataset, fmi, out.toFileIDs(f));
            out.addLowerDirectoryRecord(seriesRec, instRec);
        } else {
            if (checkDuplicate) {
                if (in.findInstanceRecord(iuid) != null) {
                    System.out.print('-');
                    return 0;
                }
            }
            Attributes instRec = recFact.createRecord(dataset, fmi, 
                    out.toFileIDs(f));
            out.addRootDirectoryRecord(instRec);
            prompt = prompt == 'F' ? 'R' : 'r';
        }
        System.out.print(prompt);
        return n + 1;
    }

    public int removeReferenceTo(File f) throws IOException {
        checkOut();
        int n = 0;
        if (f.isDirectory()) {
            for (String s : f.list())
                n += removeReferenceTo(new File(f, s));
            return n;
        }
        String pid;
        String styuid;
        String seruid;
        String iuid;
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(f);
            din.setIncludeBulkData(false);
            Attributes fmi = din.readFileMetaInformation();
            Attributes dataset = din.readDataset(-1, Tag.StudyID);
            iuid = (fmi != null)
                ? fmi.getString(Tag.MediaStorageSOPInstanceUID, null)
                : dataset.getString(Tag.SOPInstanceUID, null);
            if (iuid == null) {
                System.out.println();
                System.out.println("Skip DICOM file " + f
                        + " without SOP Instance UID (0008, 0018)");
                return 0;
            }
            pid = dataset.getString(Tag.PatientID, null);
            styuid = dataset.getString(Tag.StudyInstanceUID, null);
            seruid = dataset.getString(Tag.SeriesInstanceUID, null);
        } catch (IOException e) {
            System.out.println();
            System.out.println("Failed to parse file " + f
                    + ": " + e.getMessage());
            return 0;
        } finally {
            if (din != null)
                try { din.close(); } catch (Exception ignore) {}
        }
        Attributes instRec;
        if (styuid != null && seruid != null) {
            Attributes patRec =
                in.findPatientRecord(pid == null ? styuid : pid);
            if (patRec == null) {
                return 0;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (studyRec == null) {
                return 0;
            }
            Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
            if (seriesRec == null) {
                return 0;
            }
            instRec = in.findInstanceRecord(seriesRec, iuid);
        } else {
            instRec = in.findInstanceRecord(iuid);
        }
        if (instRec == null) {
            return 0;
        }
        out.deleteRecord(instRec);
        System.out.print('x');
        return 1;
    }

    public void commit() throws IOException {
        checkOut();
        out.commit();
    }

    public int purge() throws IOException {
        checkOut();
        return out.purge();
    }

    private void checkIn() {
        if (in == null)
            throw new IllegalStateException("no open file");
    }

    private void checkOut() {
        checkIn();
        if (out == null)
            throw new IllegalStateException("file opened for read-only");
    }

    private void checkRecordFactory() {
        if (recFact == null)
            throw new IllegalStateException("no Record Factory initialized");
    }

}
