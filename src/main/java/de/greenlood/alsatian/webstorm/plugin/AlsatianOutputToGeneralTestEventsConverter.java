package de.greenlood.alsatian.webstorm.plugin;

import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.events.*;
import org.jetbrains.annotations.NotNull;

public class AlsatianOutputToGeneralTestEventsConverter extends OutputToGeneralTestEventsConverter {

    public static AlsatianOutputToGeneralTestEventsConverter instance;

    public AlsatianOutputToGeneralTestEventsConverter(@NotNull TestConsoleProperties consoleProperties) {
        super(AlsatianConfigurationType.TEST_FRAMEWORK_NAME, consoleProperties);
        instance = this;
    }

    @Override
    public void onStartTesting() {
        super.onStartTesting();
    }

    @Override
    public synchronized void finishTesting() {
        super.finishTesting();
    }

    public void onTestStarted(TestStartedEvent testStartedEvent) {
        this.getProcessor().onTestStarted(testStartedEvent);
    }

    public void onSuiteStarted(TestSuiteStartedEvent testSuiteStartedEvent) {
        this.getProcessor().onSuiteStarted(testSuiteStartedEvent);
    }

    public void onFinishTesting() {
        this.getProcessor().onFinishTesting();
    }

    public void onSuiteFinished(TestSuiteFinishedEvent testSuiteFinishedEvent) {
        this.getProcessor().onSuiteFinished(testSuiteFinishedEvent);
    }

    public void onTestFinished(TestFinishedEvent testFinishedEvent) {
        this.getProcessor().onTestFinished(testFinishedEvent);
    }

    public void onTestIgnored(TestIgnoredEvent testIgnoredEvent) {
        this.getProcessor().onTestIgnored(testIgnoredEvent);
    }

    public void onTestFailure(TestFailedEvent testFailedEvent) {
        this.getProcessor().onTestFailure(testFailedEvent);
    }

    public void onTestOutput(TestOutputEvent testOutputEvent) {
        this.getProcessor().onTestOutput(testOutputEvent);
    }
}
