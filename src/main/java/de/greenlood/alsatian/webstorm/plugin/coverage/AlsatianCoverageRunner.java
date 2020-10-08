package de.greenlood.alsatian.webstorm.plugin.coverage;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.testing.CoverageProjectDataLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ProjectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;


public final class AlsatianCoverageRunner extends CoverageRunner {
    private static final Logger LOG = Logger.getInstance(AlsatianCoverageRunner.class);
    private String myWorkingDirectory;
    private NodeJsInterpreter myInterpreter;

    public AlsatianCoverageRunner() {
    }

    public void setWorkingDirectory(@Nullable String workingDirectory) {
        this.myWorkingDirectory = workingDirectory;
    }

    public void setInterpreter(@Nullable NodeJsInterpreter interpreter) {
        this.myInterpreter = interpreter;
    }

    @NotNull
    public static AlsatianCoverageRunner getInstance() {
        return Objects.requireNonNull(CoverageRunner.getInstance(AlsatianCoverageRunner.class));
    }

    public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
        File basePathDir = this.getBaseDir();

        try {
            return CoverageProjectDataLoader.readProjectData(sessionDataFile, basePathDir, this.myInterpreter);
        } catch (Exception var5) {
            LOG.warn("Can't read coverage data", var5);
            return null;
        }
    }

    @NotNull
    private File getBaseDir() {
        String basePath = this.myWorkingDirectory;
        return basePath != null ? new File(basePath) : new File(".");
    }

    @NotNull
    public String getPresentableName() {
        return "Alsatian Test Run";
    }

    @NotNull
    public String getId() {
        return "AlsatianTestRunnerCoverage";
    }

    @NotNull
    public String getDataFileExtension() {
        return "info";
    }

    public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
        return engine instanceof AlsatianCoverageEngine;
    }
}
