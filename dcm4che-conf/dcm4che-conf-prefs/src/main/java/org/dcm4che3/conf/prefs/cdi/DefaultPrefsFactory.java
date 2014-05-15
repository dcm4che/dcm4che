package org.dcm4che3.conf.prefs.cdi;

import java.util.prefs.Preferences;

public class DefaultPrefsFactory implements PrefsFactory {

    public Preferences getPreferences() {
        return Preferences.systemRoot();
    }

    
}
