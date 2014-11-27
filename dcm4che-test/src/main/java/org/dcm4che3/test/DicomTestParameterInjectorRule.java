package org.dcm4che3.test;

import java.lang.reflect.Method;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DicomTestParameterInjectorRule implements TestRule {


    private DicomTest test;

    public DicomTestParameterInjectorRule(DicomTest test) {
        this.test = test;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Method method = description.getTestClass().getMethod(description.getMethodName());
                test.setCurrentTestMethodParameters(method.getAnnotation(DicomParameters.class));
                test.setCurrentTestClassParameters(description.getTestClass().getAnnotation(DicomParameters.class));
                base.evaluate();
            }
        };
    }




}

