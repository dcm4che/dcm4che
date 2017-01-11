package org.dcm4che3.conf.api.upgrade;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        init(upgradeContext);

        if (firstTimeRun()) {
            Method firstRunMethod = getFirstRunMethod();
            log.info("Running upgrade script " + this.getClass().getName() + " for the first time - invoking method " + firstRunMethod.getName());
            invokeMethod(firstRunMethod);

        } else
            runFixUps(MMPVersion.fromStringVersion(getUpgradeContext().getUpgradeScriptMetadata().getLastVersionExecuted()));
    }

    public void init(UpgradeContext upgradeContext) {
        this.upgradeContext = upgradeContext;
    }

    private void runFixUps(MMPVersion lastExVer) {
        TreeMap<MMPVersion, Method> methods = getFixUpMethods(lastExVer);

        for (Map.Entry<MMPVersion, Method> methodEntry : methods.entrySet()) {
            log.info("[" + this.getClass().getName() + "] Invoking fix-up method " + methodEntry.getValue().getName() + " (fixUpTo " + methodEntry.getKey() + ")");
            invokeMethod(methodEntry.getValue());
        }
    }


    public Method getFirstRunMethod() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(UpgradeGoalFromScratch.class) != null) {
                return method;
            }
        }
        throw new RuntimeException("Method annotated with 'UpgradeGoalFromScratch' not found in " + this.getClass().getName());
    }

    public TreeMap<MMPVersion, Method> getFixUpMethods(MMPVersion lastExVer) {
        // Fix-ups are ran ordered according to @FixUpTo's values thus TreeMap
        TreeMap<MMPVersion, Method> methods = new TreeMap<MMPVersion, Method>();

        for (Method method : this.getClass().getDeclaredMethods()) {
            FixUpTo annotation = method.getAnnotation(FixUpTo.class);

            if (annotation != null) {

                MMPVersion mmpVersion = MMPVersion.fromFixUpToAnno(annotation);

                if (mmpVersion.compareTo(lastExVer) <= 0)
                    methods.put(mmpVersion, method);
            }
        }
        return methods;
    }

    private void invokeMethod(Method m) {
        try {
            m.invoke(this);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke a method of an upgrade script " + this.getClass().getName() + " . " + m.getName(), e);
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
