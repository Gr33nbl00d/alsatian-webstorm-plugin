package de.greenlood.alsatian.webstorm.plugin.coverage;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageRunnerData;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import de.greenlood.alsatian.webstorm.plugin.AlsatianRunProfileState;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public final class AlsatianCoverageProgramRunner implements ProgramRunner<RunnerSettings> {
    private static final Logger LOG = Logger.getInstance(AlsatianCoverageProgramRunner.class);
    private static final String COVERAGE_RUNNER_ID = AlsatianCoverageProgramRunner.class.getSimpleName();

    public AlsatianCoverageProgramRunner() {
    }

    @NotNull
    public String getRunnerId() {
        return COVERAGE_RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return "Coverage".equals(executorId) && profile instanceof AlsatianRunConfiguration;
    }

    public RunnerSettings createConfigurationData(@NotNull ConfigurationInfoProvider settingsProvider) {
        return new CoverageRunnerData();
    }

    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        ExecutionManager.getInstance(environment.getProject()).startRunProfile(environment, (state) -> {
            RunContentDescriptor descriptor = DefaultProgramRunnerKt.executeState(state, environment, this);
            if (descriptor != null) {
                doExecute(descriptor, (AlsatianRunProfileState) state, environment);
            }

            return descriptor;
        });
    }

    private void doExecute(@NotNull final RunContentDescriptor descriptor, @NotNull final AlsatianRunProfileState state, @NotNull final ExecutionEnvironment environment) throws ExecutionException {
        ExecutionManager.getInstance(environment.getProject()).startRunProfile(environment, (state2) -> {
            //RunContentDescriptor descriptor2 = DefaultProgramRunnerKt.executeState(state2, environment, this);
            if (descriptor != null) {
                ProcessHandler handler = (ProcessHandler) Objects.requireNonNull(descriptor.getProcessHandler());
                handler.addProcessListener(new ProcessAdapter() {
                    public void processTerminated(@NotNull ProcessEvent event) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            AlsatianCoverageProgramRunner.updateCoverageView(environment, (AlsatianRunProfileState) state);
                        }, environment.getProject().getDisposed());
                    }
                });
            }

            return descriptor;
        });
    }

    private static void updateCoverageView(@NotNull ExecutionEnvironment env, @NotNull AlsatianRunProfileState runProfileState) {
        RunConfigurationBase<?> runConfiguration = (RunConfigurationBase) env.getRunProfile();
        CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
        File lcovFile = new File(coverageEnabledConfiguration.getCoverageFilePath());
        if (lcovFile != null) {
            if (!lcovFile.isFile()) {
                LOG.warn("Cannot find " + lcovFile.getAbsolutePath());
            } else {
                RunnerSettings runnerSettings = env.getRunnerSettings();
                if (runnerSettings != null) {
                    AlsatianCoverageRunner coverageRunner = AlsatianCoverageRunner.getInstance();
                    coverageRunner.setWorkingDirectory(runProfileState.getRunSettings().getWorkingDir());
                    coverageRunner.setInterpreter(runProfileState.getRunSettings().getInterpreterRef().resolve(env.getProject()));
                    CoverageDataManager.getInstance(env.getProject()).processGatheredCoverage(runConfiguration, runnerSettings);
                }

            }
        }
    }
}
