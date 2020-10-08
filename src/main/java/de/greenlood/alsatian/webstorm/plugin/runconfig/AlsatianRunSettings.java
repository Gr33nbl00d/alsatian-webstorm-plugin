package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.util.io.FileUtil;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestFileExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestMethodExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestSuiteExecutionPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlsatianRunSettings {
    private final NodeJsInterpreterRef myInterpreterRef;
    private final String myNodeOptions;
    private final NodePackage alsatianPackage;
    private final String myWorkingDir;
    private final EnvironmentVariablesData myEnvData;
    private final AlsatianScopeKind scopeKind;
    private final AlsatianExecutionPlan alsatianExecutionPlan;
    private final List<String> listOfGlobs;

    public AlsatianRunSettings(@NotNull AlsatianRunSettings.Builder builder) {
        super();
        this.myInterpreterRef = builder.myInterpreterRef;
        this.myNodeOptions = builder.myNodeOptions;
        this.alsatianPackage = builder.alsatianPackage;
        this.myWorkingDir = FileUtil.toSystemDependentName(builder.myWorkingDir);
        this.myEnvData = builder.myEnvData;
        this.scopeKind = builder.scopeKind;
        this.alsatianExecutionPlan = builder.alsatianExecutionPlan;
        this.listOfGlobs = builder.listOfGlobs;
    }

    public AlsatianExecutionPlan getAlsatianExecutionPlan() {
        return alsatianExecutionPlan;
    }

    @NotNull
    public NodeJsInterpreterRef getInterpreterRef() {
        NodeJsInterpreterRef var10000 = this.myInterpreterRef;
        return var10000;
    }

    @NotNull
    public String getNodeOptions() {
        return this.myNodeOptions;
    }

    @NotNull
    public AlsatianScopeKind getScopeKind() {
        return scopeKind;
    }

    @Nullable
    public NodePackage getAlsatianPackage() {
        return this.alsatianPackage;
    }

    @NotNull
    public String getWorkingDir() {
        return this.myWorkingDir;
    }

    @NotNull
    public EnvironmentVariablesData getEnvData() {
        return this.myEnvData;
    }

    @NotNull
    public AlsatianRunSettings.Builder toBuilder() {
        return (new Builder())
                .setInterpreterRef(this.myInterpreterRef)
                .setNodeOptions(this.myNodeOptions)
                .setAlsatianPackage(this.alsatianPackage)
                .setWorkingDir(this.myWorkingDir)
                .setEnvData(this.myEnvData)
                .setScopeKind(this.scopeKind)
                .setAlsatianExecutionPlan(alsatianExecutionPlan)
                .setListOfGlobs(listOfGlobs);
    }

    public List<String> getListOfGlobs() {
        return listOfGlobs;
    }


    @NotNull
    public static AlsatianRunSettings.Builder builder() {
        return new Builder();
    }

    public String getGeneratedName() {
        if (this.scopeKind == AlsatianScopeKind.WILDCARD)
            return "Tests in " + listOfGlobs;
        else if (this.scopeKind == AlsatianScopeKind.TEST_FILE) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = this.alsatianExecutionPlan.getTestFileExecutionPlans().get(0);
            return new File(alsatianTestFileExecutionPlan.getLocationUrl()).getName();
        } else if (this.scopeKind == AlsatianScopeKind.SUITE) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = this.alsatianExecutionPlan.getTestFileExecutionPlans().get(0);
            AlsatianTestSuiteExecutionPlan alsatianTestSuiteExecutionPlan = alsatianTestFileExecutionPlan.getTestSuiteExecutionPlans().get(0);
            return alsatianTestSuiteExecutionPlan.getSuiteName();
        } else if (this.scopeKind == AlsatianScopeKind.TEST) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = this.alsatianExecutionPlan.getTestFileExecutionPlans().get(0);
            AlsatianTestSuiteExecutionPlan alsatianTestSuiteExecutionPlan = alsatianTestFileExecutionPlan.getTestSuiteExecutionPlans().get(0);
            AlsatianTestMethodExecutionPlan methodExecutionPlan = alsatianTestSuiteExecutionPlan.getTestMethodExecutionPlans().get(0);
            return methodExecutionPlan.getTestName();
        }
        return null;
    }

    public static class Builder {
        private NodeJsInterpreterRef myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
        private String myNodeOptions = "";
        private NodePackage alsatianPackage = null;
        private String myWorkingDir = "";
        private EnvironmentVariablesData myEnvData;
        private AlsatianScopeKind scopeKind = AlsatianScopeKind.WILDCARD;
        private AlsatianExecutionPlan alsatianExecutionPlan;
        private List<String> listOfGlobs = new ArrayList<>();

        public Builder() {
            this.myEnvData = EnvironmentVariablesData.DEFAULT;
        }

        @NotNull
        public AlsatianRunSettings.Builder setInterpreterRef(@NotNull NodeJsInterpreterRef interpreterRef) {
            this.myInterpreterRef = interpreterRef;
            return this;
        }

        @NotNull
        public AlsatianRunSettings.Builder setNodeOptions(@NotNull String nodeOptions) {
            this.myNodeOptions = nodeOptions;
            return this;
        }

        @NotNull
        public AlsatianRunSettings.Builder setAlsatianPackage(@Nullable NodePackage alsatianPackage) {
            this.alsatianPackage = alsatianPackage;
            return this;
        }


        @NotNull
        public AlsatianRunSettings.Builder setWorkingDir(@NotNull String workingDir) {
            this.myWorkingDir = workingDir;
            return this;
        }

        @NotNull
        public AlsatianRunSettings.Builder setEnvData(@NotNull EnvironmentVariablesData envData) {
            this.myEnvData = envData;
            return this;
        }

        public AlsatianRunSettings build() {
            return new AlsatianRunSettings(this);
        }

        public Builder setScopeKind(AlsatianScopeKind scopeKind) {

            this.scopeKind = scopeKind;
            return this;
        }

        public Builder setAlsatianExecutionPlan(AlsatianExecutionPlan alsatianTestFileExecutionPlan) {
            this.alsatianExecutionPlan = alsatianTestFileExecutionPlan;
            return this;
        }

        public Builder setListOfGlobs(List<String> listOfGlobs) {
            this.listOfGlobs = listOfGlobs;
            return this;
        }
    }

}
