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
 * Java(TM), hosted at https://github.com/dcm4che.
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

package org.dcm4che3.net.service;

import java.io.IOException;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.net.Status;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomServiceException extends IOException {

    private static final long serialVersionUID = -8680017798403768406L;

    private final Attributes rsp;
    private Attributes data;

    public DicomServiceException(int status) {
        rsp = new Attributes();
        setStatus(status);
    }

    public DicomServiceException(int status, String message) {
        this(status, message, true);
    }

    public DicomServiceException(int status, String message, boolean errorComment) {
        super(message);
        rsp = new Attributes();
        setStatus(status);
        if (errorComment) {
            setErrorComment(getMessage());
        }
    }

    public DicomServiceException(int status, Throwable cause) {
        this(status, cause, true);
    }

    public DicomServiceException(int status, Throwable cause, boolean errorComment) {
        super(cause);
        rsp = new Attributes();
        setStatus(status);
        if (errorComment) {
            setErrorComment(getMessage());
        }
    }

    public static Throwable initialCauseOf(Throwable e) {
        if (e == null)
            return null;

        Throwable cause;
        while ((cause = e.getCause()) != null)
            e = cause;
        return e;
    }


    private void setStatus(int status) {
        rsp.setInt(Tag.Status, VR.US, status);
    }

    public int getStatus() {
        return rsp.getInt(Tag.Status, 0);
    }

    public DicomServiceException setUID(int tag, String value) {
        rsp.setString(tag, VR.UI, value);
        return this;
    }

    public DicomServiceException setErrorComment(String val) {
        if (val != null)
            rsp.setString(Tag.ErrorComment, VR.LO, StringUtils.truncate(val, 64));
        return this;
    }

    public DicomServiceException setErrorID(int val) {
        rsp.setInt(Tag.ErrorID, VR.US, val);
        return this;
    }

    public DicomServiceException setEventTypeID(int val) {
        rsp.setInt(Tag.EventTypeID, VR.US, val);
        return this;
    }

    public DicomServiceException setActionTypeID(int val) {
        rsp.setInt(Tag.ActionTypeID, VR.US, val);
        return this;
    }

    public DicomServiceException setOffendingElements(int... tags) {
        rsp.setInt(Tag.OffendingElement, VR.AT, tags);
        return this;
    }

    public DicomServiceException setAttributeIdentifierList(int... tags) {
        rsp.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
        return this;
    }

    public Attributes mkRSP(int cmdField, int msgId) {
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rsp;
    }

    public final Attributes getDataset() {
        return data;
    }

    public final DicomServiceException setDataset(Attributes data) {
        this.data = data;
        return this;
    }

    public static DicomServiceException valueOf(ValidationResult result, Attributes attrs) {
        if (result.hasNotAllowedAttributes())
            return new DicomServiceException(Status.NoSuchAttribute, result.getErrorComment(), false)
                .setAttributeIdentifierList(result.tagsOfNotAllowedAttributes());
        if (result.hasMissingAttributes())
            return new DicomServiceException(Status.MissingAttribute, result.getErrorComment(), false)
                .setAttributeIdentifierList(result.tagsOfMissingAttributes());
        if (result.hasMissingAttributeValues())
            return new DicomServiceException(Status.MissingAttributeValue, result.getErrorComment(), false)
                .setDataset(new Attributes(attrs, result.tagsOfMissingAttributeValues()));
        if (result.hasInvalidAttributeValues())
            return new DicomServiceException(Status.InvalidAttributeValue, result.getErrorComment(), false)
                .setDataset(new Attributes(attrs, result.tagsOfInvalidAttributeValues()));
        return null;
    }
}
