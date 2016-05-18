package org.dcm4che3.conf.api.upgrade;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class VersionDrivenUpgradeScript implements UpgradeScript {

    private static Logger log = LoggerFactory.getLogger(VersionDrivenUpgradeScript.class);


    private UpgradeContext upgradeContext;

    protected UpgradeContext getUpgradeContext() {
        return upgradeContext;
    }

    @Override
    public void upgrade(UpgradeContext upgradeContext) throws ConfigurationException {

        this.upgradeContext = upgradeContext;

        if (firstTimeRun()) {
            runUpgradeGoalFromScratch();
        } else
            runFixUps();
    }

    private void runUpgradeGoalFromScratch() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(UpgradeGoalFromScratch.class) != null) {
                log.info("Running upgrade script "+this.getClass().getName()+" for the first time - invoking method " + method.getName());
                invokeMethod(method);
                return;
            }
        }

        throw new RuntimeException("Method annotated with 'UpgradeGoalFromScratch' not found in " + this.getClass().getName());
    }

    private void invokeMethod(Method m) {
        try {
            m.invoke(this);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke a method of an upgrade script " + this.getClass().getName() + " . " + m.getName(), e);
        }
    }

    private void runFixUps() {

        // Fix-ups are ran in ascending lexicographic order of the @FixUpTo's values (i.e. in the same order they were added), thus TreeMap
        TreeMap<String, Method> methods = new TreeMap<String, Method>();

        for (Method method : this.getClass().getDeclaredMethods()) {
            FixUpTo annotation = method.getAnnotation(FixUpTo.class);

            if (annotation != null) {
                String value = annotation.value();

                if (value == null)
                    throw new IllegalArgumentException("value must not be null in annotation FixUpTo on method " + method.getName());

                if (getUpgradeContext().getUpgradeScriptMetadata().getLastVersionExecuted().compareTo(value)<=0)
                    methods.put(value, method);
            }
        }

        for (Map.Entry<String, Method> methodEntry : methods.entrySet()) {
            log.info("["+this.getClass().getName()+"] Invoking fix-up method "+methodEntry.getValue().getName()+" (fixUpTo "+methodEntry.getKey()+")");
            invokeMethod(methodEntry.getValue());
        }
    }

    private boolean firstTimeRun() {
        try {
            return getUpgradeContext().getUpgradeScriptMetadata().getLastVersionExecuted().equals(NO_VERSION);
        } catch (NullPointerException e) {
            return true;
        }
    }

}
