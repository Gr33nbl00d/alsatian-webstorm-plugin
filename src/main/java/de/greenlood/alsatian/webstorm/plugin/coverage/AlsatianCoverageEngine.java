//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.greenlood.alsatian.webstorm.plugin.coverage;

import com.intellij.coverage.*;
import com.intellij.coverage.view.CoverageListRootNode;
import com.intellij.coverage.view.CoverageViewExtension;
import com.intellij.coverage.view.CoverageViewManager.StateBean;
import com.intellij.coverage.view.DirectoryCoverageViewExtension;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.WrappingRunConfiguration;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.util.containers.ContainerUtil;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianCoverageEnabledConfiguration;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class AlsatianCoverageEngine extends CoverageEngine {
    public static final String ID = "AlsatianCoverageEngine";

    public AlsatianCoverageEngine() {
    }

    public boolean isApplicableTo(@Nullable RunConfigurationBase configuration) {
        RunConfiguration result = configuration;
        if (configuration instanceof WrappingRunConfiguration) {
            result = ((WrappingRunConfiguration)configuration).getPeer();
        }

        return result instanceof AlsatianRunConfiguration;
    }

    public boolean canHavePerTestCoverage(@Nullable RunConfigurationBase configuration) {
        return false;
    }

    @NotNull
    public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@Nullable RunConfigurationBase configuration) {
        return new AlsatianCoverageEnabledConfiguration(configuration);
    }

    public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner, @NotNull String name, @NotNull CoverageFileProvider coverageDataFileProvider, @Nullable String[] filters, long lastCoverageTimeStamp, @Nullable String suiteToMerge, boolean coverageByTestEnabled, boolean tracingEnabled, boolean trackTestFolders, Project project) {
        return new AlsatianCoverageSuite(covRunner, name, coverageDataFileProvider, lastCoverageTimeStamp, coverageByTestEnabled, tracingEnabled, trackTestFolders, project, this);
    }

    public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner, @NotNull String name, @NotNull CoverageFileProvider coverageDataFileProvider, @NotNull CoverageEnabledConfiguration config) {
        if (config instanceof AlsatianCoverageEnabledConfiguration) {
            Project project = config.getConfiguration().getProject();
            return this.createCoverageSuite(covRunner, name, coverageDataFileProvider, (String[])null, (new Date()).getTime(), (String)null, false, false, true, project);
        } else {
            return null;
        }
    }

    public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
        return new AlsatianCoverageSuite(this);
    }

    @NotNull
    public CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
        AlsatianCoverageAnnotator var10000 = AlsatianCoverageAnnotator.getInstance(project);
        return var10000;
    }

    public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
        return psiFile instanceof JSFile || psiFile instanceof HtmlFileImpl;
    }

    public boolean acceptedByFilters(@NotNull PsiFile psiFile, @NotNull CoverageSuitesBundle suite) {
        return true;
    }

    public boolean recompileProjectAndRerunAction(@NotNull Module module, @NotNull CoverageSuitesBundle suite, @NotNull Runnable chooseSuiteAction) {
        return false;
    }

    public String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
        return getQName(sourceFile);
    }

    @Nullable
    private static String getQName(@NotNull PsiFile sourceFile) {
        VirtualFile file = sourceFile.getVirtualFile();
        return file == null ? null : file.getPath();
    }

    @NotNull
    public Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
        String qName = getQName(sourceFile);
        Set var10000 = qName != null ? Collections.singleton(qName) : Collections.emptySet();
        return var10000;
    }

    public boolean includeUntouchedFileInCoverage(@NotNull String qualifiedName, @NotNull File outputFile, @NotNull PsiFile sourceFile, @NotNull CoverageSuitesBundle suite) {
        return false;
    }

    public List<Integer> collectSrcLinesForUntouchedFile(@NotNull File classFile, @NotNull CoverageSuitesBundle suite) {
        return null;
    }

    public List<PsiElement> findTestsByNames(@NotNull String[] testNames, @NotNull Project project) {
        return Collections.emptyList();
    }

    public String getTestMethodName(@NotNull PsiElement element, @NotNull AbstractTestProxy testProxy) {
        return null;
    }

    public String getPresentableText() {
        return "AlsatianTestRunnerCoverage";
    }

    public boolean coverageProjectViewStatisticsApplicableTo(VirtualFile fileOrDir) {
        return !fileOrDir.isDirectory();
    }

    public CoverageViewExtension createCoverageViewExtension(final Project project, final CoverageSuitesBundle suiteBundle, StateBean stateBean) {
        return new DirectoryCoverageViewExtension(project, this.getCoverageAnnotator(project), suiteBundle, stateBean) {
            public List<AbstractTreeNode<?>> getChildrenNodes(AbstractTreeNode node) {
                return ContainerUtil.filter(super.getChildrenNodes(node), (child) -> {
                    return !StringUtil.equals(child.getName(), ".idea");
                });
            }

            @NotNull
            public AbstractTreeNode createRootNode() {
                AbstractTreeNode var10000 = (AbstractTreeNode)ReadAction.compute(() -> {
                    VirtualFile rootDir = AlsatianCoverageEngine.findRootDir(project, suiteBundle);
                    if (rootDir == null) {
                        rootDir = this.myProject.getBaseDir();
                    }

                    PsiDirectory psiRootDir = PsiManager.getInstance(this.myProject).findDirectory(rootDir);
                    return new CoverageListRootNode(this.myProject, psiRootDir, this.mySuitesBundle, this.myStateBean);
                });
                return var10000;
            }
        };
    }

    @Nullable
    private static VirtualFile findRootDir(@NotNull Project project, @NotNull CoverageSuitesBundle suitesBundle) {
        CoverageDataManager coverageDataManager = CoverageDataManager.getInstance(project);
        CoverageSuite[] var3 = suitesBundle.getSuites();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            CoverageSuite suite = var3[var5];
            ProjectData data = suite.getCoverageData(coverageDataManager);
            if (data != null) {
                Iterator var8 = data.getClasses().keySet().iterator();

                while(var8.hasNext()) {
                    Object key = var8.next();
                    if (key instanceof String) {
                        String path = (String)key;
                        VirtualFile file = VfsUtil.findFileByIoFile(new File(path), false);
                        if (file != null && file.isValid()) {
                            ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
                            VirtualFile contentRoot = projectFileIndex.getContentRootForFile(file);
                            if (contentRoot != null && contentRoot.isDirectory() && contentRoot.isValid()) {
                                return contentRoot;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
