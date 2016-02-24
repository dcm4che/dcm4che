/*
 * *** BEGIN LICENSE BLOCK *****
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
 *  Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4che3.conf.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Roman K
 */
public class Path {

    public static final Path ROOT = new Path();

    final List<String> pathItems;
    private String simpleEscapeXPath;

    public Path() {
        pathItems = Collections.unmodifiableList(new ArrayList<String>());
    }

    public Path(String... pathItems) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String pathItem : pathItems) {
            strings.add(pathItem);
        }
        this.pathItems = Collections.unmodifiableList(strings);
    }

    public Path(List<String> pathItems) {
        this.pathItems = Collections.unmodifiableList(new ArrayList<String>(pathItems));
    }

    public List<String> getPathItems() {
        return pathItems;
    }

    public String toSimpleEscapedXPath() {
        if (simpleEscapeXPath != null)
            return simpleEscapeXPath;

        String xpath = "";
        for (String item : pathItems) xpath += "/" + item.replace("/", "\\/");
        simpleEscapeXPath = xpath;
        return xpath;
    }
}
