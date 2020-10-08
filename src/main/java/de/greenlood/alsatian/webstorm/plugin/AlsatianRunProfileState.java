package de.greenlood.alsatian.webstorm.plugin;

import com.google.gson.Gson;
import com.intellij.coverage.CoverageHelper;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.nodejs.debug.NodeLocalDebugRunProfileState;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import de.greenlood.alsatian.webstorm.plugin.coverage.AlsatianCoverageRunner;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestFileExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestSuiteExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.pluginconfig.AlsatianPluginSettingsState;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianCoverageEnabledConfiguration;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunSettings;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianScopeKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.builtInWebServer.BuiltInServerOptions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AlsatianRunProfileState implements RunProfileState, NodeLocalDebugRunProfileState {
    private static final Logger logger = Logger.getInstance(AlsatianCoverageRunner.class);
    private final Project project;
    private final AlsatianRunConfiguration alsatianRunConfiguration;
    private final ExecutionEnvironment env;
    private static OutputToGeneralTestEventsConverter alsatianOutputToGeneralTestEventsConverter;
    private AlsatianConsoleProperties consoleProperties;
    private List<AbstractTestProxy> failedTests;
    private final Gson gson = new Gson();

    public AlsatianRunProfileState(@NotNull Project project, AlsatianRunConfiguration alsatianRunConfiguration, @NotNull ExecutionEnvironment env) {

        this.project = project;
        this.alsatianRunConfiguration = alsatianRunConfiguration;
        this.env = env;
    }

    @Override
    public @NotNull
    ExecutionResult execute(int debugPort) throws ExecutionException {
        Project project = this.env.getProject();
        CoverageTempDataPaths coverageTempDataPaths = null;
        if (isRunWithCoverage()) {
            coverageTempDataPaths = createCoverageTempPaths();
        }
        final NodeJsInterpreter interpreter = this.alsatianRunConfiguration.getInterpreterRef().resolve(project);
        final NodeJsLocalInterpreter localInterpreter = NodeJsLocalInterpreter.castAndValidate(interpreter);
        final GeneralCommandLine commandLine = this.getCommandLine(localInterpreter, debugPort, coverageTempDataPaths);
        final ProcessHandler processHandler = NodeCommandLineUtil.createProcessHandler(commandLine, false);
        ProcessTerminatedListener.attach(processHandler);
        final ConsoleView consoleView = createTestConsoleView(processHandler, this.env, new AlsatianTestLocationProvider());

        if (isRunWithCoverage()) {
            registerCoveragePostProcessing(coverageTempDataPaths, processHandler);
        }

        final DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
        executionResult.setRestartActions(new AlsatianRerunFailedTestAction((SMTRunnerConsoleView) consoleView, consoleProperties), new ToggleAutoTestAction());
        return executionResult;
    }

    private boolean isRunWithCoverage() {
        return env.getExecutor().getId().equals("Coverage");
    }

    @NotNull
    private CoverageTempDataPaths createCoverageTempPaths() {
        CoverageTempDataPaths coverageTempDataPaths;
        try {
            File nycTempDirectory = FileUtilRt.createTempDirectory("nyc", "temp");
            File reportTempDirectory = FileUtilRt.createTempDirectory("nyc", "temp");
            coverageTempDataPaths = new CoverageTempDataPaths(nycTempDirectory, reportTempDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return coverageTempDataPaths;
    }

    private void registerCoveragePostProcessing(CoverageTempDataPaths coverageTempDataPaths, ProcessHandler processHandler) {
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                AlsatianCoverageEnabledConfiguration coverageEnabledConfiguration = (AlsatianCoverageEnabledConfiguration) CoverageEnabledConfiguration.getOrCreate(alsatianRunConfiguration);

                if (coverageTempDataPaths.getNycTempDirectory() != null)
                    FileUtilRt.delete(coverageTempDataPaths.getNycTempDirectory());
                if (coverageTempDataPaths.getReportTempDirectory() != null) {
                    File lcovTempFile = new File(coverageTempDataPaths.getReportTempDirectory(), "lcov.info");
                    String targetPath = coverageEnabledConfiguration.getCoverageFilePath();
                    CoverageHelper.resetCoverageSuit(coverageEnabledConfiguration.getConfiguration());
                    try {
                        FileUtilRt.copy(lcovTempFile, new File(targetPath));
                    } catch (IOException e) {
                        logger.warn("could not copy coverage file", e);
                    }
                    FileUtilRt.delete(coverageTempDataPaths.getReportTempDirectory());
                }

            }
        });
    }


    private ConsoleView createTestConsoleView(@NotNull ProcessHandler processHandler, @NotNull ExecutionEnvironment env, @NotNull SMTestLocator locator) {
        RunConfiguration runConfiguration = (RunConfiguration) env.getRunProfile();
        consoleProperties = new AlsatianConsoleProperties(env, runConfiguration, env.getExecutor(), locator, alsatianRunConfiguration);
        alsatianOutputToGeneralTestEventsConverter = new AlsatianOutputToGeneralTestEventsConverter(consoleProperties);
        consoleProperties.setOutputToGeneralTestEventsConverter(alsatianOutputToGeneralTestEventsConverter);
        //alsatianOutputToGeneralTestEventsConverter=new ImportedToGeneralTestEventsConverter("TestRunnerAlsatian",consoleProperties,)
        final ConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createConsole("TestRunnerAlsatian", consoleProperties);

        String basePath = project.getBasePath();

        assert basePath != null;
        final File workingDir = new File(basePath);
        testsOutputConsoleView.addMessageFilter(new NodeStackTraceFilter(project, workingDir));
        testsOutputConsoleView.addMessageFilter(new NodeConsoleAdditionalFilter(project, workingDir));
        testsOutputConsoleView.addMessageFilter(new TypeScriptErrorConsoleFilter(project, workingDir));
        testsOutputConsoleView.attachToProcess(processHandler);
        Disposer.register(env.getProject(), testsOutputConsoleView);
        return testsOutputConsoleView;
    }

    @NotNull
    private GeneralCommandLine getCommandLine(@NotNull NodeJsLocalInterpreter interpreter, int debugPort, CoverageTempDataPaths coverageTempDataPaths) throws ExecutionException {
        Project project = env.getProject();
        final GeneralCommandLine commandLine = new GeneralCommandLine();

        commandLine.withCharset(StandardCharsets.UTF_8);
        AlsatianRunSettings runSettings = alsatianRunConfiguration.getRunSettings();
        commandLine.withWorkDirectory(runSettings.getWorkingDir());
        commandLine.setExePath(interpreter.getInterpreterSystemDependentPath());
        NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), debugPort, true, interpreter, true);
        commandLine.withRedirectErrorStream(true);
        if (isRunWithCoverage()) {
            AlsatianPluginSettingsState pluginSettings = AlsatianPluginSettingsState.getInstance(project);
            String nycRelativePath = Paths.get(pluginSettings.nycPackage, "bin/nyc.js").toString();
            commandLine.addParameter(nycRelativePath);
            commandLine.addParameter("--temp-dir");
            commandLine.addParameter(coverageTempDataPaths.getNycTempDirectory().getAbsolutePath());
            commandLine.addParameter("--report-dir");
            commandLine.addParameter(coverageTempDataPaths.getReportTempDirectory().getAbsolutePath());
            commandLine.addParameter("--check-coverage");
            commandLine.addParameter("false");
            commandLine.addParameter("--reporter");
            commandLine.addParameter("lcovonly");
            commandLine.addParameter(interpreter.getInterpreterSystemDependentPath());
        }
        //todo replace this with alsatian package from config once included in alsation
        commandLine.addParameter("node_modules/alsatian-webstorm/dist/TestRunner.js");
        int effectiveBuiltInServerPort = BuiltInServerOptions.getInstance().getEffectiveBuiltInServerPort();
        commandLine.addParameter("localhost");
        commandLine.addParameter(String.valueOf(effectiveBuiltInServerPort));


        if (failedTests != null) {
            AlsatianExecutionPlan src = new AlsatianExecutionPlan();
            for (AbstractTestProxy failedTest : failedTests) {
                SMTestProxy failedSMTestProxy = (SMTestProxy) failedTest;
                if (failedTest.getLocationUrl() != null) {
                    if (((SMTestProxy) failedTest).isSuite()) {
                        String locationUrl = failedSMTestProxy.getLocationUrl();
                        AlsatianTestFileExecutionPlan fileExecutionPlan = src.addOrGetTestFile(locationUrl);
                        fileExecutionPlan.addOrGetSuite(failedTest.getName());
                    } else {
                        SMTestProxy parent = (SMTestProxy) failedTest.getParent();
                        if (parent.isSuite() == false)
                            throw new IllegalStateException("test method has to be in a suite");

                        String locationUrl = parent.getLocationUrl();
                        AlsatianTestFileExecutionPlan fileExecutionPlan = src.addOrGetTestFile(locationUrl);
                        AlsatianTestSuiteExecutionPlan suiteExecutionPlan = fileExecutionPlan.addOrGetSuite(parent.getName());
                        suiteExecutionPlan.addOrGetTest(failedTest.getName());
                    }
                }
            }
            createExecutionPlanCommandLine(commandLine, src);
        } else if (runSettings.getScopeKind() == AlsatianScopeKind.WILDCARD) {
            createListOfGlobsCommandLine(commandLine, runSettings.getListOfGlobs());
        } else {
            AlsatianExecutionPlan src = runSettings.getAlsatianExecutionPlan();
            createExecutionPlanCommandLine(commandLine, src);
        }
        return commandLine;
    }

    private void createExecutionPlanCommandLine(GeneralCommandLine commandLine, AlsatianExecutionPlan src) {
        commandLine.addParameter("EXECUTIONPLAN");
        File tempFile = null;
        try {
            Gson gson = new Gson();
            String jsonExecutionPlan = gson.toJson(src);
            tempFile = FileUtil.createTempFile("executionplan", UUID.randomUUID().toString());
            Files.write(tempFile.toPath(), jsonExecutionPlan.getBytes("UTF-8"));
            commandLine.addParameter(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createListOfGlobsCommandLine(GeneralCommandLine commandLine, List<String> listOfGlobs) {
        commandLine.addParameter("LISTOFGLOBS");
        for (String glob : listOfGlobs) {
            commandLine.addParameter(glob);
        }
    }

    public void setFailedTests(List<AbstractTestProxy> failedTests) {

        this.failedTests = failedTests;
    }

    public AlsatianRunSettings getRunSettings() {
        return alsatianRunConfiguration.getRunSettings();
    }
}
