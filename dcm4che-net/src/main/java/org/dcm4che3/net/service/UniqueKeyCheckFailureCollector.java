//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2020                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4che3.net.service;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.util.TagUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author apyii
 */
public class UniqueKeyCheckFailureCollector {
    private Map<UniqueKeyCheckFailure.FailureType, List<UniqueKeyCheckFailure>> failures;
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    public void add(UniqueKeyCheckFailure failure) {
        List<UniqueKeyCheckFailure> previous = failures.computeIfAbsent(failure.type, t -> new ArrayList<>());
        previous.add(failure);
    }

    public boolean isEmpty() {
        return failures.isEmpty();
    }

    public String getFailureMessage() {
        StringBuilder sb = new StringBuilder();

        for (UniqueKeyCheckFailure.FailureType type: failures.keySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(type.description);
            List<UniqueKeyCheckFailure> entries = failures.get(type);
            if (entries.size() > 1) {
                sb.append("s");
            }
            sb.append(": ");
            List<String> messages = entries.stream().map(entry -> {
                String entryMsg = MessageFormat.format("{0} {1}",
                        DICT.keywordOf(entry.key), TagUtils.toString(entry.key));
                if (Objects.nonNull(entry.value)) {
                    entryMsg = MessageFormat.format("{0} - \"{1}\"", entryMsg, entry.value);
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
