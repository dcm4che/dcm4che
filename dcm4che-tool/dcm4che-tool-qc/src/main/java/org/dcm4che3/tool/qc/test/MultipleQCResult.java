package org.dcm4che3.tool.qc.test;

import org.dcm4che3.tool.common.test.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by umberto on 8/20/15.
 */
public class MultipleQCResult implements TestResult{

    private List<QCResult> results = new ArrayList<QCResult>();

    public List<QCResult> getResults() {
        return results;
    }

    public void setResults(List<QCResult> results) {
        this.results = results;
    }
}
