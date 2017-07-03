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
 * Java(TM), hosted at https://github.com/dcm4che/dcm4che3.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.net.audit;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DeviceExtension;
import java.util.*;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 */
public class AuditLoggerDeviceExtension extends DeviceExtension {

    private final LinkedHashMap<String, AuditLogger> loggers =
            new LinkedHashMap<String, AuditLogger>();

    public void addAuditLogger(AuditLogger logger) {
        logger.setDevice(device);
        loggers.put(logger.getCommonName(), logger);
    }

    public AuditLogger removeAuditLogger(String name) {
        AuditLogger logger = loggers.remove(name);
        if (logger != null)
            logger.setDevice(null);
        return logger;
    }

    @Override
    public void verifyNotUsed(Connection conn) {
        for (AuditLogger logger : loggers.values())
            if (logger.getConnections().contains(conn))
                throw new IllegalStateException(conn + " used by Audit Logger" + logger.getCommonName());
    }

    @Override
    public void reconfigure(DeviceExtension from)  {
        reconfigure((AuditLoggerDeviceExtension) from);
    }

    private void reconfigure(AuditLoggerDeviceExtension from) {
        loggers.keySet().retainAll(from.loggers.keySet());
        for (AuditLogger src : from.loggers.values()) {
            AuditLogger logger = loggers.get(src.getApplicationName());
            if (logger == null)
                addAuditLogger(logger = new AuditLogger(src.getCommonName()));
            logger.reconfigure(src);
        }
    }

    public Collection<AuditLogger> getAuditLoggers() {
        return loggers.values();
    }

    public boolean containsAuditLogger(String name) {
        return loggers.containsKey(name);
    }

    public AuditLogger getAuditLogger(String name) {
        return loggers.get(name);
    }

    public Collection<String> getAuditLoggerNames() {
        return loggers.keySet();
    }
}
