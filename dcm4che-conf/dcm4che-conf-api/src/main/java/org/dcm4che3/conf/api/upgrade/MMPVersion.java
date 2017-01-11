package org.dcm4che3.conf.api.upgrade;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Major-minor-patch version
 */
public class MMPVersion implements Comparable<MMPVersion> {

    /**
     * Matches both 8.2.1-02 and 8.1-33
     * ignores the .1 in  8.2.1-02
     */
    private static Pattern stringVersionFormat = Pattern.compile("([0-9]+)\\.([0-9]+)(\\.[0-9]+)?(-([0-9]+))?");
    //  "(?<major>[0-9]+)\\.(?<minor>[0-9]+)(\\.([0-9]+))?-(?<patch>[0-9]+)");

    private int major;
    private int minor;
    private int patch;


    public static MMPVersion fromFixUpToAnno(FixUpTo anno) throws IllegalArgumentException {
        return fromAnnotatedVersion(anno.value(), anno.major(), anno.minor(), anno.patch());
    }

    public static MMPVersion fromScriptVersionAnno(ScriptVersion anno) throws IllegalArgumentException {
        return fromAnnotatedVersion(anno.value(), anno.major(), anno.minor(), anno.patch());
    }

    public static MMPVersion fromStringVersion(String value) throws IllegalArgumentException {
        MMPVersion mmpVersion = new MMPVersion();
        Matcher matcher = stringVersionFormat.matcher(value);
        if (!matcher.matches())
            throw new IllegalArgumentException("Unexpected version format: " + value);

        mmpVersion.major = Integer.parseInt(matcher.group(1));
        mmpVersion.minor = Integer.parseInt(matcher.group(2));
        String group = matcher.group(5);
        if (group != null) {
            mmpVersion.patch = Integer.parseInt(group);
        }
        return mmpVersion;
    }

    private static MMPVersion fromAnnotatedVersion(String value, int major, int minor, int patch) {
        if (value.isEmpty()) {
            MMPVersion mmpVersion = new MMPVersion();
            mmpVersion.major = major;
            mmpVersion.minor = minor;
            mmpVersion.patch = patch;

            if (mmpVersion.major < 0 || mmpVersion.minor < 0 || mmpVersion.patch < 0)
                throw new IllegalArgumentException("Major, minor, and patch values of a version must not be negative - violated by " + mmpVersion);
            return mmpVersion;

        } else {
            return fromStringVersion(value);
        }
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "-" + patch;
    }

    @Override
    public int compareTo(MMPVersion otherVersion) {
        if (major < otherVersion.major) return -1;
        if (major > otherVersion.major) return 1;
        if (minor < otherVersion.minor) return -1;
        if (minor > otherVersion.minor) return 1;
        if (patch < otherVersion.patch) return -1;
        if (patch > otherVersion.patch) return 1;
        return 0;
    }

}
