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

package org.dcm4che.tool.dcmdir;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.media.DicomDirWriter;
import org.dcm4che.media.RecordFactory;
import org.dcm4che.media.RecordType;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.tool.common.EncodingParams;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DcmDir {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.dcmdir.dcmdir");

    /** default number of characters per line */
    private static final int DEFAULT_WIDTH = 78;

    private boolean inUse;
    private String uid;
    private String id;
    private File descFile;
    private String descFileCharset;
    private int width = DEFAULT_WIDTH;
    private final EncodingParams encParams = new EncodingParams();
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
        CLIUtils.addCommonOptions(opts);
        OptionGroup cmdGroup = new OptionGroup();
        addCommandOptions(cmdGroup);
        opts.addOptionGroup(cmdGroup);
        addFilesetInfoOptions(opts);
        opts.addOption(OptionBuilder
                .withLongOpt("width")
                .hasArg()
                .withArgName("col")
                .withDescription(rb.getString("width"))
                .create("w"));
        opts.addOption(null, "in-use", false, rb.getString("in-use"));
        opts.addOption(null, "orig-seq-len", false,
                rb.getString("orig-seq-len"));
        CLIUtils.addEncodingOptions(opts);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, DcmDir.class);
        if (cmdGroup.getSelected() == null)
            throw new ParseException(rb.getString("missing"));
        return cl;
    }

    @SuppressWarnings("static-access")
    private static void addFilesetInfoOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("desc")
                .hasArg()
                .withArgName("txtfile")
                .withDescription(rb.getString("desc"))
                .create("D"));
        opts.addOption(OptionBuilder
                .withLongOpt("desc-charset")
                .hasArg()
                .withArgName("code")
                .withDescription(rb.getString("desc-charset"))
                .create("C"));
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-id")
                .hasArg()
                .withArgName("id")
                .withDescription(rb.getString("fileset-id"))
                .create("I"));
        opts.addOption(OptionBuilder
                .withLongOpt("fileset-uid")
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("fileset-uid"))
                .create("U"));
    }

    @SuppressWarnings("static-access")
    private static void addCommandOptions(OptionGroup cmdGroup) {
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("list"))
                .create("l"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("create"))
                .create("c"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("update"))
                .create("u"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("delete"))
                .create("d"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("purge"))
                .create("p"));
        cmdGroup.addOption(OptionBuilder
                .hasArg()
                .withArgName("dicomdir")
                .withDescription(rb.getString("compact"))
                .create("z"));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmDir dcmdir = new DcmDir();
            dcmdir.setInUse(cl.hasOption("in-use"));
            CLIUtils.configure(dcmdir.encParams, cl);
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
                    throw new ParseException(MessageFormat.format(
                            rb.getString("illegal-width"), s));
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
                    System.out.println(MessageFormat.format(
                            rb.getString("deleted"),
                            num, dcmdir.getFile(), (end - start)));
                } else if (cl.hasOption("p")) {
                    dcmdir.open(new File(cl.getOptionValue("p")));
                    int num = dcmdir.purge();
                    dcmdir.close();
                    long end = System.currentTimeMillis();
                    System.out.println(MessageFormat.format(
                            rb.getString("purged"),
                            num, dcmdir.getFile(), (end - start)));
                } else if (cl.hasOption("z")) {
                    String fpath = cl.getOptionValue("z");
                    File f = new File(fpath);
                    File bak = new File(fpath + "~");
                    dcmdir.compact(f, bak);
                    long end = System.currentTimeMillis();
                    System.out.println(MessageFormat.format(
                            rb.getString("compacted"),
                            f, bak.length(), f.length(), (end - start)));
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
                    System.out.println(MessageFormat.format(
                            rb.getString("added"),
                            num, dcmdir.getFile(), (end - start)));
                }
            } finally {
                dcmdir.close();
            }
        } catch (ParseException e) {
            System.err.println("dcmdir: " + e.getMessage());
            System.err.println(rb.getString("try"));
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
        rename(f, bak);
        rename(tmp, f);
    }

    private void rename(File from, File to) throws IOException {
        if (!from.renameTo(to))
            throw new IOException(
                    MessageFormat.format(rb.getString("failed-to-rename"),
                            from, to));
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
        SafeClose.close(in);
        in = null;
        out = null;
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
        out.setEncodeGroupLength(encParams.isGroupLength());
        if (!origSeqLength) {
            out.setUndefSequenceLength(encParams.isUndefSequenceLength());
            out.setUndefEmptySequenceLength(
                    encParams.isUndefEmptySequenceLength());
            out.setUndefItemLength(encParams.isUndefItemLength());
            out.setUndefEmptyItemLength(encParams.isUndefEmptyItemLength());
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
            System.out.println(
                    MessageFormat.format(rb.getString("failed-to-parse"),
                            f, e.getMessage()));
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
            System.out.println(MessageFormat.format(
                    rb.getString("skip-file"), f));
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
                System.out.println(MessageFormat.format(
                        rb.getString("skip-file"), f));
                return 0;
            }
            pid = dataset.getString(Tag.PatientID, null);
            styuid = dataset.getString(Tag.StudyInstanceUID, null);
            seruid = dataset.getString(Tag.SeriesInstanceUID, null);
        } catch (IOException e) {
            System.out.println();
            System.out.println(
                    MessageFormat.format(rb.getString("failed-to-parse"),
                            f, e.getMessage()));
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
            throw new IllegalStateException(rb.getString("no-open-file"));
    }

    private void checkOut() {
        checkIn();
        if (out == null)
            throw new IllegalStateException(rb.getString("read-only"));
    }

    private void checkRecordFactory() {
        if (recFact == null)
            throw new IllegalStateException(rb.getString("no-record-factory"));
    }

}
