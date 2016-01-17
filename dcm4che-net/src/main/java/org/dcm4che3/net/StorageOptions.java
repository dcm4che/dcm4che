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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.net;

import java.io.Serializable;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.pdu.ExtendedNegotiation;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@LDAP
@ConfigurableClass
public class StorageOptions implements Serializable {

    private static final long serialVersionUID = 6911502883119290413L;

    public enum LevelOfSupport {
        LEVEL_0, LEVEL_1, LEVEL_2, UNSPECIFIED;

        public static LevelOfSupport valueOf(int level) {
            switch(level) {
            case 0:
                return LEVEL_0;
            case 1:
                return LEVEL_1;
            case 2:
                return LEVEL_2;
            }
            return UNSPECIFIED;
        }
    };

    public enum DigitalSignatureSupport {
        UNSPECIFIED, LEVEL_1, LEVEL_2, LEVEL_3;

        public static DigitalSignatureSupport valueOf(int level) {
            switch(level) {
            case 1:
                return LEVEL_1;
            case 2:
                return LEVEL_2;
            case 3:
                return LEVEL_3;
            }
            return UNSPECIFIED;
        }
    };

    public enum ElementCoercion {
        NO, YES, UNSPECIFIED;

        public static ElementCoercion valueOf(int i) {
            switch(i) {
            case 0:
                return NO;
            case 1:
                return YES;
            }
            return UNSPECIFIED;
        }
    };

    @ConfigurableProperty(name="dcmStorageConformance",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "3"
    )
    private LevelOfSupport levelOfSupport = LevelOfSupport.UNSPECIFIED;

    @ConfigurableProperty(name="dcmDigitalSignatureSupport",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "0"
    )
    private DigitalSignatureSupport digitalSignatureSupport = DigitalSignatureSupport.UNSPECIFIED;

    @ConfigurableProperty(name="dcmDataElementCoercion",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "2"
    )
    private ElementCoercion elementCoercion = ElementCoercion.UNSPECIFIED;

    public StorageOptions() {
        this(LevelOfSupport.UNSPECIFIED, 
             DigitalSignatureSupport.UNSPECIFIED,
             ElementCoercion.UNSPECIFIED);
    }

    public StorageOptions(LevelOfSupport levelOfSupport,
            DigitalSignatureSupport levelOfDigitalSignatureSupport, 
            ElementCoercion getElementCoercion) {
        this.levelOfSupport = levelOfSupport;
        this.digitalSignatureSupport = levelOfDigitalSignatureSupport;
        this.elementCoercion = getElementCoercion;
    }

    public final LevelOfSupport getLevelOfSupport() {
        return levelOfSupport;
    }

    public final void setLevelOfSupport(LevelOfSupport levelOfSupport) {
        this.levelOfSupport = levelOfSupport;
    }

    public final DigitalSignatureSupport getDigitalSignatureSupport() {
        return digitalSignatureSupport;
    }

    public final void setDigitalSignatureSupport(
            DigitalSignatureSupport digitalSignatureSupport) {
        this.digitalSignatureSupport = digitalSignatureSupport;
    }

    public final ElementCoercion getElementCoercion() {
        return elementCoercion;
    }

    public final void setElementCoercion(ElementCoercion elementCoercion) {
        this.elementCoercion = elementCoercion;
    }

    public byte[] toExtendedNegotiationInformation() {
         return new byte[] {
                 (byte) levelOfSupport.ordinal(), 0,
                 (byte) digitalSignatureSupport.ordinal(), 0,
                 (byte) elementCoercion.ordinal(), 0 };
    }

    public static StorageOptions valueOf(ExtendedNegotiation extNeg) {
        return new StorageOptions(
                        LevelOfSupport.valueOf(extNeg.getField(0, (byte) 3)),
                        DigitalSignatureSupport.valueOf(extNeg.getField(2, (byte) 0)),
                        ElementCoercion.valueOf(extNeg.getField(4, (byte) 2)));
    }

    @Override
    public int hashCode() {
        return levelOfSupport.hashCode()
            + digitalSignatureSupport.hashCode()
            + elementCoercion.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof StorageOptions))
            return false;

        StorageOptions other = (StorageOptions) o; 
        return levelOfSupport == other.levelOfSupport
            && digitalSignatureSupport == other.digitalSignatureSupport
            &&  elementCoercion == other.elementCoercion;
    }

    @Override
    public String toString() {
        return "StorageOptions[levelOfSupport=" + levelOfSupport.ordinal()
                + ", digitalSignatureSupport=" + digitalSignatureSupport.ordinal()
                + ", elementCoercion=" + elementCoercion.ordinal() + "]";
    }

    public StorageOptions copy() {
        return new StorageOptions(getLevelOfSupport(),getDigitalSignatureSupport(),getElementCoercion());
    }
}
