package org.dcm4che3.conf.api.upgrade;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Major-minor-revision-patch version
 */
public class NumericVersion implements Comparable<NumericVersion> {

    /**
     * All components must be provided
     * 8.1-33 no match since revision is missing
     * 8.1.1 no match since patch is missing
     * 8.1.1-0 match
     */
    private static Pattern stringVersionFormat = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)-([0-9]+)");
    //  "(?<major>[0-9]+)\\.(?<minor>[0-9]+)(\\.([0-9]+))?-(?<patch>[0-9]+)");

    private int major;
    private int minor;
    private int revision;
    private int patch;


    public static NumericVersion fromFixUpToAnno(FixUpTo anno) throws IllegalArgumentException {
        return fromStringVersion(anno.value());
    }

    public static NumericVersion fromScriptVersionAnno(ScriptVersion anno) throws IllegalArgumentException {
        return fromStringVersion(anno.value());
    }

    public static NumericVersion fromStringVersion(String value) throws IllegalArgumentException {

        NumericVersion numericVersion = new NumericVersion();

        Matcher matcher = stringVersionFormat.matcher(value);

        if (!matcher.matches())
            throw new IllegalArgumentException("Unexpected version format: " + value);

        numericVersion.major = Integer.parseInt(matcher.group(1));
        numericVersion.minor = Integer.parseInt(matcher.group(2));
        numericVersion.revision = Integer.parseInt(matcher.group(3));
        numericVersion.patch = Integer.parseInt(matcher.group(4));

        return numericVersion;
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

    public int getRevision()
    {
        return revision;
    }

    public void setRevision( int revision )
    {
        this.revision = revision;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + revision + "-" + patch;
    }

    @Override
    public int compareTo(NumericVersion otherVersion) {
        if (major < otherVersion.major) return -1;
        if (major > otherVersion.major) return 1;
        if (minor < otherVersion.minor) return -1;
        if (minor > otherVersion.minor) return 1;
        if (revision < otherVersion.revision) return -1;
        if (revision > otherVersion.revision) return 1;
        if (patch < otherVersion.patch) return -1;
        if (patch > otherVersion.patch) return 1;
        return 0;
    }

}
