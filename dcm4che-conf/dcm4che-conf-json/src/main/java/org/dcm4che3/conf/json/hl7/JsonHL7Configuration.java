/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.json.hl7;

import jakarta.json.stream.JsonParser;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.json.ConfigurationDelegate;
import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jan 2016
 */
public class JsonHL7Configuration extends JsonConfigurationExtension {

    private final List<JsonHL7ConfigurationExtension> extensions = new ArrayList<>();

    public void addHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        return true;
    }

    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        HL7DeviceExtension ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("hl7Application");
        for (HL7Application hl7App : ext.getHL7Applications())
            writeTo(device, hl7App, writer);

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config)
            throws ConfigurationException {
        if (!reader.getString().equals("hl7Application"))
            return false;

        HL7DeviceExtension ext = new HL7DeviceExtension();
        loadFrom(ext, reader, device, config);
        device.addDeviceExtension(ext);
        return true;
    }

    private void writeTo(Device device, HL7Application hl7App, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("hl7ApplicationName", hl7App.getApplicationName(), null);
        writer.writeNotNull("dicomInstalled", hl7App.getInstalled());
        writer.writeConnRefs(device.listConnections(), hl7App.getConnections());
        writer.writeNotEmpty("hl7AcceptedSendingApplication", hl7App.getAcceptedSendingApplications());
        writer.writeNotEmpty("hl7OtherApplicationName", hl7App.getOtherApplicationNames());
        writer.writeNotEmpty("hl7AcceptedMessageType", hl7App.getAcceptedMessageTypes());
        writer.writeNotNullOrDef("hl7DefaultCharacterSet", hl7App.getHL7DefaultCharacterSet(), "ASCII");
        writer.writeNotNullOrDef("hl7SendingCharacterSet", hl7App.getHL7SendingCharacterSet(), "ASCII");
        writer.writeNotEmpty("hl7OptionalMSHField", hl7App.getOptionalMSHFields());
        writer.writeNotNullOrDef("dicomDescription", hl7App.getDescription(), null);
        writer.writeNotEmpty("dicomApplicationCluster", hl7App.getApplicationClusters());
        for (JsonHL7ConfigurationExtension ext : extensions)
            ext.storeTo(hl7App, device, writer);
        writer.writeEnd();
    }

    private void loadFrom(HL7DeviceExtension ext, JsonReader reader, Device device, ConfigurationDelegate config)
            throws ConfigurationException {
        List<Connection> conns = device.listConnections();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            HL7Application hl7App = new HL7Application();
            loadFrom(hl7App, reader, device, conns, config);
            reader.expect(JsonParser.Event.END_OBJECT);
            ext.addHL7Application(hl7App);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(HL7Application hl7App, JsonReader reader, Device device, List<Connection> conns,
                          ConfigurationDelegate config) throws ConfigurationException {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "hl7ApplicationName":
                    hl7App.setApplicationName(reader.stringValue());
                    break;
                case "dicomInstalled":
                    hl7App.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        hl7App.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                case "hl7AcceptedSendingApplication":
                    hl7App.setAcceptedSendingApplications(reader.stringArray());
                    break;
                case "hl7OtherApplicationName":
                    hl7App.setOtherApplicationNames(reader.stringArray());
                    break;
                case "hl7AcceptedMessageType":
                    hl7App.setAcceptedMessageTypes(reader.stringArray());
                    break;
                case "hl7DefaultCharacterSet":
                    hl7App.setHL7DefaultCharacterSet(reader.stringValue());
                    break;
                case "hl7SendingCharacterSet":
                    hl7App.setHL7SendingCharacterSet(reader.stringValue());
                    break;
                case "hl7OptionalMSHField":
                    hl7App.setOptionalMSHFields(reader.intArray());
                    break;
                case "dicomDescription":
                    hl7App.setDescription(reader.stringValue());
                    break;
                case "dicomApplicationCluster":
                    hl7App.setApplicationClusters(reader.stringArray());
                    break;
                default:
                    if (!loadHL7ApplicationExtension(device, hl7App, reader, config))
                        reader.skipUnknownProperty();
            }
        }
    }

    private boolean loadHL7ApplicationExtension(Device device, HL7Application hl7App, JsonReader reader,
                                                ConfigurationDelegate config) throws ConfigurationException {
        for (JsonHL7ConfigurationExtension ext : extensions)
            if (ext.loadHL7ApplicationExtension(device, hl7App, reader, config))
                return true;
        return false;
    }

}
