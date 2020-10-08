package de.greenlood.alsatian.webstorm.plugin.pluginconfig;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
        name = "de.greenlood.alsatian.jetbrains.plugin.executor.pluginconfig.AlsatianPluginSettingsState"
)
public class AlsatianPluginSettingsState implements PersistentStateComponent<AlsatianPluginSettingsState> {

    protected Project project;

    public String defaultDirectoryWildcard = "**/*.spec.ts";
    public String nycPackage = "node_modules/nyc/bin/nyc.js";

    public static AlsatianPluginSettingsState getInstance(Project project) {
        AlsatianPluginSettingsState service = ServiceManager.getService(project, AlsatianPluginSettingsState.class);
        service.project = project;
        return service;
    }

    @Nullable
    @Override
    public AlsatianPluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AlsatianPluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}