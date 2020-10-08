//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.greenlood.alsatian.webstorm.plugin.coverage;

import com.intellij.coverage.CoverageBundle;
import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianCoverageAnnotator extends SimpleCoverageAnnotator {
    public AlsatianCoverageAnnotator(@NotNull Project project) {
        super(project);
    }

    public static AlsatianCoverageAnnotator getInstance(@NotNull Project project) {
        return (AlsatianCoverageAnnotator)ServiceManager.getService(project, AlsatianCoverageAnnotator.class);
    }

    protected boolean shouldCollectCoverageInsideLibraryDirs() {
        return false;
    }

    @Nullable
    public String getDirCoverageInformationString(@NotNull PsiDirectory directory, @NotNull CoverageSuitesBundle currentSuite, @NotNull CoverageDataManager manager) {
        DirCoverageInfo coverageInfo = this.getDirCoverageInfo(directory, currentSuite);
        if (coverageInfo == null) {
            return null;
        } else if (manager.isSubCoverageActive()) {
            return coverageInfo.coveredLineCount > 0 ? "covered" : null;
        } else {
            String filesCoverageInfo = this.getFilesCoverageInformationString(coverageInfo);
            if (filesCoverageInfo != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(filesCoverageInfo);
                String linesCoverageInfo = this.getLinesCoverageInformationString(coverageInfo);
                if (linesCoverageInfo != null) {
                    builder.append(": ").append(linesCoverageInfo);
                }

                return builder.toString();
            } else {
                return null;
            }
        }
    }

    protected String getLinesCoverageInformationString(@NotNull FileCoverageInfo info) {
        if (info.totalLineCount == 0) {
            return null;
        } else {
            return info.coveredLineCount == 0 ? CoverageBundle.message("lines.covered.info.not.covered", new Object[0]) : calcCoveragePercentage(info) + CoverageBundle.message("lines.covered.info.percent.lines.covered", new Object[0]);
        }
    }

    @Nullable
    protected String getFilesCoverageInformationString(@NotNull DirCoverageInfo info) {
        return info.totalFilesCount == 0 ? null : info.totalFilesCount + (info.totalFilesCount == 1 ? " file" : " files");
    }
}
