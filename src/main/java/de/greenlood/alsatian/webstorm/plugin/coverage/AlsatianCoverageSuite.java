//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.greenlood.alsatian.webstorm.plugin.coverage;

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianCoverageSuite extends BaseCoverageSuite {
    private final AlsatianCoverageEngine myCoverageEngine;

    public AlsatianCoverageSuite(AlsatianCoverageEngine coverageEngine) {
        this.myCoverageEngine = coverageEngine;
    }

    public AlsatianCoverageSuite(CoverageRunner coverageRunner, String name, @Nullable CoverageFileProvider fileProvider, long lastCoverageTimeStamp, boolean coverageByTestEnabled, boolean tracingEnabled, boolean trackTestFolders, Project project, AlsatianCoverageEngine coverageEngine) {
        super(name, fileProvider, lastCoverageTimeStamp, coverageByTestEnabled, tracingEnabled, trackTestFolders, coverageRunner, project);
        this.myCoverageEngine = coverageEngine;
    }

    @NotNull
    public CoverageEngine getCoverageEngine() {
        return this.myCoverageEngine;
    }
}
