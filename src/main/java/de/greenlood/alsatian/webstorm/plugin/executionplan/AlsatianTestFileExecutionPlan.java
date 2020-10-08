package de.greenlood.alsatian.webstorm.plugin.executionplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlsatianTestFileExecutionPlan {
    List<AlsatianTestSuiteExecutionPlan> testSuiteExecutionPlans = new ArrayList<>();
    private String locationUrl;

    public AlsatianTestFileExecutionPlan(String locationUrl) {

        this.locationUrl = locationUrl;
    }

    public AlsatianTestSuiteExecutionPlan addOrGetSuite(String suiteName) {
        AlsatianTestSuiteExecutionPlan suiteExecutionPlan = new AlsatianTestSuiteExecutionPlan(suiteName);
        int index = testSuiteExecutionPlans.indexOf(suiteExecutionPlan);
        if (index == -1) {
            testSuiteExecutionPlans.add(suiteExecutionPlan);
            return suiteExecutionPlan;
        } else {
            return testSuiteExecutionPlans.get(index);
        }
    }

    public List<AlsatianTestSuiteExecutionPlan> getTestSuiteExecutionPlans() {
        return testSuiteExecutionPlans;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlsatianTestFileExecutionPlan that = (AlsatianTestFileExecutionPlan) o;
        return Objects.equals(locationUrl, that.locationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationUrl);
    }
}
