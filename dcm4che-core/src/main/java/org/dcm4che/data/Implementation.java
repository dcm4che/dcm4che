package org.dcm4che.data;

public class Implementation {

    private static final String IMPL_CLASS_UID = "1.2.40.0.13.1.1";
    private static final String IMPL_VERS_NAME = versionName();
    private static String versionName() {
        StringBuilder sb = new StringBuilder(16);
        sb.append("dcm4che-");
        sb.append(Implementation.class.getPackage()
                .getImplementationVersion());
        return sb.substring(0, Math.min(16, sb.length()));
    }

    public static String getClassUID() {
        return IMPL_CLASS_UID;
    }

    public static String getVersionName() {
        return IMPL_VERS_NAME;
    }

}
