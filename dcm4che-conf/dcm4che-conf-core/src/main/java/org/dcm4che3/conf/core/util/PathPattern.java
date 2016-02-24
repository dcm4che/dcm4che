/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that allows to safely set parameters for xpath patterns (by proper escaping) and to retrieve them from an escaped form
 * ex: dicomConfigurationRoot/dicomDevicesRoot/*[dicomNetworkAE[@name='{aeName}']]/dicomDeviceName
 */
public class PathPattern {

    String pattern;
    private Pattern compiledPattern;

    public PathPattern(String pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile(getParseRegex(pattern));
    }

    /**
     *
     * Converts
     * <pre>
     * /dicomConfigurationRoot/dicomDevicesRoot[@name='{deviceName}']
     * </pre>
     * to
     * <pre>
     * \Q/dicomConfigurationRoot/dicomDevicesRoot[@name='\E(?&lt;deviceName&gt;.*)\Q']\E
     * </pre>
     * @param pattern
     * @return
     */
    private String getParseRegex(String pattern) {
        String p = pattern;
        String res = "";
        try {
            while (p.indexOf("{") > -1) {

                // quote before
                String piece = p.substring(0, p.indexOf("{"));
                if (!piece.equals(""))
                    res += Pattern.quote(piece);
                p = p.substring(p.indexOf("{") + 1);

                // add group
                String varName = p.substring(0, p.indexOf("}"));
                res += "(?<" + varName + ">.*)";

                p = p.substring(p.indexOf("}") + 1);
            }
            // add rest if any
            if (!p.equals("")) {
                res += Pattern.quote(p);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Path pattern " + pattern + " is invalid", e);
        }
        return res;
    }

    /**
     * quick-start chaining
     *
     * @param paramName
     * @param value
     * @return
     */
    public PathCreator set(String paramName, String value) {
        return createPath().set(paramName, value);
    }

    public String path() {
        return pattern;
    }


    public PathCreator createPath() {
        return new PathCreator();
    }

    public PathParser parse(String path) {

        return new PathParser(path);
    }

    public PathParser parseIfMatches(String path) {
        Matcher matcher = compiledPattern.matcher(path);

        if (matcher.matches())
            return new PathParser(matcher);
        else
            return null;

    }

    public class PathParser {
        private final Matcher matcher;

        public PathParser(Matcher matcher) {
            this.matcher = matcher;
        }

        public PathParser(String path) {
            matcher = compiledPattern.matcher(path);
            if (!matcher.matches()) throw new IllegalArgumentException("Path " + path + " is invalid");
        }

        public String getParam(String paramName) {
            String str = matcher.group(paramName);
            return str.replace("&apos;", "'");
        }
    }

    public class PathCreator {

        String res;

        public PathCreator() {
            res = pattern;
        }

        public PathCreator set(String paramName, String value) {
            if (!res.contains(paramName))
                throw new IllegalArgumentException("No parameter " + paramName + " in path " + res);
            if (value == null)
                throw new IllegalArgumentException("Attempted to set parameter " + paramName + " to NULL in path " + res);
            res = res.replace("{" + paramName + "}", value.replace("'", "&apos;"));
            return this;
        }

        public String path() {
            return res;
        }
    }
}
