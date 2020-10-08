package de.greenlood.alsatian.webstorm.plugin.executionplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlsatianExecutionPlan {
    private List<AlsatianTestFileExecutionPlan> testFileExecutionPlans = new ArrayList<>();

    public AlsatianTestFileExecutionPlan addOrGetTestFile(String locationUrl) {
        AlsatianTestFileExecutionPlan fileExecutionPlan = new AlsatianTestFileExecutionPlan(locationUrl);
        int index = testFileExecutionPlans.indexOf(fileExecutionPlan);
        if (index == -1) {
            testFileExecutionPlans.add(fileExecutionPlan);
            return fileExecutionPlan;
        } else {
            return testFileExecutionPlans.get(index);
        }
    }

    public List<AlsatianTestFileExecutionPlan> getTestFileExecutionPlans() {
        return testFileExecutionPlans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlsatianExecutionPlan that = (AlsatianExecutionPlan) o;
        return testFileExecutionPlans.equals(that.testFileExecutionPlans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testFileExecutionPlans);
    }
}
