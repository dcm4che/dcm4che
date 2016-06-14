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
package org.dcm4che3.tool.qc;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.qc.test.QCResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */

public class QC {
    static final Logger LOG = LoggerFactory.getLogger(QC.class);
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.qc.messages");
    private static Options opts;
    private Code qcRejectionCode;
    private QCOperation operation;
    private String targetStudyUID;
    private String deleteParams;
    private String studyToDelete;
    private String seriesToDelete;
    private String instanceToDelete;
    private String url;
    private QCUpdateScope updateScope = QCUpdateScope.NONE;
    private Attributes targetStudyAttrs = new Attributes();
    private Attributes targetSeriesAttrs = new Attributes();
    private Attributes targetPatientAttributes = new Attributes();
    private Attributes sourcePatientAttributes = new Attributes();
    private Attributes updateAttrs = new Attributes();
    private IDWithIssuer pid;
    private ArrayList<String> moveUIDs;
    private ArrayList<String> cloneUIDs;
    private ArrayList<String> mergeUIDs;
    private ArrayList<String> rrUIDs;

    public QC() {

    }

    public QC(String url, Code qcRejectionCode, QCOperation operation) {
        this.operation = operation;
        this.qcRejectionCode = qcRejectionCode;
        this.url = url;
    }

    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            cl = parseComandLine(args);
            QC qc = initArgs(cl);
            initializeOptions(cl, qc);

            QCResult result = performOperation("Standard CLI tool Call", qc);
            printResult(result);
        } catch (Exception e) {
            System.err.println("qc: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static void printResult(QCResult result) {
        System.out.println("Server Responsed ["+result.getResponseCode()+"]"+": \n"
                + " with the following message: \n"+ result.getResponseMessage());
    }


    public static QCResult performOperation(String opDescription, QC qc) throws IllegalArgumentException{

        switch (qc.getOperation()){

            case SPLIT:
            case MERGE:
            case SEGMENT:
            case UPDATE:
            case REJECT:
            case RESTORE:
                JsonObject qcMessage = initQCObject(qc);
                System.out.println(qcMessage.toString());
                return sendRequest(opDescription, qc, qcMessage);
            case DELETE:
                if (checkDelete(qc)) {
                    return sendDeleteRequest(opDescription, qc);
                }
            case PATIENT_LINK:
            case PATIENT_MERGE:
            case PATIENT_UNLINK:
            case PATIENT_UPDATEIDS:
                JsonArray qcPatientMessage = initPatientObject(qc);
                qc.setUrl(adjustPatientURL(qc).replace(" ", "%20"));
                return sendRequest(opDescription, qc, qcPatientMessage);
            case LINK2MWL:
            case UPDATE_STUDY_SERIES:
                JsonObject qfMessage = initQCObject(qc);
                return sendRequest(opDescription, qc, qfMessage);
            default:
                throw new IllegalArgumentException("Incorrect Operation specified");
        }
    }

    private static boolean checkDelete(QC qc) {

        if (qc.getPid() != null) {
            return true;
        }

        String[] components = qc.getDeleteParams().split(":");
        int idx = 0;
        for (String str : components) {
            switch (idx) {
                case 0:
                    qc.setStudyToDelete(str);
                    break;
                case 1:
                    qc.setSeriesToDelete(str);
                    break;
                case 2:
                    qc.setInstanceToDelete(str);
                    break;
                default:
                    return false;
            }
            idx++;
        }
        return true;
    }
    
    private static String encodeRejectionCode(Code rejCode) {
        return rejCode.getCodeValue() + ":" + rejCode.getCodeMeaning() + ":" + rejCode.getCodingSchemeDesignator();
    }

    private static QCResult sendDeleteRequest(String desc, QC qc) {
        HttpURLConnection connection = null;
        String bfr = "";
        QCResult result = null;
        try {
            String qcDeleteUrlString = adjustDeleteURL(qc);
            qcDeleteUrlString += "?rejCode=" + encodeRejectionCode(qc.getQcRejectionCode());
            qcDeleteUrlString = qcDeleteUrlString.replace(" ", "%20");
            
            URL url = new URL(qcDeleteUrlString);
            
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream in = connection.getInputStream();

                BufferedReader rdr = new BufferedReader(new InputStreamReader(
                        in));

                while (rdr.ready())
                    bfr += rdr.readLine();
            }
            result = new QCResult(desc, bfr, responseCode);
            return result;
        } catch (Exception e) {
            System.out.println("Error preparing request or "
                    + "parsing Server response - " + e);
        }
        return result;

    }

    private static String adjustPatientURL(QC qc) {
        String url = qc.getUrl();
        return url += "/patients/"+qc.getOperation().toString().split("_")[1].toLowerCase();
    }

    private static String adjustDeleteURL(QC qc) {
        String url = qc.getUrl();
        if (qc.getPid() != null) {
            url += "/delete/patient/" + qc.getPid().getID();
            return   url += qc.getPid().getIssuer() != null? "/issuer/"+ qc.getPid().getIssuer().toString(':'):"";
        }
        url += "/delete/studies/" + qc.getStudyToDelete();
        if (qc.getSeriesToDelete() != null)
            url += "/series/" + qc.getSeriesToDelete();
        if (qc.getInstanceToDelete() != null)
            url += "/instances/" + qc.getInstanceToDelete();
        return url;
    }

    private static QCResult sendRequest(String desc, QC qc, Object qcMessage) {

        HttpURLConnection connection = null;
        String bfr = "";
        QCResult result = null;
        try {
            URL url = new URL(qc.getUrl());
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);

            connection.setDoInput(true);

            connection.setInstanceFollowRedirects(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");

            connection.setRequestProperty("Accept", "application/json");

            connection.setUseCaches(false);

            writeMessage(connection, (JsonStructure) qcMessage);
            int rspCode = connection.getResponseCode();
            InputStream in = rspCode == 200 ? connection.getInputStream() : connection.getErrorStream();

            BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
            while (rdr.ready())
                bfr += rdr.readLine();
            result = new QCResult(desc, bfr, rspCode);
            return result;
        } catch (Exception e) {
            System.out.println("Error preparing request or "
                    + "parsing Server response - " + e);
        }
        connection.disconnect();
        return result;
    }

    private static void writeMessage(HttpURLConnection connection,
                                     JsonStructure qcMessage) throws Exception {
        DataOutputStream wr;
        wr = new DataOutputStream(connection.getOutputStream());
        // wr.writeBytes("Content-Type: application/json" + " \r\n");
        // wr.writeBytes("\r\n");
        JsonWriter writer = Json.createWriter(wr);
        if(qcMessage instanceof JsonObject)
            writer.writeObject((JsonObject) qcMessage);
        else
            writer.writeArray((JsonArray)qcMessage);

        writer.close();
        wr.close();
    }

    private static JsonObject initQCObject(QC qc) {
        mergeUIDs(qc);
        JsonObjectBuilder builder = Json
                .createObjectBuilder()
                .add("operation", qc.getOperation().toString())
                .add("updateScope", qc.getUpdateScope() == null?
                        QCUpdateScope.NONE.toString() : qc.getUpdateScope().toString())
                .add("moveSOPUIDs", toArrayBuilder(qc.getMoveUIDs()))
                .add("cloneSOPUIDs", toArrayBuilder(qc.getCloneUIDs()))
                .add("restoreOrRejectUIDs",
                        toArrayBuilder(qc.getRrUIDs()))
                .add("targetStudyUID", qc.getTargetStudyUID())
                .add("qcRejectionCode", toCodeObject(qc.getQcRejectionCode()))
                .add("targetSeriesData",
                        toAttributesObject(qc.getTargetSeriesAttrs()))
                .add("targetStudyData",
                        toAttributesObject(qc.getTargetStudyAttrs()))
                .add("updateData", toAttributesObject(qc.getUpdateAttrs()));
        if(qc.getPid() != null)
            builder.add("pid", toIDWithIssuerObject(qc.getPid()));
        return builder.build();
    }

    private static JsonArray initPatientObject(QC qc) {
        JsonArrayBuilder builder = Json
                .createArrayBuilder()
                .add(toAttributesObject(qc.getSourcePatientAttributes()))
                .add(toAttributesObject(qc.getTargetPatientAttributes()));
        return builder.build();
    }

    protected static void mergeUIDs(QC qc) {
        if (qc.getOperation() == QCOperation.MERGE) {
            ArrayList<String> tmpMove = qc.getMoveUIDs() == null ?
                    new ArrayList<String>() : qc.getMoveUIDs();
            tmpMove.addAll(qc.getMergeUIDs());
            qc.setMoveUIDs(tmpMove);
        }
    }

    private static JsonObject toIDWithIssuerObject(IDWithIssuer pid) {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("id", emptyIfNull(pid.getID()));
        if (pid.getIssuer() != null) {
            builder.add("issuer", toIssuerObject(pid.getIssuer()));
        }
        return builder.build();
    }

    private static JsonObject toIssuerObject(Issuer issuer) {
        JsonObjectBuilder builder =  Json
                .createObjectBuilder()
                .add("localNamespaceEntityID",
                        emptyIfNull(issuer.getLocalNamespaceEntityID()));
        if(issuer.getUniversalEntityID()!=null)
            builder.add("universalEntityID",
                    emptyIfNull(issuer.getUniversalEntityID()))
                    .add("universalEntityIDType",
                            emptyIfNull(issuer.getUniversalEntityIDType()));
        return builder.build();
    }

    private static JsonObject toAttributesObject(Attributes targetSeriesAttrs) {
        StringWriter strWriter = new StringWriter();
        JsonGenerator gen = Json.createGenerator(strWriter);
        JSONWriter writer = new JSONWriter(gen);
        writer.write(targetSeriesAttrs);
        gen.flush();
        gen.close();
        return Json.createReader(new StringReader(strWriter.toString()))
                .readObject();
    }

    private static JsonObject toCodeObject(Code qcRejectionCode) {
        JsonObjectBuilder codeBuilder = Json
                .createObjectBuilder()
                .add("codeValue", emptyIfNull(qcRejectionCode.getCodeValue()))
                .add("codeMeaning",
                        emptyIfNull(qcRejectionCode.getCodeMeaning()))
                .add("codingSchemeDesignator",
                        emptyIfNull(qcRejectionCode.getCodingSchemeDesignator()));
        if (qcRejectionCode.getCodingSchemeVersion() != null)
            codeBuilder.add("codingSchemeVersion",
                    qcRejectionCode.getCodingSchemeVersion());
        return codeBuilder.build();
    }

    private static String emptyIfNull(String obj) {
        return obj == null ? "" : obj;
    }

    private static JsonArrayBuilder toArrayBuilder(ArrayList<String> moveUIDs) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (String str : moveUIDs)
            arr.add(str);
        return arr;
    }

    private static ArrayList<String> toUIDS(String optionValue) {
        ArrayList<String> uids = new ArrayList<String>();
        for (String str : optionValue.split(",")) {
            uids.add(str);
        }
        return uids;
    }

    public static IDWithIssuer toIDWithIssuer(String optionValue)
            throws MissingArgumentException {
        String[] components = optionValue.split(":");
        if (components.length < 2)
            throw new MissingArgumentException("Missing issuer information");
        if (components.length == 2) // pid and local entity uid
            return new IDWithIssuer(components[0], components[1]);
        else if (components.length == 3) // pid with universal entity and type
            return new IDWithIssuer(components[0], new Issuer(null,
                    components[1], components[2]));
        else
            return new IDWithIssuer(components[0], new Issuer(components[1],
                    components[2], components[3]));
    }

    private static Code toCode(String codeComponents)
            throws MissingArgumentException {
        String[] components = codeComponents.split(":");
        if (components.length < 3)
            throw new MissingArgumentException("Invalid code specified "
                    + " code can be specified as codevalue"
                    + ":codeschemedesignator"
                    + ":codemeaning:codeversion where only "
                    + "version is optional");

        return new Code(components[0], components[2], components.length == 4 ? components[3]
                : null,components[1]);
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs().withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("updateattributes"))
                .create("updateattributes"));
        opts.addOption(OptionBuilder.hasArgs().withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("targetpatientattributes"))
                .create("targetpatientattributes"));
        opts.addOption(OptionBuilder.hasArgs().withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("sourcepatientattributes"))
                .create("sourcepatientattributes"));
        CLIUtils.addCommonOptions(opts);
        opts.addOption(OptionBuilder.hasArgs().withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("overrideseriesattributes"))
                .create("overrideseriesattributes"));
        opts.addOption(OptionBuilder.hasArgs().withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("overridestudyattributes"))
                .create("overridestudyattributes"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("cloneuids"))
                .create("cloneuids"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("moveuids")).create("moveuids"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("mergestudyuids"))
                .create("mergestudyuids"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("restorerejectuids"))
                .create("restorerejectuids"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("pid")).create("pid"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("updatescope"))
                .withArgName("<STUDY|SERIES|PATIENT|INSTANCE>")
                .create("updatescope"));
        opts.addOption(OptionBuilder.hasArg()
                .withDescription(rb.getString("deleteobject"))
                .create("deleteobject"));

        return CLIUtils.parseComandLine(args, opts, rb, QC.class);
    }

    private static QC initArgs(CommandLine cl) throws MissingArgumentException, IndexOutOfBoundsException {

        String codeComponents = null;
        String parsedURL = (String) cl.getArgList().get(0);
        String operation = (String) cl.getArgList().get(1);

        if(cl.getArgList().size() > 2)
            codeComponents = (String) cl.getArgList().get(2);

        QC qc = new QC(parsedURL, codeComponents!=null?toCode(codeComponents):null,
                QCOperation.valueOf(operation.toUpperCase()));

        if (cl.getArgList().size() > 3) {
            qc.setTargetStudyUID((String) cl.getArgList().get(3));
        }

        return qc;
    }

    private static void initializeOptions(CommandLine cl, QC qc) throws MissingArgumentException {
        if (qc.operation == QCOperation.UPDATE)
            if (!cl.hasOption("update-scope"))
                throw new MissingArgumentException(
                        "Missing required argument"
                                + " update scope for update operation");
            else
                qc.setUpdateScope(QCUpdateScope.valueOf(cl
                        .getOptionValue("update-scope")));

        if (cl.hasOption("overridestudyattributes"))
            qc.targetStudyAttrs = getAttributes(cl, "overridestudyattributes");
        if (cl.hasOption("overrideseriesattributes"))
            qc.setTargetSeriesAttrs(getAttributes(cl,
                    "overrideseriesattributes"));
        if (cl.hasOption("updateattributes"))
            qc.setUpdateAttrs(getAttributes(cl, "updateattributes"));
        if(cl.hasOption("targetpatientattributes"))
            qc.setTargetPatientAttributes(getAttributes(cl, "targetpatientattributes"));
        if(cl.hasOption("sourcepatientattributes"))
            qc.setSourcePatientAttributes(getAttributes(cl, "sourcepatientattributes"));
        if (cl.hasOption("pid"))
            qc.setPid(toIDWithIssuer(cl.getOptionValue("pid")));
        if (cl.hasOption("moveuids"))
            qc.setMoveUIDs(toUIDS(cl.getOptionValue("moveuids")));
        if (cl.hasOption("cloneuids"))
            qc.setCloneUIDs(toUIDS(cl.getOptionValue("cloneuids")));
        if (cl.hasOption("restorerejectuids"))
            qc.setRrUIDs(toUIDS(cl.getOptionValue("restorerejectuids")));
        if (cl.hasOption("mergestudyuids"))
            qc.setMergeUIDs(toUIDS(cl.getOptionValue("mergeuids")));
        if (cl.hasOption("deleteobject")
                && qc.getOperation() == QCOperation.DELETE)
            qc.setDeleteParams(cl.getOptionValue("deleteobject"));
    }

    private static Attributes getAttributes(CommandLine cl, String optionName) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues(optionName));
        return temp;
    }

    public Code getQcRejectionCode() {
        return qcRejectionCode;
    }

    public QCOperation getOperation() {
        return operation;
    }

    public String getTargetStudyUID() {
        return targetStudyUID;
    }

    public String getUrl() {
        return url;
    }

    public Attributes getTargetStudyAttrs() {
        return targetStudyAttrs;
    }

    public Attributes getTargetSeriesAttrs() {
        return targetSeriesAttrs;
    }

    public Attributes getUpdateAttrs() {
        return updateAttrs;
    }

    public IDWithIssuer getPid() {
        return pid;
    }

    public ArrayList<String> getMoveUIDs() {
        return moveUIDs == null ? new ArrayList<String>() : moveUIDs;
    }

    public ArrayList<String> getCloneUIDs() {
        return cloneUIDs == null ? new ArrayList<String>() : cloneUIDs;
    }

    public ArrayList<String> getMergeUIDs() {
        return mergeUIDs == null ? new ArrayList<String>() : mergeUIDs;
    }

    public ArrayList<String> getRrUIDs() {
        return rrUIDs == null ? new ArrayList<String>() : rrUIDs;
    }

    public void setQcRejectionCode(Code qcRejectionCode) {
        this.qcRejectionCode = qcRejectionCode;
    }

    public void setOperation(QCOperation operation) {
        this.operation = operation;
    }

    public void setTargetStudyUID(String targetStudyUID) {
        this.targetStudyUID = targetStudyUID;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTargetStudyAttrs(Attributes targetStudyAttrs) {
        this.targetStudyAttrs = targetStudyAttrs;
    }

    public void setTargetSeriesAttrs(Attributes targetSeriesAttrs) {
        this.targetSeriesAttrs = targetSeriesAttrs;
    }

    public void setUpdateAttrs(Attributes updateAttrs) {
        this.updateAttrs = updateAttrs;
    }

    public void setPid(IDWithIssuer pid) {
        this.pid = pid;
    }

    public void setMoveUIDs(ArrayList<String> moveUIDs) {
        this.moveUIDs = moveUIDs;
    }

    public void setCloneUIDs(ArrayList<String> cloneUIDs) {
        this.cloneUIDs = cloneUIDs;
    }

    public void setMergeUIDs(ArrayList<String> mergeUIDs) {
        this.mergeUIDs = mergeUIDs;
    }

    public void setRrUIDs(ArrayList<String> rrUIDs) {
        this.rrUIDs = rrUIDs;
    }

    public QCUpdateScope getUpdateScope() {
        return updateScope;
    }

    public void setUpdateScope(QCUpdateScope updateScope) {
        this.updateScope = updateScope;
    }

    public String getDeleteParams() {
        return deleteParams;
    }

    public void setDeleteParams(String deleteParams) {
        this.deleteParams = deleteParams;
    }

    public String getStudyToDelete() {
        return studyToDelete;
    }

    public void setStudyToDelete(String studyToDelete) {
        this.studyToDelete = studyToDelete;
    }

    public String getSeriesToDelete() {
        return seriesToDelete;
    }

    public void setSeriesToDelete(String seriesToDelete) {
        this.seriesToDelete = seriesToDelete;
    }

    public String getInstanceToDelete() {
        return instanceToDelete;
    }

    public void setInstanceToDelete(String instanceToDelete) {
        this.instanceToDelete = instanceToDelete;
    }

    public Attributes getTargetPatientAttributes() {
        return targetPatientAttributes;
    }

    public void setTargetPatientAttributes(Attributes targetPatientAttributes) {
        this.targetPatientAttributes = targetPatientAttributes;
    }

    public Attributes getSourcePatientAttributes() {
        return sourcePatientAttributes;
    }

    public void setSourcePatientAttributes(Attributes sourcePatientAttributes) {
        this.sourcePatientAttributes = sourcePatientAttributes;
    }
}