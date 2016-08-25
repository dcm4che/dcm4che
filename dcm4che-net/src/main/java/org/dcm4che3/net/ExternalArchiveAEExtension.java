
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

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.AEExtension;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@LDAP(objectClasses = "dcmExternalArchiveAEExtension", noContainerNode = true)
@ConfigurableClass
public class ExternalArchiveAEExtension extends AEExtension {

    private static final long serialVersionUID = -2390448404282661045L;

    @ConfigurableProperty(name = "dcmAEFetchPriority")
    private int aeFetchPriority;

    @ConfigurableProperty(name = "dcmAEPrefersForwarding", defaultValue="false")
    private boolean prefersForwarding = false;

    @ConfigurableProperty(name = "dcmLinkedQueryAETs",
            description = "List of AETs linked to this (tipically store) AET and to be used for query/retrieve",
            collectionOfReferences = true)
    private Collection<ApplicationEntity> linkedQueryAETs = new ArrayList<ApplicationEntity>();

    @ConfigurableProperty(name = "dcmDefaultForStorage", defaultValue="false")
    private boolean defaultForStorage = false;

    public int getAeFetchPriority() {
        return aeFetchPriority;
    }
    
    public void setAeFetchPriority(int aeFetchPriority) {
        this.aeFetchPriority = aeFetchPriority;
    }

    public boolean isPrefersForwarding() {
        return prefersForwarding;
    }

    public void setPrefersForwarding(boolean prefersForwarding) {
        this.prefersForwarding = prefersForwarding;
    }

    public Collection<ApplicationEntity> getLinkedQueryAETs() {
        return linkedQueryAETs;
    }

    public void setLinkedQueryAETs(Collection<ApplicationEntity> linkedQueryAETs) {
        this.linkedQueryAETs = linkedQueryAETs;
    }

    public void addIgnoreSeriesStudyMissmatchErrorsAET(ApplicationEntity ae) {
        this.linkedQueryAETs.add(ae);
    }

    public boolean removeIgnoreSeriesStudyMissmatchErrorsAET(ApplicationEntity ae) {
        return this.linkedQueryAETs.remove(ae);
    }

    public boolean isDefaultForStorage() {
        return defaultForStorage;
    }

    public void setDefaultForStorage(boolean defaultForStorage) {
        this.defaultForStorage = defaultForStorage;
    }
}
