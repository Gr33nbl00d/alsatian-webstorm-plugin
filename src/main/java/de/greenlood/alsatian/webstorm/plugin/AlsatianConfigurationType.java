package de.greenlood.alsatian.webstorm.plugin;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;

public class AlsatianConfigurationType extends SimpleConfigurationType implements DumbAware {

    public static final String NAME = "Alsatian";
    public static final String CONFIGURATION_NAME = NAME + "TestRunner";
    public static final String TEST_FRAMEWORK_NAME = NAME + "Framework";

    public AlsatianConfigurationType() {
        super(CONFIGURATION_NAME, NAME, NAME, NotNullLazyValue.createValue(() -> IconLoader.getIcon("/de/greenblood/alsatian/webstorm/plugin/paw-print.png")));
    }

    public static AlsatianConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(AlsatianConfigurationType.class);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new AlsatianRunConfiguration(project, this, NAME);
    }

    @NotNull
    @Override
    public String getTag() {
        return NAME;
    }

    @NotNull
    @Override
    public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }
}
