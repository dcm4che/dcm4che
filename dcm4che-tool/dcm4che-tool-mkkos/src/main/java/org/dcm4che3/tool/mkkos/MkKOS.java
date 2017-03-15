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

package org.dcm4che3.tool.mkkos;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class MkKOS {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.mkkos.messages");

    private static final int[] PATIENT_AND_STUDY_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.IssuerOfAccessionNumberSequence,
        Tag.ReferringPhysicianName,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyInstanceUID,
        Tag.StudyID 
    };

    private final Attributes attrs = new Attributes();
    private String uidSuffix;
    private String fname;
    private boolean nofmi;
    private DicomEncodingOptions encOpts;
    private String tsuid;
    private String seriesNumber;
    private String instanceNumber;
    private String keyObjectDescription;
    private String retrieveAET;
    private String retrieveURL;
    private String locationUID;
    private Attributes documentTitle;
    private Attributes documentTitleModifier;
    private Properties codes;

    private Attributes kos;
    private Sequence evidenceSeq;
    private Sequence contentSeq;

    public String getFname() {
        return fname;
    }
        
    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setOutputFile(String fname) {
        this.fname = fname;
    }

    public void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public final void setTransferSyntax(String tsuid) {
        this.tsuid = tsuid;
    }

    public final void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public final void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public final void setKeyObjectDescription(String keyObjectDescription) {
        this.keyObjectDescription = keyObjectDescription;
    }

    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    public void setRetrieveURL(String retrieveURL) {
        this.retrieveURL = retrieveURL;
    }

    public void setLocationUID(String locationUID) {
        this.locationUID = locationUID;
    }

    public final void setCodes(Properties codes) {
        this.codes = codes;
    }

    public final void setDocumentTitle(Attributes codeItem) {
        this.documentTitle = codeItem;
    }

    public final void setDocumentTitleModifier(Attributes codeItem) {
        this.documentTitleModifier = codeItem;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        try {
            CommandLine cl = parseComandLine(args);
            final MkKOS main = new MkKOS();
            configure(main, cl);
            System.out.println(rb.getString("scanning"));
            DicomFiles.scan(cl.getArgList(), new DicomFiles.Callback() {
                
                @Override
                public boolean dicomFile(File f, Attributes fmi,
                        long dsPos, Attributes ds) {
                    return main.addInstance(ds);
                }
            });
            System.out.println();
            main.writeKOS();
            System.out.println(
                    MessageFormat.format(rb.getString("stored"), main.fname));
        } catch (ParseException e) {
            System.err.println("mkkos: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        addOptions(opts);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, MkKOS.class);
        if (cl.getArgList().isEmpty())
            throw new ParseException(rb.getString("missing"));
        return cl;
    }

    @SuppressWarnings("static-access")
    public static void addOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code")
                .withDescription(rb.getString("title"))
                .withLongOpt("title")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("code")
                .withDescription(rb.getString("modifier"))
                .withLongOpt("modifier")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("code-config"))
                .withLongOpt("code-config")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("text")
                .withDescription(rb.getString("desc"))
                .withLongOpt("desc")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("aet")
                .withDescription(rb.getString("retrieve-aet"))
                .withLongOpt("retrieve-aet")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("url")
                .withDescription(rb.getString("retrieve-url"))
                .withLongOpt("retrieve-url")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("location-uid"))
                .withLongOpt("location-uid")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("no")
                .withDescription(rb.getString("series-no"))
                .withLongOpt("series-no")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("no")
                .withDescription(rb.getString("inst-no"))
                .withLongOpt("inst-no")
                .create());
       opts.addOption(OptionBuilder
               .hasArg()
               .withArgName("file")
               .withDescription(rb.getString("o-file"))
               .create("o"));
       OptionGroup group = new OptionGroup();
       group.addOption(OptionBuilder
               .withLongOpt("no-fmi")
               .withDescription(rb.getString("no-fmi"))
               .create("F"));
       group.addOption(OptionBuilder
               .withLongOpt("transfer-syntax")
               .hasArg()
               .withArgName("uid")
               .withDescription(rb.getString("transfer-syntax"))
               .create("t"));
       opts.addOptionGroup(group);
       opts.addOption(OptionBuilder
               .hasArgs()
               .withArgName("[seq/]attr=value")
               .withValueSeparator('=')
               .withDescription(rb.getString("set"))
               .create("s"));
       opts.addOption(OptionBuilder
               .hasArg()
               .withArgName("suffix")
               .withDescription(rb.getString("uid-suffix"))
               .withLongOpt("uid-suffix")
               .create(null));
       CLIUtils.addEncodingOptions(opts);
   }

    private static void configure(MkKOS main, CommandLine cl) throws Exception {
        main.setCodes(CLIUtils.loadProperties(
                cl.getOptionValue("code-config", "resource:code.properties"),
                null));
        main.setDocumentTitle(main.toCodeItem(documentTitleOf(cl)));
        if (cl.hasOption("modifier"))
            main.setDocumentTitleModifier(
                    main.toCodeItem(cl.getOptionValue("modifier")));
        main.setKeyObjectDescription(cl.getOptionValue("desc"));
        main.setRetrieveAET(cl.getOptionValue("retrieve-aet", null));
        main.setRetrieveURL(cl.getOptionValue("retrieve-url", null));
        main.setLocationUID(cl.getOptionValue("location-uid", null));
        main.setSeriesNumber(cl.getOptionValue("series-no", "999"));
        main.setInstanceNumber(cl.getOptionValue("inst-no", "1"));
        main.setOutputFile(outputFileOf(cl));
        main.setNoFileMetaInformation(cl.hasOption("F"));
        main.setTransferSyntax(cl.getOptionValue("t", UID.ExplicitVRLittleEndian));
        main.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
        CLIUtils.addAttributes(main.attrs, cl.getOptionValues("s"));
        main.setUIDSuffix(cl.getOptionValue("uid-suffix"));
    }

    public static String outputFileOf(CommandLine cl) throws MissingOptionException {
        if (!cl.hasOption("o"))
            throw new MissingOptionException(rb.getString("missing-o-file"));
        return cl.getOptionValue("o");
    }

    private static String documentTitleOf(CommandLine cl) throws MissingOptionException {
        if (!cl.hasOption("title"))
            throw new MissingOptionException(rb.getString("missing-title"));
        return cl.getOptionValue("title");
    }

    public Attributes toCodeItem(String codeValue) {
        if (codes == null)
            throw new IllegalStateException("codes not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (codeMeaning == null)
            throw new IllegalArgumentException("undefined code value: "
                        + codeValue);
        int endDesignator = codeValue.indexOf('-');
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.CodeValue, VR.SH,
                endDesignator >= 0
                    ? codeValue.substring(endDesignator + 1)
                    : codeValue);
        attrs.setString(Tag.CodingSchemeDesignator, VR.SH,
                endDesignator >= 0
                    ? codeValue.substring(0, endDesignator)
                    : "DCM");
        attrs.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        return attrs;
    }

    public boolean addInstance(Attributes inst) {
        CLIUtils.updateAttributes(inst, attrs, uidSuffix);
        String studyIUID = inst.getString(Tag.StudyInstanceUID);
        String seriesIUID = inst.getString(Tag.SeriesInstanceUID);
        String iuid = inst.getString(Tag.SOPInstanceUID);
        String cuid = inst.getString(Tag.SOPClassUID);
        if (studyIUID == null || seriesIUID == null || iuid == null || cuid == null)
            return false;
        if (kos == null)
            kos = createKOS(inst);
        refSOPSeq(refSeriesSeq(studyIUID), seriesIUID).add(refSOP(cuid, iuid));
        contentSeq.add(contentItem(valueTypeOf(inst), refSOP(cuid, iuid)));
        return true;
    }

    public void writeKOS() throws IOException {
        DicomOutputStream dos = new DicomOutputStream(
                new BufferedOutputStream(fname != null 
                        ? new FileOutputStream(fname)
                        : new FileOutputStream(FileDescriptor.out)),
                nofmi ? UID.ImplicitVRLittleEndian
                      : UID.ExplicitVRLittleEndian);
        dos.setEncodingOptions(encOpts);
        try {
            dos.writeDataset(
                    nofmi ? null : kos.createFileMetaInformation(tsuid),
                    kos);
        } finally {
            dos.close();
        }
    }

    private Sequence refSeriesSeq(String studyIUID) {
        for (Attributes refStudy : evidenceSeq)
            if (studyIUID.equals(refStudy.getString(Tag.StudyInstanceUID)))
                return refStudy.getSequence(Tag.ReferencedSeriesSequence);

        Attributes refStudy = new Attributes(2);
        Sequence refSeriesSeq = refStudy.newSequence(Tag.ReferencedSeriesSequence, 10);
        refStudy.setString(Tag.StudyInstanceUID, VR.UI, studyIUID);
        evidenceSeq.add(refStudy);
        return refSeriesSeq;
    }

    private Sequence refSOPSeq(Sequence refSeriesSeq , String seriesIUID) {
        for (Attributes refSeries : refSeriesSeq)
            if (seriesIUID.equals(refSeries.getString(Tag.SeriesInstanceUID)))
                return refSeries.getSequence(Tag.ReferencedSOPSequence);

        Attributes refSeries = new Attributes(5);
        if (retrieveAET != null)
            refSeries.setString(Tag.RetrieveAETitle, VR.AE, retrieveAET);
        if (retrieveURL != null)
            refSeries.setString(Tag.RetrieveURL, VR.UR, retrieveURL);
        Sequence refSOPSeq = refSeries.newSequence(Tag.ReferencedSOPSequence, 100);
        refSeries.setString(Tag.SeriesInstanceUID, VR.UI, seriesIUID);
        if (locationUID != null)
            refSeries.setString(Tag.RetrieveLocationUID, VR.UI, locationUID);
        refSeriesSeq.add(refSeries);
        return refSOPSeq;
    }

    private String valueTypeOf(Attributes inst) {
        return inst.contains(Tag.PhotometricInterpretation) ? "IMAGE"
                      : inst.contains(Tag.WaveformSequence) ? "WAVEFORM"
                                                            : "COMPOSITE";
    }

    private Attributes refSOP(String cuid, String iuid) {
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        return item;
    }

    private Attributes createKOS(Attributes inst) {
        Attributes attrs = new Attributes(inst, PATIENT_AND_STUDY_ATTRS);
        attrs.setString(Tag.SOPClassUID, VR.UI, UID.KeyObjectSelectionDocumentStorage);
        attrs.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        attrs.setDate(Tag.ContentDateAndTime, new Date());
        attrs.setString(Tag.Modality, VR.CS, "KO");
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        attrs.setString(Tag.SeriesNumber, VR.IS, seriesNumber);
        attrs.setString(Tag.InstanceNumber, VR.IS, instanceNumber);
        attrs.setString(Tag.ValueType, VR.CS, "CONTAINER");
        attrs.setString(Tag.ContinuityOfContent, VR.CS, "SEPARATE");
        attrs.newSequence(Tag.ConceptNameCodeSequence, 1).add(documentTitle);
        evidenceSeq = attrs.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1);
        attrs.newSequence(Tag.ContentTemplateSequence, 1).add(templateIdentifier());
        contentSeq = attrs.newSequence(Tag.ContentSequence, 1);
        if (documentTitleModifier != null)
            contentSeq.add(documentTitleModifier());
        if (keyObjectDescription != null)
            contentSeq.add(keyObjectDescription());
        return attrs;
    }

    private Attributes templateIdentifier() {
        Attributes attrs = new Attributes(2);
        attrs.setString(Tag.MappingResource, VR.CS, "DCMR");
        attrs.setString(Tag.TemplateIdentifier, VR.CS, "2010");
        return attrs ;
    }

    private Attributes documentTitleModifier() {
        Attributes item = new Attributes(4);
        item.setString(Tag.RelationshipType, VR.CS, "HAS CONCEPT MOD");
        item.setString(Tag.ValueType, VR.CS, "CODE");
        item.newSequence(Tag.ConceptNameCodeSequence, 1).add(toCodeItem("DCM-113011"));
        item.newSequence(Tag.ConceptCodeSequence, 1).add(documentTitleModifier);
        return item;
    }

    private Attributes keyObjectDescription() {
        Attributes item = new Attributes(4);
        item.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
        item.setString(Tag.ValueType, VR.CS, "TEXT");
        item.newSequence(Tag.ConceptNameCodeSequence, 1).add(toCodeItem("DCM-113012"));
        item.setString(Tag.TextValue, VR.UT, keyObjectDescription);
        return item;
    }

    private Attributes contentItem(String valueType, Attributes refSOP) {
        Attributes item = new Attributes(3);
        item.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
        item.setString(Tag.ValueType, VR.CS, valueType);
        item.newSequence(Tag.ReferencedSOPSequence, 1).add(refSOP);
        return item;
    }

}
