/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
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
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.dicom.adapters;

import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.adapters.DefaultConfigTypeAdapters;
import org.dcm4che3.data.Code;

public class AuditSimpleTypeAdapters {
    public static class EventTypeCodeAdapter<T> extends DefaultConfigTypeAdapters.CommonAbstractStringTypeAdapter<EventTypeCode> {
        public EventTypeCodeAdapter() {
            super("string");
            metadata.put("class", "EventTypeCode");
        }

        @Override
        public EventTypeCode fromConfigNode(String configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
            Code code = new Code(configNode);

            EventTypeCode eventTypeCode = new EventTypeCode();
            eventTypeCode.setCode(code.getCodeValue());
            eventTypeCode.setCodeSystemName(code.getCodingSchemeDesignator());
            eventTypeCode.setOriginalText(code.getCodeMeaning());

            return eventTypeCode;
        }

        @Override
        public String toConfigNode(EventTypeCode object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
            return new Code(
                    object.getCode(),
                    object.getCodeSystemName(),
                    null,
                    object.getOriginalText())
                    .toString();
        }
    }

    public static class EventIDTypeAdapter<T> extends DefaultConfigTypeAdapters.CommonAbstractStringTypeAdapter<EventID> {
        public EventIDTypeAdapter() {
            super("string");
            metadata.put("class", "EventID");
        }

        @Override
        public EventID fromConfigNode(String configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
            Code code = new Code(configNode);

            EventID eventID = new EventID();
            eventID.setCode(code.getCodeValue());
            eventID.setCodeSystemName(code.getCodingSchemeDesignator());
            eventID.setOriginalText(code.getCodeMeaning());

            return eventID;
        }

        @Override
        public String toConfigNode(EventID object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
            return new Code(
                    object.getCode(),
                    object.getCodeSystemName(),
                    null,
                    object.getOriginalText())
                    .toString();
        }
    }

    public static class RoleIDCodeTypeAdapter<T> extends DefaultConfigTypeAdapters.CommonAbstractStringTypeAdapter<RoleIDCode> {
        public RoleIDCodeTypeAdapter() {
            super("string");
            metadata.put("class", "RoleIDType");
        }

        @Override
        public RoleIDCode fromConfigNode(String configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
            Code code = new Code(configNode);

            RoleIDCode roleIDcode = new RoleIDCode();
            roleIDcode.setCode(code.getCodeValue());
            roleIDcode.setCodeSystemName(code.getCodingSchemeDesignator());
            roleIDcode.setOriginalText(code.getCodeMeaning());

            return roleIDcode;
        }

        @Override
        public String toConfigNode(RoleIDCode object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {
            return new Code(
                    object.getCode(),
                    object.getCodeSystemName(),
                    null,
                    object.getOriginalText())
                    .toString();
        }
    }
}
