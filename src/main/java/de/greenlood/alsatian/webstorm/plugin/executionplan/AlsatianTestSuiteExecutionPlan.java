package de.greenlood.alsatian.webstorm.plugin.executionplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlsatianTestSuiteExecutionPlan {
    private List<AlsatianTestMethodExecutionPlan> testMethodExecutionPlans = new ArrayList<>();
    private String suiteName;

    public AlsatianTestSuiteExecutionPlan(String suiteName) {

        this.suiteName = suiteName;
    }

    public AlsatianTestMethodExecutionPlan addOrGetTest(String testName) {
        AlsatianTestMethodExecutionPlan methodExecutionPlan = new AlsatianTestMethodExecutionPlan(testName);
        int index = testMethodExecutionPlans.indexOf(methodExecutionPlan);
        if (index == -1) {
            testMethodExecutionPlans.add(methodExecutionPlan);
            return methodExecutionPlan;
        } else {
            return testMethodExecutionPlans.get(index);
        }
    }

    public List<AlsatianTestMethodExecutionPlan> getTestMethodExecutionPlans() {
        return testMethodExecutionPlans;
    }

    public String getSuiteName() {
        return suiteName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlsatianTestSuiteExecutionPlan that = (AlsatianTestSuiteExecutionPlan) o;
        return Objects.equals(suiteName, that.suiteName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suiteName);
    }
}
