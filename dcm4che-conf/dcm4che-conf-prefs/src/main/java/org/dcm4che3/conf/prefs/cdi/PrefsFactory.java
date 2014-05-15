package org.dcm4che3.conf.prefs.cdi;

import java.util.prefs.Preferences;

/**
 * To be used with CDI.
 * This interface will be injected into dcm4che components that use Preferences as a configuration backend. 
 * @author Roman K
 *
 */
public interface PrefsFactory {
    Preferences getPreferences();
}
