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

package org.dcm4che3.net.audit;

import java.util.List;

import org.dcm4che3.audit.ActiveParticipant;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventIdentification;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Code;

/**
 * Specifies criteria for {@link EventIdentification} and optional also for
 * {@link ActiveParticipant}s of {@link AuditMessage}s which shall be suppressed.
 *
 * Only Audit Messages which match all specified criteria will be suppressed.
 * 
 * Audit Messages without any {@code ActiveParticipant} will only match an
 * {@code AuditSuppressCriteria} with does not specify criteria for
 * {@code ActiveParticipant}s. 
 * 
 * Audit Messages with multiple {@code ActiveParticipants} will match if one
 * of the {@code ActiveParticipants} match all {@code ActiveParticipant}
 * specific criteria.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@LDAP(objectClasses = "dcmAuditSuppressCriteria")
@ConfigurableClass
public class AuditSuppressCriteria {

    @ConfigurableProperty(name="cn")
    private String commonName;

    @ConfigurableProperty(name="dcmAuditEventID")
    private EventID[] eventIDs = {};

    @ConfigurableProperty(name="dcmAuditEventTypeCode")
    private EventTypeCode[] eventTypeCodes = {};

    @ConfigurableProperty(name="dcmAuditEventActionCode")
    private String eventActionCodes[] = {};

    @ConfigurableProperty(name="dcmAuditEventOutcomeIndicator")
    private String[] eventOutcomeIndicators = {};

    @ConfigurableProperty(name="dcmAuditUserID")
    private String[] userIDs = {};

    @ConfigurableProperty(name="dcmAuditAlternativeUserID")
    private String[] alternativeUserIDs = {};

    @ConfigurableProperty(name="dcmAuditUserRoleIDCode")
    private RoleIDCode[] roleIDCodes = {};

    @ConfigurableProperty(name="dcmAuditNetworkAccessPointID")
    private String[] networkAccessPointIDs = {};

    @ConfigurableProperty(name="dcmAuditUserIsRequestor")
    private Boolean userIsRequestor;

    public AuditSuppressCriteria() {
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public AuditSuppressCriteria(String cn) {
        if (cn.isEmpty())
            throw new IllegalArgumentException("cn must not be empty");
        this.commonName = cn;
    }

    public String getCommonName() {
        return commonName;
    }

    public EventID[] getEventIDs() {
        return eventIDs;
    }

    /** Specifies values of {@link EventID} of the {@link EventIdentification}
     * of messages which shall be suppressed.
     * 
     * If no value is specified, the {@link EventID} of the {@link EventIdentification}
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * @param eventIDs values of {@code EventID} of the {@code EventIdentification}
     * of messages which shall be suppressed. 
     */
    public void setEventIDs(EventID... eventIDs) {
        this.eventIDs = eventIDs;
    }

    public String[] getEventIDsAsStringArray() {
        return toStringArray(eventIDs);
    }

    public void setEventIDsAsStringArray(String[] ss) {
        setEventIDs(toEventIDArray(ss));
    }

    public EventTypeCode[] getEventTypeCodes() {
        return eventTypeCodes;
    }

    /** Specifies values of {@link EventTypeCode} of the {@link EventIdentification}
     * of messages which shall be suppressed.
     * 
     * If no value is specified, the {@link EventTypeCode} of the {@link EventIdentification}
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * If values are specified, Audit Messages without {@code EventTypeCode}s
     * will not match this {@code AuditSuppressCriteria}.
     * 
     * Audit Messages with multiple {@code EventTypeCode}s will match if one
     * of the {@code EventTypeCode} match one of the specified values.
     * 
     * @param eventTypeCodes values of {@link EventTypeCode} of the {@link EventIdentification}
     * of messages which shall be suppressed. 
     */
    public void setEventTypeCodes(EventTypeCode... eventTypeCodes) {
        this.eventTypeCodes = eventTypeCodes;
    }

    public String[] getEventTypeCodesAsStringArray() {
        return toStringArray(eventTypeCodes);
    }

    public void setEventTypeCodesAsStringArray(String... ss) {
        setEventTypeCodes(toEventTypeCodeArray(ss));
    }

    public String[] getEventActionCodes() {
        return eventActionCodes;
    }

    /** Specifies values of {@code EventActionCode} of the {@link EventIdentification}
     * of messages which shall be suppressed.
     * 
     * If no value is specified, the {@code EventActionCode} of the {@link EventIdentification}
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * If values are specified, Audit Messages without {@code EventActionCode}
     * will not match this {@code AuditSuppressCriteria}.
     * 
     * @param eventActionCodes values of {@code EventActionCode} of the {@link EventIdentification}
     * of messages which shall be suppressed. 
     */
    public void setEventActionCodes(String... eventActionCodes) {
        this.eventActionCodes = eventActionCodes;
    }

    public String[] getEventOutcomeIndicators() {
        return eventOutcomeIndicators;
    }

    /** Specifies values of {@code EventOutcomeIndicator} of the {@link EventIdentification}
     * of messages which shall be suppressed.
     * 
     * If no value is specified, the {@code EventOutcomeIndicator} of the {@link EventIdentification}
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * @param eventOutcomeIndicators values of {@code EventOutcomeIndicator} of the {@link EventIdentification}
     * of messages which shall be suppressed. 
     */
    public void setEventOutcomeIndicators(String... eventOutcomeIndicators) {
        this.eventOutcomeIndicators = eventOutcomeIndicators;
    }

    public String[] getUserIDs() {
        return userIDs;
    }

    /** Specifies values of {@code UserID} of {@link ActiveParticipant}s of
     * messages which shall be suppressed.
     * 
     * If no value is specified, the {@code UserID} of {@link ActiveParticipant}s
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * @param userIDs values of {@code UserID} of the {@link ActiveParticipant}s
     * of messages which shall be suppressed. 
     */
    public void setUserIDs(String... userIDs) {
        this.userIDs = userIDs;
    }

    public String[] getAlternativeUserIDs() {
        return alternativeUserIDs;
    }

    /** Specifies values of {@code AlternativeUserID} of {@link ActiveParticipant}s of
     * messages which shall be suppressed.
     * 
     * {@code ActiveParticipant}s with multiple {@code EventTypeCode}s will match if one
     * of the {@code EventTypeCode} match one of the specified values.
     *
     * If no value is specified, the {@code AlternativeUserID} of {@code ActiveParticipant}s
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * If values are specified,  {@code ActiveParticipant}s without {@code AlternativeUserID}
     * will not match this {@code AuditSuppressCriteria}.
     *
     * @param altUserID values of {@code AlternativeUserID} of the {@link ActiveParticipant}s
     * of messages which shall be suppressed. 
     */
    public void setAlternativeUserIDs(String... altUserID) {
        this.alternativeUserIDs = altUserID;
    }

    public RoleIDCode[] getUserRoleIDCodes() {
        return roleIDCodes;
    }

    /** Specifies values of {@link RoleIDCode} of {@link ActiveParticipant}s of
     * messages which shall be suppressed.
     * 
     * If no value is specified, the {@code RoleIDCode} of {@link ActiveParticipant}s
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * If values are specified,  {@code ActiveParticipant}s without {@code RoleIDCode}s
     * will not match this {@code AuditSuppressCriteria}.
     *
     * {@code ActiveParticipant}s with multiple {@code RoleIDCode}s will match if one
     * of the {@code RoleIDCode} match one of the specified values.
     * 
     * @param roleIDCodes values of {@code RoleIDCode} of the {@link ActiveParticipant}s
     * of messages which shall be suppressed. 
     */
    public void setUserRoleIDCodes(RoleIDCode... roleIDCodes) {
        this.roleIDCodes = roleIDCodes;
    }

    public String[] getUserRoleIDCodesAsStringArray() {
        return toStringArray(roleIDCodes);
    }

    public void setUserRoleIDCodesAsStringArray(String... ss) {
        setUserRoleIDCodes(toRoleIDCodeArray(ss));
    }

    public String[] getNetworkAccessPointIDs() {
        return networkAccessPointIDs;
    }

    /** Specifies values of {@code NetworkAccessPointID} of {@link ActiveParticipant}s of
     * messages which shall be suppressed.
     * 
     * If no value is specified, the {@code NetworkAccessPointID} of {@link ActiveParticipant}s
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * If values are specified,  {@code ActiveParticipant}s without {@code NetworkAccessPointID}s
     * will not match this {@code AuditSuppressCriteria}.
     *
     * @param networkAccessPointIDs values of {@code NetworkAccessPointID} of the {@link ActiveParticipant}s
     * of messages which shall be suppressed. 
     */
    public void setNetworkAccessPointIDs(String... networkAccessPointIDs) {
        this.networkAccessPointIDs = networkAccessPointIDs;
    }

    public Boolean getUserIsRequestor() {
        return userIsRequestor;
    }

    /** Specifies value of {@code UserIsRequestor} of {@link ActiveParticipant}s of
     * messages which shall be suppressed.
     * 
     * If {@code null} is specified, the value of {@code UserIsRequestor} of {@link ActiveParticipant}s
     * of a message will not be concerned by this {@code AuditSuppressCriteria}.
     * 
     * @param userIsRequestor value of {@code UserIsRequestor} of the {@link ActiveParticipant}s
     * of messages which shall be suppressed or {@code null}. 
     */
    public void setUserIsRequestor(Boolean userIsRequestor) {
        this.userIsRequestor = userIsRequestor;
    }

    public boolean match(AuditMessage msg) {
        if (!match(msg.getEventIdentification()))
            return false;

        if (!match(msg.getActiveParticipant()))
            return false;

        return true;
    }

    private boolean match(EventIdentification eventIdentification) {
        if (!matchEventID(eventIdentification.getEventID()))
            return false;

        if (!matchEventTypeCodes(eventIdentification.getEventTypeCode()))
            return false;

        if (!isEmptyOrContains(eventActionCodes,
                        eventIdentification.getEventActionCode()))
            return false;

        if (!isEmptyOrContains(eventOutcomeIndicators,
                        eventIdentification.getEventOutcomeIndicator()))
            return false;

        return true;
    }

    private boolean matchEventID(EventID o) {
        if (eventIDs.length == 0)
            return true;

        for (EventID eventyID : eventIDs) {
            if (eventyID.getCode().equals(o.getCode())
                 && equals(eventyID.getCodeSystemName(), o.getCodeSystemName()))
                return true;
        }
        return false;
    }

    private boolean equals(Object o1, Object o2) {
        return o1 != null ? o1.equals(o2) : o2 == null;
    }

    private boolean matchEventTypeCodes(List<EventTypeCode> list) {
        if (eventTypeCodes.length == 0)
            return true;

        for (EventTypeCode o : list) {
            for (EventTypeCode eventTypeCode : eventTypeCodes) {
                if (eventTypeCode.getCode().equals(o.getCode())
                     && equals(eventTypeCode.getCodeSystemName(), o.getCodeSystemName()))
                    return true;
            }
        }

        return false;
    }

    private boolean match(List<ActiveParticipant> aps) {
        if (!containsActiveParticipantCriteria())
            return true;

        for (ActiveParticipant ap : aps) {
            if (match(ap))
                return true;
        }
        return false;
    }

    private boolean match(ActiveParticipant ap) {
        if (!isEmptyOrContains(userIDs, ap.getUserID()))
            return false;

        if (!isEmptyOrContains(alternativeUserIDs,
                ap.getAlternativeUserID()))
            return false;

        if (!isEmptyOrContains(networkAccessPointIDs, 
                ap.getNetworkAccessPointID()))
            return false;

        if (!matchRoleIDCodes(ap.getRoleIDCode()))
            return false;

        return userIsRequestor == null
                || ap.isUserIsRequestor() == userIsRequestor.booleanValue();
    }

    private boolean matchRoleIDCodes(List<RoleIDCode> list) {
        if (roleIDCodes.length == 0)
            return true;

        for (RoleIDCode o : list) {
            for (RoleIDCode roleIDCode : roleIDCodes) {
                if (roleIDCode.getCode().equals(o.getCode())
                     && equals(roleIDCode.getCodeSystemName(), o.getCodeSystemName()))
                    return true;
            }
        }

        return false;
    }

    public boolean containsActiveParticipantCriteria() {
        return userIDs.length == 0
                && alternativeUserIDs.length == 0
                && roleIDCodes.length == 0
                && networkAccessPointIDs.length == 0
                && userIsRequestor == null;
    }

    private boolean isEmptyOrContains(String[] ss, String o) {
        if (ss.length == 0)
            return true;

        if (o == null)
            return false;

        for (String s : ss)
            if (o.equals(s))
                return true;

        return false;
    }

    private static String[] toStringArray(EventID... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCode(),
                    a[i].getCodeSystemName(),
                    null, 
                    a[i].getOriginalText())
                .toString();
        }
        return ss;
    }

    private static String[] toStringArray(EventTypeCode... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCode(),
                    a[i].getCodeSystemName(),
                    null, 
                    a[i].getOriginalText())
                .toString();
        }
        return ss;
    }

    private static String[] toStringArray(RoleIDCode... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCode(),
                    a[i].getCodeSystemName(),
                    null, 
                    a[i].getOriginalText())
                .toString();
        }
        return ss;
    }

    private static EventID[] toEventIDArray(String... ss) {
        EventID[] a = new EventID[ss.length];
        for (int i = 0; i < ss.length; i++) {
            Code code = new Code(ss[i]);
            a[i] = new EventID();
            a[i].setCode(code.getCodeValue());
            a[i].setCodeSystemName(code.getCodingSchemeDesignator());
            a[i].setOriginalText(code.getCodeMeaning());
        }
        return a;
    }

    private static EventTypeCode[] toEventTypeCodeArray(String... ss) {
        EventTypeCode[] a = new EventTypeCode[ss.length];
        for (int i = 0; i < ss.length; i++) {
            Code code = new Code(ss[i]);
            a[i] = new EventTypeCode();
            a[i].setCode(code.getCodeValue());
            a[i].setCodeSystemName(code.getCodingSchemeDesignator());
            a[i].setOriginalText(code.getCodeMeaning());
        }
        return a;
    }

    private static RoleIDCode[] toRoleIDCodeArray(String... ss) {
        RoleIDCode[] a = new RoleIDCode[ss.length];
        for (int i = 0; i < ss.length; i++) {
            Code code = new Code(ss[i]);
            a[i] = new RoleIDCode();
            a[i].setCode(code.getCodeValue());
            a[i].setCodeSystemName(code.getCodingSchemeDesignator());
            a[i].setOriginalText(code.getCodeMeaning());
        }
        return a;
    }
}
