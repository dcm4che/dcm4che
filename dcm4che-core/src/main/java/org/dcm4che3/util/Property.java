/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Property implements Serializable {

    private static final long serialVersionUID = 6618989493749845502L;

    private final String name;
    private final Object value;

    public Property(String name, Object value) {
        if (name == null)
            throw new NullPointerException("name");
        if (value == null)
            throw new NullPointerException("value");

        if (!(value instanceof String
           || value instanceof Boolean
           || value instanceof Number))
            throw new IllegalArgumentException("value: " + value.getClass());

        this.name = name;
        this.value = value;
    }

    public Property(String s) {
        int endParamName = s.indexOf('=');
        name = s.substring(0, endParamName);
        value = valueOf(s.substring(endParamName+1));
    }

    public final String getName() {
        return name;
    }

    public final Object getValue() {
        return value;
    }

    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE :
                  s.equalsIgnoreCase("false") ? Boolean.FALSE
                                              : s;
        }
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Property other = (Property) obj;
        return name.equals(other.name) 
           && value.equals(other.value);
    }

    @Override
    public String toString() {
        return name + '=' + value;
    }

    public void setAt(Object o) {
        String setterName = "set" 
                + name.substring(0,1).toUpperCase(Locale.ENGLISH)
                + name.substring(1);
        try {
            Class<?> clazz = o.getClass();
            if (value instanceof String) {
                clazz.getMethod(setterName, String.class).invoke(o, value);
            } else if (value instanceof Boolean) {
                clazz.getMethod(setterName, boolean.class).invoke(o, value);
            } else { // value instanceof Number
                try {
                    clazz.getMethod(setterName, double.class)
                        .invoke(o, ((Number) value).doubleValue());
                } catch (NoSuchMethodException e) {
                    try {
                        clazz.getMethod(setterName, float.class)
                            .invoke(o, ((Number) value).floatValue());
                    } catch (NoSuchMethodException e2) {
                        try {
                            clazz.getMethod(setterName, int.class)
                                .invoke(o, ((Number) value).intValue());
                        } catch (NoSuchMethodException e3) {
                            throw e;
                        }
                   }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props)
            if (prop.name.equals(name))
                return (T) prop.value;
        return defVal;
    }
}
