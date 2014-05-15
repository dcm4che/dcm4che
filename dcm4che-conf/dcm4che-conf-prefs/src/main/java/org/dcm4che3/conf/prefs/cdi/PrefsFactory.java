package org.dcm4che3.conf.prefs.cdi;

import java.util.prefs.Preferences;

/**
 * To be used with CDI. The default implementation is org.dcm4che3.conf.prefs.cdi.DefaultPrefsFactory.java
 * @author Roman K
 *
 */
public interface PrefsFactory {
    Preferences getPreferences();
}
