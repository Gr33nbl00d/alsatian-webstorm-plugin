package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AlsatianRunSettingsEditor extends SettingsEditor<AlsatianRunConfiguration> {
    private final AlsatianRunSettingsPanel alsatianRunSettingsPanel;

    public AlsatianRunSettingsEditor(@NotNull Project project) {
        this.alsatianRunSettingsPanel = new AlsatianRunSettingsPanel();
        alsatianRunSettingsPanel.init(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull AlsatianRunConfiguration alsatianRunConfiguration) {
        alsatianRunSettingsPanel.resetEditorFrom(alsatianRunConfiguration);
    }

    @Override
    protected void applyEditorTo(@NotNull AlsatianRunConfiguration alsatianRunConfiguration) throws ConfigurationException {
        alsatianRunSettingsPanel.applyEditorTo(alsatianRunConfiguration);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return this.alsatianRunSettingsPanel;
    }
}
