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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>CAUTION:</b>
 * List indexes in the path representation start with 0. When converted to XPath, 1 is added as in XPath indexes start with 1.
 *
 * @author Roman K
 */
public class Path implements Serializable {

    private static final long serialVersionUID = 1069976968612802603L;
    private static Pattern itemPattern = Pattern.compile("/(?<item>(\\\\/|[^/\\[\\]@\\*])*)");
    private static Pattern simplePathPattern = Pattern.compile("(" + itemPattern + ")*");

    public static final Path ROOT = new Path();

    private final List<Object> pathItems;


    private transient String simpleEscapedXPath;
    private transient String simpleEscapedPath;

    public Path() {
        pathItems = Collections.unmodifiableList(new ArrayList<Object>());
    }

    public Path(Object... pathItems) {

        ArrayList<Object> strings = new ArrayList<Object>(pathItems.length);
        Collections.addAll(strings, pathItems);
        this.pathItems = Collections.unmodifiableList(strings);

        validate();
    }

    public Path(List<?> pathItems) {
        this.pathItems = Collections.unmodifiableList(new ArrayList<Object>(pathItems));
        validate();
    }


    public Path(Iterator<Object> stringIterator) {
        ArrayList<Object> strings = new ArrayList<Object>();
        while (stringIterator.hasNext())
            strings.add(stringIterator.next());
        this.pathItems = Collections.unmodifiableList(strings);
        validate();
    }

    private void validate() {
        for (Object pathItem : this.pathItems) {
            if (!((pathItem instanceof String) || (pathItem instanceof Integer)))
                throw new IllegalArgumentException("Item '" + pathItem + "' is not allowed in path");
        }
    }


    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;
        if (!(obj instanceof Path))
            return false;

        return pathItems.equals(((Path) obj).pathItems);
    }

    public List<Object> getPathItems() {
        return pathItems;
    }

    /**
     * @param indexFrom inclusive
     * @param indexTo   NOT inclusive
     */
    public Path subPath(int indexFrom, int indexTo) {

        ArrayList<Object> newItems = new ArrayList<Object>();
        while (indexFrom < indexTo) {
            newItems.add(pathItems.get(indexFrom++));
        }

        return new Path(newItems);
    }

    public int size() {
        return getPathItems().size();
    }

    public String toSimpleEscapedXPath() {
        if (simpleEscapedXPath != null)
            return simpleEscapedXPath;

        String xpath = "";
        for (Object item : pathItems) {

            if (item instanceof String) {
                xpath += "/" + ((String) item).replace("/", "\\/");
            } else if (item instanceof Integer) {
                // XPath indexes START WITH 1
                xpath += "[" + ((Integer) item + 1) + "]";
            } else
                throw new RuntimeException("Unexpected error");

        }
        simpleEscapedXPath = xpath;
        return xpath;
    }

    public String toSimpleEscapedPath() {

        if (simpleEscapedPath != null)
            return simpleEscapedPath;

        String xpath = "";
        for (Object item : pathItems) {
            xpath += "/";

            if (item instanceof Integer) {
                xpath += "#";
            }

            xpath += item
                    .toString()
                    .replace("/", "\\/")
                    .replace("#", "\\#");
        }
        simpleEscapedPath = xpath;

        return xpath;
    }


    @Override
    public String toString() {
        return toSimpleEscapedXPath();
    }

    public static Path fromSimpleEscapedPath(String pathStr) {
        Path path = fromSimpleEscapedPathOrNull(pathStr);

        if (path == null)
            throw new IllegalArgumentException("Simple path " + pathStr + " is invalid");
        return path;
    }

    public static Path fromSimpleEscapedPathOrNull(String path) {

        if (!simplePathPattern.matcher(path).matches())
            return null;

        Matcher matcher = itemPattern.matcher(path);

        List<Object> list = new ArrayList<Object>();
        while (matcher.find()) {
            String item = matcher.group("item");

            if (item.startsWith("#")) {
                list.add(Integer.parseInt(item.substring(1)));
            } else {
                list.add(item
                        .replace("\\/", "/")
                        .replace("\\#", "#")
                );
            }

        }

        return new Path(list);
    }
}
