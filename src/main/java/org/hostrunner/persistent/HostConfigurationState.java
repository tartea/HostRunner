package org.hostrunner.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.hostrunner.model.HostConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局配置状态持久化组件
 */
@State(
    name = "HostConfigurationState",
    storages = @Storage("hostrunner-config.xml")
)
public class HostConfigurationState implements PersistentStateComponent<HostConfigurationState> {

    private List<HostConfiguration> configurations = new ArrayList<>();
    private String selectedConfigurationId; // 当前选中的配置ID

    public static HostConfigurationState getInstance() {
        return ApplicationManager.getApplication()
            .getService(HostConfigurationState.class);
    }

    public List<HostConfiguration> getConfigurations() {
        if (configurations == null) {
            configurations = new ArrayList<>();
        }
        return configurations;
    }

    public void setConfigurations(List<HostConfiguration> configurations) {
        this.configurations = configurations != null ? configurations : new ArrayList<>();
    }

    public String getSelectedConfigurationId() {
        return selectedConfigurationId;
    }

    public void setSelectedConfigurationId(String selectedConfigurationId) {
        this.selectedConfigurationId = selectedConfigurationId;
    }

    public HostConfiguration getSelectedConfiguration() {
        if (selectedConfigurationId == null) {
            return null;
        }
        return getConfigurations().stream()
            .filter(config -> selectedConfigurationId.equals(config.getId()))
            .findFirst()
            .orElse(null);
    }

    public boolean hasSelectedConfiguration() {
        return selectedConfigurationId != null && getSelectedConfiguration() != null;
    }

    @Nullable
    @Override
    public HostConfigurationState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HostConfigurationState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}