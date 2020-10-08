// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package de.greenlood.alsatian.webstorm.plugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class AlsatianRerunFailedTestAction extends AbstractRerunFailedTestsAction {

    public AlsatianRerunFailedTestAction(@NotNull SMTRunnerConsoleView consoleView, AlsatianConsoleProperties consoleProperties) {
        super(consoleView);
        init(consoleProperties);
        setModel(consoleView.getResultsViewer());
    }

    @Nullable
    @Override
    protected MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
        AlsatianRunConfiguration configuration = (AlsatianRunConfiguration) myConsoleProperties.getConfiguration();
        final AlsatianRunProfileState runProfileState = new AlsatianRunProfileState(configuration.getProject(), configuration, environment);
        runProfileState.setFailedTests(getFailedTests(configuration.getProject()));
        return new MyRunProfile(configuration) {
            @Override
            public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
                return runProfileState;
            }
        };
    }
}
