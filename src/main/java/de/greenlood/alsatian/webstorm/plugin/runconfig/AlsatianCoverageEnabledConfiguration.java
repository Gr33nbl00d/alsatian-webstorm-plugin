//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import de.greenlood.alsatian.webstorm.plugin.coverage.AlsatianCoverageRunner;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class AlsatianCoverageEnabledConfiguration extends CoverageEnabledConfiguration {

    public AlsatianCoverageEnabledConfiguration(@NotNull RunConfigurationBase configuration) {
        super(configuration);
        AlsatianCoverageRunner coverageRunner = CoverageRunner.getInstance(AlsatianCoverageRunner.class);
        this.setCoverageRunner(coverageRunner);
    }

    @Nullable
    @Override
    public String getCoverageFilePath() {
        @NonNls final String coverageRootPath = PathManager.getSystemPath() + File.separator + "coverage";
        final String path = coverageRootPath + File.separator + FileUtil.sanitizeFileName(getConfiguration().getProject().getName()) + coverageFileNameSeparator()
                + FileUtil.sanitizeFileName(getConfiguration().getName());

        new File(coverageRootPath).mkdirs();
        return path;

    }
}
