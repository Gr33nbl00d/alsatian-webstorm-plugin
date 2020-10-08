package de.greenlood.alsatian.webstorm.plugin.executionplan;

import java.util.Objects;

public class AlsatianTestMethodExecutionPlan {
    private String testName;

    public AlsatianTestMethodExecutionPlan(String testName) {

        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlsatianTestMethodExecutionPlan that = (AlsatianTestMethodExecutionPlan) o;
        return Objects.equals(testName, that.testName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testName);
    }
}
