package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.javascript.testFramework.JsTestElementPath;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
import de.greenlood.alsatian.webstorm.plugin.AlsatianConfigurationType;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestFileExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestSuiteExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructure;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructureBuilder;
import de.greenlood.alsatian.webstorm.plugin.pluginconfig.AlsatianPluginSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

public class AlsatianRunConfigurationProducer extends JsTestRunConfigurationProducer<AlsatianRunConfiguration> {
    public AlsatianRunConfigurationProducer() {
        super(Collections.singletonList("alsatian"));
    }

    @Override
    protected boolean setupConfigurationFromCompatibleContext(@NotNull AlsatianRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {

        PsiElement element = context.getPsiLocation();
        if (element != null && this.isTestRunnerPackageAvailableFor(element, context)) {
            TestElementInfo elementRunInfo = createTestElementRunInfo(element, context, configuration.getRunSettings());
            if (elementRunInfo == null) {
                return false;
            } else {
                AlsatianRunSettings runSettings = elementRunInfo.getRunSettings();
                configuration.setRunSettings(runSettings);
                String generatedName = runSettings.getGeneratedName();
                if (generatedName != null) {
                    configuration.setName(generatedName);
                }
                sourceElement.set(elementRunInfo.getEnclosingTestElement());
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    protected boolean isConfigurationFromCompatibleContext(@NotNull AlsatianRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement element = context.getPsiLocation();
        if (element == null) {
            return false;
        } else {
            TestElementInfo elementRunInfo = createTestElementRunInfo(element, context, configuration.getRunSettings());
            if (elementRunInfo == null) {
                return false;
            } else {
                AlsatianRunSettings thisRunSettings = elementRunInfo.getRunSettings();
                AlsatianRunSettings thatRunSettings = configuration.getRunSettings();
                if (thisRunSettings.getScopeKind() != thatRunSettings.getScopeKind()) {
                    return false;
                } else {
                    if (thisRunSettings.getScopeKind() == AlsatianScopeKind.WILDCARD) {
                        return thatRunSettings.getListOfGlobs().equals(thisRunSettings.getListOfGlobs());
                    }
                    else {
                        return thatRunSettings.getAlsatianExecutionPlan().equals(thisRunSettings.getAlsatianExecutionPlan());
                    }
                }
            }
        }
    }

    private static class TestElementInfo {
        private final AlsatianRunSettings myRunSettings;
        private final PsiElement myEnclosingTestElement;

        TestElementInfo(@NotNull AlsatianRunSettings runSettings, @NotNull PsiElement enclosingTestElement) {
            super();
            if (runSettings.getWorkingDir().trim().isEmpty()) {
                VirtualFile file = (VirtualFile) Objects.requireNonNull(PsiUtilCore.getVirtualFile(enclosingTestElement));
                runSettings = runSettings.toBuilder().setWorkingDir(AlsatianRunConfigurationProducer.guessWorkingDir(enclosingTestElement.getProject(), file)).build();
            }

            this.myRunSettings = runSettings;
            this.myEnclosingTestElement = enclosingTestElement;
        }

        @NotNull
        public AlsatianRunSettings getRunSettings() {
            return this.myRunSettings;
        }

        @NotNull
        public PsiElement getEnclosingTestElement() {
            return this.myEnclosingTestElement;
        }
    }

    @NotNull
    private static String guessWorkingDir(@NotNull Project project, @NotNull VirtualFile contextFile) {
        VirtualFile configFile = JSProjectUtil.findFileUpToContentRoot(project, contextFile, new String[]{"package.json"});
        VirtualFile workingDir = configFile != null ? configFile.getParent() : null;
        if (workingDir == null) {
            workingDir = contextFile.getParent();
        }

        return workingDir != null ? workingDir.getPath() : "";
    }

    private static TestElementInfo createTestElementRunInfo(@NotNull PsiElement element, ConfigurationContext context, @NotNull AlsatianRunSettings templateRunSettings) {
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
        if (virtualFile == null) {
            return null;
        } else {
            JsTestElementPath tep = createSuiteOrTestData(element);
            if (tep == null) {
                return createFileInfo(element, virtualFile, templateRunSettings);
            } else {
                AlsatianRunSettings.Builder builder = templateRunSettings.toBuilder();
                AlsatianExecutionPlan alsatianExecutionPlan = new AlsatianExecutionPlan();

                String relativeTestPathString = toRelativePath(virtualFile, context);
                AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = alsatianExecutionPlan.addOrGetTestFile(relativeTestPathString);
                if (tep.getTestElement() instanceof TypeScriptFunction) {
                    String testName = tep.getTestName();
                    if (testName != null) {
                        builder.setScopeKind(AlsatianScopeKind.TEST);
                        TypeScriptFunction function = (TypeScriptFunction) tep.getTestElement();
                        PsiElement parent = function.getParent();
                        TypeScriptClass typeScriptClass = ObjectUtils.tryCast(parent, TypeScriptClass.class);
                        String className = typeScriptClass.getName();
                        AlsatianTestSuiteExecutionPlan suite = alsatianTestFileExecutionPlan.addOrGetSuite(className);
                        suite.addOrGetTest(testName);
                    }
                } else if (tep.getTestElement() instanceof TypeScriptClass) {
                    builder.setScopeKind(AlsatianScopeKind.SUITE);
                    TypeScriptClass typeScriptClass = ObjectUtils.tryCast(tep.getTestElement(), TypeScriptClass.class);
                    String className = typeScriptClass.getName();
                    AlsatianTestSuiteExecutionPlan suite = alsatianTestFileExecutionPlan.addOrGetSuite(className);
                } else {
                    return null;
                }
                builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
                return new TestElementInfo(builder.build(), tep.getTestElement());
            }
        }
    }

    private static String toRelativePath(VirtualFile virtualFile, ConfigurationContext context) {
        Project project = context.getProject();
        return toRelativePath(virtualFile, project);
    }

    private static String toRelativePath(VirtualFile virtualFile, Project project) {
        String testPathIndependent = FileUtil.toSystemIndependentName(virtualFile.getPath());
        Path testPath = Paths.get(testPathIndependent);
        Path projectPath = Paths.get(project.getBasePath());
        Path relativeTestPath = projectPath.relativize(testPath);
        String relativePathSystemDependent = relativeTestPath.toString();
        return FileUtil.toSystemIndependentName(relativePathSystemDependent);
    }

    @Nullable
    private static AlsatianRunConfigurationProducer.TestElementInfo createFileInfo(@NotNull PsiElement element, @NotNull VirtualFile virtualFile, @NotNull AlsatianRunSettings templateRunSettings) {
        JSFile jsFile = (JSFile) ObjectUtils.tryCast(element.getContainingFile(), JSFile.class);
        if (jsFile != null && AlsatianFileStructureBuilder.isAlsatianTestFile(jsFile)) {
            AlsatianRunSettings.Builder builder = templateRunSettings.toBuilder();
            builder.setScopeKind(AlsatianScopeKind.TEST_FILE);
            AlsatianExecutionPlan alsatianExecutionPlan = new AlsatianExecutionPlan();
            String testPathIndependent = FileUtil.toSystemIndependentName(virtualFile.getPath());
            alsatianExecutionPlan.addOrGetTestFile(testPathIndependent);
            builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
            return new TestElementInfo(builder.build(), jsFile);
        }
        PsiDirectory psiDirectory = (PsiDirectory) ObjectUtils.tryCast(element, PsiDirectory.class);
        if (psiDirectory != null) {
            AlsatianRunSettings.Builder builder = templateRunSettings.toBuilder();
            builder.setScopeKind(AlsatianScopeKind.WILDCARD);
            AlsatianExecutionPlan alsatianExecutionPlan = new AlsatianExecutionPlan();
            builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
            VirtualFile testDirectory = psiDirectory.getVirtualFile();
            String testDirectoryRelativePath = toRelativePath(testDirectory, element.getProject());
            AlsatianPluginSettingsState settings = AlsatianPluginSettingsState.getInstance(element.getProject());
            builder.setListOfGlobs(Collections.singletonList("./" + testDirectoryRelativePath + "/" + settings.defaultDirectoryWildcard));
            return new TestElementInfo(builder.build(), psiDirectory);
        }
        return null;
    }

    @Nullable
    private static JsTestElementPath createSuiteOrTestData(@NotNull PsiElement element) {
        if (element instanceof PsiFileSystemItem) {
            return null;
        } else {
            PsiFile containingFile = element.getContainingFile();
            JSFile jsFile = (JSFile) ObjectUtils.tryCast(containingFile, JSFile.class);
            TextRange textRange = element.getTextRange();
            if (jsFile != null && textRange != null) {
                AlsatianFileStructure alsatianFileStructure = (AlsatianFileStructure) AlsatianFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
                return alsatianFileStructure.findTestElementPath(textRange);
            } else {
                return null;
            }
        }
    }

    @NotNull
    public ConfigurationFactory getConfigurationFactory() {
        return AlsatianConfigurationType.getInstance();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull AlsatianRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        return super.setupConfigurationFromContext(configuration, context, sourceElement);
    }
}
