/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"; you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.hl7;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Sep 2017
 */
public class ERRSegment extends HL7Segment {

    public static final String SegmentSequenceError = "100";
    public static final String RequiredFieldMissing = "101";
    public static final String DataTypeError = "102";
    public static final String TableValueNotFound = "103";
    public static final String UnsupportedMessageType = "200";
    public static final String UnsupportedEventCode = "201";
    public static final String UnsupportedProcessingID = "202";
    public static final String UnsupportedVersionID = "203";
    public static final String UnknownKeyIdentifier = "204";
    public static final String DuplicateKeyIdentifier = "205";
    public static final String ApplicationRecordLocked = "206";
    public static final String ApplicationInternalError = "207";

    public static final String UnknownSendingApplication = "MSH^1^3";
    public static final String UnknownSendingFacility = "MSH^1^4";
    public static final String UnknownReceivingApplication = "MSH^1^5";
    public static final String UnknownReceivingFacility = "MSH^1^6";
    public static final String MissingMessageType = "MSH^1^9";
    public static final String MissingMessageControlID = "MSH^1^10";

    public ERRSegment(char fieldSeparator, String encodingCharacters) {
        super(9, fieldSeparator, encodingCharacters);
        setField(0, "ERR");
        setHL7ErrorCode(ApplicationInternalError);
        setSeverity("E");
    }

    public ERRSegment() {
        this('|', "^~\\&");
    }

    public ERRSegment(HL7Segment msh) {
        this(msh.getFieldSeparator(), msh.getEncodingCharacters());
    }

    public ERRSegment setErrorLocation(String errorLocation) {
        setField(2, errorLocation.replace('^', getComponentSeparator()));
        return this;
    }

    public ERRSegment setHL7ErrorCode(String hl7ErrorCode) {
        setField(3, hl7ErrorCode);
        return this;
    }

    public ERRSegment setSeverity(String severity) {
        setField(4, severity);
        return this;
    }

    public ERRSegment setApplicationErrorCode(String applicationErrorCode) {
        setField(5, applicationErrorCode);
        return this;
    }

    public ERRSegment setApplicationErrorParameter(String applicationErrorParameter) {
        setField(6, applicationErrorParameter);
        return this;
    }

    public ERRSegment setDiagnosticInformation(String diagnosticInformation) {
        setField(7, diagnosticInformation);
        return this;
    }

    public ERRSegment setUserMessage(String userMessage) {
        setField(8, userMessage);
        return this;
    }
}
