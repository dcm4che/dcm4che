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
 * Portions created by the Initial Developer are Copyright (C) 2014
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

package org.dcm4che3.conf.prefs.generic;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.DiffWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic Java Prefs ConfigurationExtension implementation that works for an arbitrary config class annotated 
 * with @ConfigClass and @ConfigField annotations. 
 * Config class must be provided both as a generic arg and as a constructor arg.  
 * @author Roman K
 *
 * @param <T> Config class
 */

public class PreferencesGenericConfigExtension<T extends DeviceExtension> extends PreferencesDicomConfigurationExtension {

	public static final Logger log = LoggerFactory.getLogger(PreferencesGenericConfigExtension.class);

	private String nodename;

	private Class<T> confClass;

	public PreferencesGenericConfigExtension(Class<T> confClass) throws ConfigurationException {
		super();

		this.confClass = confClass;

		ConfigClass ccAnno = (ConfigClass) confClass.getAnnotation(ConfigClass.class);

		// no annotation - no configuration
		if (ccAnno == null)
			throw new ConfigurationException("The configuration class must be annotated with @ConfigClass");

		if (ccAnno.nodeName().equals(""))
			throw new ConfigurationException(
					"To use java preferences config, specify node name for the config class in @ConfigClass annotation");

		nodename = ccAnno.nodeName();

	}

	@Override
	protected void storeChilds(Device device, Preferences deviceNode) {
		T confObj = device.getDeviceExtension(confClass);
		if (confObj != null)
			storeTo(confObj, deviceNode.node(nodename));
	}

	private void storeTo(T confObj, final Preferences prefs) {

		ConfigWriter prefsWriter = new PrefsConfigWriter(prefs);

		try {

			ReflectiveConfig.store(confObj, prefsWriter);

		} catch (Exception e) {
			log.error("Unable to store configuration!");
			log.error("{}", e);

		}

	}

	@Override
	protected void loadChilds(Device device, Preferences deviceNode) throws BackingStoreException, ConfigurationException {
		if (!deviceNode.nodeExists(nodename))
			return;

		Preferences loggerNode = deviceNode.node(nodename);
		T confObj;

		try {
			confObj = confClass.newInstance();
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

		loadFrom(confObj, loggerNode);
		device.addDeviceExtension(confObj);
	}

	private void loadFrom(T confObj, final Preferences prefs) {

		ConfigReader ldapReader = new PrefsConfigReader(prefs);

		try {

			ReflectiveConfig.read(confObj, ldapReader);

		} catch (Exception e) {
			log.error("Unable to read configuration!");
			log.error("{}", e);
		}

	}

	@Override
	protected void mergeChilds(Device prev, Device device, Preferences deviceNode) throws BackingStoreException {
		T prevConfObj = prev.getDeviceExtension(confClass);
		T confObj = device.getDeviceExtension(confClass);

		if (confObj == null && prevConfObj == null)
			return;

		Preferences xdsNode = deviceNode.node(nodename);
		if (confObj == null)
			xdsNode.removeNode();
		else if (prevConfObj == null)
			storeTo(confObj, xdsNode);
		else
			storeDiffs(xdsNode, prevConfObj, confObj);
	}

	private void storeDiffs(final Preferences prefs, T prevConfObj, T confObj) {

		DiffWriter prefsDiffWriter = new PrefsDiffWriter(prefs);

		try {

			ReflectiveConfig.storeAllDiffs(prevConfObj, confObj, prefsDiffWriter);

		} catch (Exception e) {
			log.error("Unable to store the diffs for configuration!");
			log.error("{}", e);
		}

	}
}
