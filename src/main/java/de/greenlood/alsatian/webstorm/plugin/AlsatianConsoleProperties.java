package de.greenlood.alsatian.webstorm.plugin;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {
    private final SMTestLocator myLocator;
    private ExecutionEnvironment myEnvironment;
    private AlsatianRunConfiguration myConfiguration;
    private OutputToGeneralTestEventsConverter outputToGeneralTestEventsConverter;

    public AlsatianConsoleProperties(@NotNull ExecutionEnvironment environment, @NotNull RunConfiguration config, @NotNull Executor executor, @NotNull SMTestLocator locator, AlsatianRunConfiguration configuration) {
        super(config, AlsatianConfigurationType.TEST_FRAMEWORK_NAME, executor);
        this.myConfiguration = configuration;
        setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
        setIfUndefined(TestConsoleProperties.SHOW_STATISTICS, true);
        setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true);
        setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
        myLocator = locator;
        myEnvironment = environment;
    }

    @Nullable
    @Override
    public SMTestLocator getTestLocator() {
        return myLocator;
    }


    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
        return outputToGeneralTestEventsConverter;
    }

    @Nullable
    @Override
    public AbstractRerunFailedTestsAction createRerunFailedTestsAction(ConsoleView consoleView) {
        return new AlsatianRerunFailedTestAction((SMTRunnerConsoleView)consoleView, this);
    }

    public void setOutputToGeneralTestEventsConverter(OutputToGeneralTestEventsConverter outputToGeneralTestEventsConverter) {
        this.outputToGeneralTestEventsConverter = outputToGeneralTestEventsConverter;
    }
}
