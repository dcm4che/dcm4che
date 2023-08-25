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

package org.dcm4che3.conf.json.audit;

import jakarta.json.stream.JsonParser;
import org.dcm4che3.conf.json.ConfigurationDelegate;
import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;

import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public class JsonAuditRecordRepositoryConfiguration extends JsonConfigurationExtension {
    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        AuditRecordRepository arr = device.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null)
            return;

        writer.writeStartObject("dcmAuditRecordRepository");
        writer.writeConnRefs(device.listConnections(), arr.getConnections());
        writer.writeNotNull("dicomInstalled", arr.getInstalled());
        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config) {
        if (!reader.getString().equals("dcmAuditRecordRepository"))
            return false;

        reader.next();
        reader.expect(JsonParser.Event.START_OBJECT);
        AuditRecordRepository arr = new AuditRecordRepository();
        loadFrom(arr, reader, device.listConnections());
        device.addDeviceExtension(arr);
        reader.expect(JsonParser.Event.END_OBJECT);
        return true;
    }

    private void loadFrom(AuditRecordRepository arr, JsonReader reader, List<Connection> conns) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomInstalled":
                    arr.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        arr.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
    }
}
