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

    public static final String SEGMENT_SEQUENCE_ERROR = "100^Segment sequence error^HL70357";
    public static final String REQUIRED_FIELD_MISSING = "101^Required field missing^HL70357";
    public static final String DATA_TYPE_ERROR = "102^Data type error^HL70357";
    public static final String TABLE_VALUE_NOT_FOUND = "103^Table value not found^HL70357";
    public static final String UNSUPPORTED_MESSAGE_TYPE = "200^Unsupported message type^HL70357";
    public static final String UNSUPPORTED_EVENT_CODE = "201^Unsupported event code^HL70357";
    public static final String UNSUPPORTED_PROCESSING_ID = "202^Unsupported processing id^HL70357";
    public static final String UNSUPPORTED_VERSION_ID = "203^Unsupported version id^HL70357";
    public static final String UNKNOWN_KEY_IDENTIFIER = "204^Unknown key identifier^HL70357";
    public static final String DUPLICATE_KEY_IDENTIFIER = "205^Duplicate key identifier^HL70357";
    public static final String APPLICATION_RECORD_LOCKED = "206^Application record locked^HL70357";
    public static final String APPLICATION_INTERNAL_ERROR = "207^Application internal error^HL70357";

    public static final String SENDING_APPLICATION = "MSH^1^3^1^1";
    public static final String SENDING_FACILITY = "MSH^1^4^1^1";
    public static final String RECEIVING_APPLICATION = "MSH^1^5^1^1";
    public static final String RECEIVING_FACILITY = "MSH^1^6^1^1";
    public static final String MESSAGE_CODE = "MSH^1^9^1^1";
    public static final String TRIGGER_EVENT = "MSH^1^9^1^2";
    public static final String MESSAGE_DATETIME = "MSH^1^7^1^1";
    public static final String MESSAGE_CONTROL_ID = "MSH^1^10^1^1";
    public static final String MESSAGE_PROCESSING_ID = "MSH^1^11^1^1";
    public static final String MESSAGE_VERSION_ID = "MSH^1^12^1^1";

    public ERRSegment(char fieldSeparator, String encodingCharacters) {
        super(9, fieldSeparator, encodingCharacters);
        setField(0, "ERR");
        setHL7ErrorCode(APPLICATION_INTERNAL_ERROR);
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
