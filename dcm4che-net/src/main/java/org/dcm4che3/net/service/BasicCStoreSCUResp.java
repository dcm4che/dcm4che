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

package org.dcm4che3.net.service;

import org.dcm4che3.net.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class BasicCStoreSCUResp {

    private int status;
    private int warning;
    private Set<String> completedUIDs = new HashSet<>();
    private Set<String> failedUIDs = new HashSet<>();
    private List<Exception> errors = new ArrayList<>();

    public BasicCStoreSCUResp() { }

    /**
     * This creates a shallow copy of the BasicCStoreSCUResp referenced by
     * other.
     * @param other The BasicCStoreSCUResp to be copied.
     */
    protected BasicCStoreSCUResp(BasicCStoreSCUResp other) {
        this.status = other.status;
        this.warning = other.warning;
        this.completedUIDs = other.completedUIDs;
        this.failedUIDs = other.failedUIDs;
        this.errors = other.errors;
    }
   
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getCompleted() {
        return completedUIDs.size();
    }
    public int getFailed() {
        return failedUIDs.size();
    }
    public int getWarning() {
        return warning;
    }
    public void setWarning(int warning) {
        this.warning = warning;
    }
    public Set<String> getCompletedUIDs() {
        return Collections.unmodifiableSet(this.completedUIDs);
    }
    public void addCompletedUIDs(Collection<String> completedUIDs) {
        this.completedUIDs.addAll(completedUIDs);
    }
    public Set<String> getFailedUIDs() {
        return Collections.unmodifiableSet(this.failedUIDs);
    }
    public void addFailedUIDs(Collection<String> failedUIDs) {
        this.failedUIDs.addAll(failedUIDs);
    }
    public List<Exception> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    public Optional<Exception> getLastError() {
        return errors.stream().skip(Math.max(0, errors.size() - 1)).findFirst();
    }
    public void addError(Exception error) {
        this.errors.add(error);
    }

    /**
     * Extends the current response object with the results from the
     * addendumResponse.
     * @param addendumResponse the response object to use for extension.
     */
    public void extendResponse(BasicCStoreSCUResp addendumResponse) {
        if (getStatus() != addendumResponse.getStatus()) {
            setStatus(Status.OneOrMoreFailures);
        }
        errors.addAll(addendumResponse.getErrors());
        setWarning(getWarning() + addendumResponse.getWarning());
        addCompletedUIDs(addendumResponse.getCompletedUIDs());
        addFailedUIDs(addendumResponse.getFailedUIDs());
    }
}
