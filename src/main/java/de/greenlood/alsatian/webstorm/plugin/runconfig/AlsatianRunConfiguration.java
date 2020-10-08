package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.JSRunProfileWithCompileBeforeLaunchOption;
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import de.greenlood.alsatian.webstorm.plugin.AlsatianConfigurationType;
import de.greenlood.alsatian.webstorm.plugin.AlsatianRunProfileState;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianRunConfiguration extends LocatableConfigurationBase<RunConfigurationOptions> implements NodeDebugRunConfiguration, JSRunProfileWithCompileBeforeLaunchOption {
    private final NodeJsInterpreterRef myInterpreterRef;
    private AlsatianPackageDescriptorFactory alsatianPackageDescriptorFactory = new AlsatianPackageDescriptorFactory();
    private AlsatianRunSettings mySettings;
    private boolean myWorkingDirectoryDetected;
    AlsatianRunConfigurationPersister alsatianRunConfigurationPersister = new AlsatianRunConfigurationPersister();

    public AlsatianRunConfiguration(Project project, AlsatianConfigurationType factory, String name) {
        super(project, factory, name);
        this.myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
        this.mySettings = AlsatianRunSettings.builder().build();
    }

    @NotNull
    public AlsatianRunSettings getRunSettings() {
        return this.mySettings;
    }

    public void setRunSettings(@NotNull AlsatianRunSettings runSettings) {
        this.mySettings = runSettings;
        if(this.getName() == null || this.getName().equals("")) {
            this.setName(runSettings.getGeneratedName());
        }
    }


    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new AlsatianRunSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new AlsatianRunProfileState(getProject(),
                this,
                env);
    }

    public NodeJsInterpreterRef getInterpreterRef() {
        return myInterpreterRef;
    }

    public NodePackage getAlsatianPackage() {
        if (RunManager.getInstance(this.getProject()).isTemplate(this)) {

            return (NodePackage) ObjectUtils.notNull(this.mySettings.getAlsatianPackage(), new NodePackage(""));
        } else {
            NodePackage pkg = this.mySettings.getAlsatianPackage();
            if (pkg == null) {
                Project project = this.getProject();
                NodePackageDescriptor packageDescriptor = alsatianPackageDescriptorFactory.getPackageDescriptor();
                VirtualFile f = findFile(this.mySettings.getWorkingDir());
                pkg = packageDescriptor.findFirstDirectDependencyPackage(project, (NodeJsInterpreter) null, f);
                if (pkg.isEmptyPath()) {
                    pkg = getAlsatianPackage(project);
                } else {
                    setAlsatianPackage(project, pkg);
                }

                this.mySettings = this.mySettings.toBuilder().setAlsatianPackage(pkg).build();
            }
            return pkg;
        }

    }

    @Nullable
    private static VirtualFile findFile(@NotNull String path) {
        return FileUtil.isAbsolute(path) ? LocalFileSystem.getInstance().findFileByPath(path) : null;
    }

    @NotNull
    public static NodePackage getAlsatianPackage(@NotNull Project project) {
        String packagePath = getPackagePath(project);
        return new NodePackage(StringUtil.notNullize(packagePath));
    }

    @Nullable
    private static String getPackagePath(@NotNull Project project) {
        return PropertiesComponent.getInstance(project).getValue("nodejs.alsatian.alsatian_package");
    }

    public static void setAlsatianPackage(@NotNull Project project, @NotNull NodePackage jestPackage) {
        setPackagePath(project, jestPackage.getSystemIndependentPath());
    }

    private static void setPackagePath(@NotNull Project project, @NotNull String value) {
        PropertiesComponent.getInstance(project).setValue("nodejs.alsatian.alsatian_package", value);
    }

    @Override
    public void onNewConfigurationCreated() {
        if (mySettings.getScopeKind() == null) {
            mySettings = mySettings.toBuilder().setScopeKind(AlsatianScopeKind.WILDCARD).build();
        }
        detectWorkingDirectoryIfNeeded();
    }

    private void detectWorkingDirectoryIfNeeded() {
        if (!myWorkingDirectoryDetected
                && StringUtil.isEmptyOrSpaces(mySettings.getWorkingDir())
                && !getProject().isDefault()
                && !RunManager.getInstance(getProject()).isTemplate(this)) {
            VirtualFile workingDir = JsTestRunConfigurationProducer.guessWorkingDirectory(getProject(), "");
            if (workingDir != null) {
                String workingDirectory = workingDir.getPath();
                mySettings = mySettings.toBuilder().setWorkingDir(workingDirectory).build();
            }
        }
        myWorkingDirectoryDetected = true;
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        this.mySettings = alsatianRunConfigurationPersister.read(element,getProject());
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        alsatianRunConfigurationPersister.write(element,getProject(),getRunSettings());
    }
}
