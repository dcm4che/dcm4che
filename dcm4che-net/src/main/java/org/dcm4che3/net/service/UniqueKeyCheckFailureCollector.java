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
 * Portions created by the Initial Developer are Copyright (C) 2020
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

import org.dcm4che3.util.TagUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Homero Cardoso de Almeida <homero.cardosodealmeida@agfa.com>
 */
public class UniqueKeyCheckFailureCollector {
    private Map<UniqueKeyCheckFailure.FailureType, List<UniqueKeyCheckFailure>> failures =
            new EnumMap<>(UniqueKeyCheckFailure.FailureType.class);

    public void add(UniqueKeyCheckFailure failure) {
        List<UniqueKeyCheckFailure> previous = failures.computeIfAbsent(failure.type, t -> new ArrayList<>());
        previous.add(failure);
    }

    public boolean isEmpty() {
        return failures.isEmpty();
    }

    public String getFailureMessage() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<UniqueKeyCheckFailure.FailureType, List<UniqueKeyCheckFailure>> entry: failures.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(entry.getKey().description);
            if (entry.getValue().size() > 1) {
                sb.append("s");
            }
            sb.append(": ");
            List<String> messages = entry.getValue().stream().map(failed -> {
                String entryMsg = TagUtils.toString(failed.key);
                if (Objects.nonNull(failed.value)) {
                    entryMsg = String.format("%s - \"%s\"", entryMsg, failed.value);
                }
                return entryMsg;
            }).collect(Collectors.toList());
            sb.append(String.join(", ", messages));
        }

        return sb.toString();
    }

    public int[] getTags() {
        return failures.values().stream()
                .flatMap(List::stream)
                .mapToInt(entry -> entry.key)
                .toArray();
    }
}
