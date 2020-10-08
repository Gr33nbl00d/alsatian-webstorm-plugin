package de.greenlood.alsatian.webstorm.plugin.pluginconfig;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AlsatianPluginSettings implements Configurable {

    private AlsatianPluginSettingsPanel panel;
    private Project project;

    public AlsatianPluginSettings(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Alsatian";
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.panel = new AlsatianPluginSettingsPanel();
        return panel;
    }

    @Override
    public boolean isModified() {
        AlsatianPluginSettingsState settings = AlsatianPluginSettingsState.getInstance(project);
        return panel.isModified(settings);
    }

    @Override
    public void apply() throws ConfigurationException {
        AlsatianPluginSettingsState settings = AlsatianPluginSettingsState.getInstance(project);
        this.panel.apply(settings);
    }

    @Override
    public void reset() {
        AlsatianPluginSettingsState settings = AlsatianPluginSettingsState.getInstance(project);
        this.panel.reset(settings);
    }

    @Override
    public void disposeUIResources() {
        panel=null;
    }
}
