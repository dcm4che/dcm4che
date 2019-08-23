package org.dcm4che3.data;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * 2 issuers can represent the same (if NSID or Universal ID are the same) but still differ (one might be missing the universal id for instance
 * because it was filtered out because it was requested by the query.)
 * There can max be 2 different issuers for a 'all'. The issuer from the rootId and a modified version of this issuer for OtherPatientId.
 * If there are 2 similar issuers present we will remove the one from the rootId. We don't want duplicates.
 */
public class OtherPatientIdCleaner {
    private final Set<IDWithIssuer> all;
    private final IDWithIssuer rootWithMainId;

    public OtherPatientIdCleaner(Set<IDWithIssuer> all, IDWithIssuer rootWithMainId) {
        this.all = all;
        this.rootWithMainId = rootWithMainId;
    }

    public Set<IDWithIssuer> filterOutDuplicates() {

        if (isNull(rootWithMainId)) return all;

        List<List<IDWithIssuer>> issuersPerPID_that_have_a_duplicate = all.stream()
                .collect(Collectors.groupingBy(IDWithIssuer::getID))
                .values().stream().filter(moreThan1())
                .collect(Collectors.toList());

        Set<IDWithIssuer> duplicateRootIssuersThatShouldBeRemoved = issuersPerPID_that_have_a_duplicate
                .stream()
                .map(list -> list.stream()
                        .filter(idWithIssuer -> occursTwiceIn(idWithIssuer, list))
                        .filter(rootWithMainId::equals)
                        .collect(Collectors.toList())
                )
                .filter(notEmpty())
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        all.removeAll(duplicateRootIssuersThatShouldBeRemoved);
        return all;

    }

    private Predicate<List<IDWithIssuer>> notEmpty() {
        return l -> !l.isEmpty();
    }

    private Predicate<List<IDWithIssuer>> moreThan1() {
        return l -> l.size() > 1;
    }

    private boolean occursTwiceIn(IDWithIssuer idWithIssuer, List<IDWithIssuer> list) {
        List<IDWithIssuer> workList = new ArrayList<>(list);
        workList.remove(idWithIssuer);
        Set<IDWithIssuer> seek = new TreeSet<>(equalIfTheyMatch());
        seek.addAll(workList);
        boolean thereIsStillAMAtchingOnePresent = seek.contains(idWithIssuer);

        return thereIsStillAMAtchingOnePresent;
    }

    private Comparator<IDWithIssuer> equalIfTheyMatch() {
        return (o1, o2) -> o1.matches(o2) ? 0 : -1;
    }
}
