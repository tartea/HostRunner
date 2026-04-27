package org.hostrunner.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.persistent.HostConfigurationState;
import org.hostrunner.messaging.HostConfigurationMessagePublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置管理服务
 */
@Service
public final class HostConfigurationService {

    private final HostConfigurationState state;
    private final HostConfigurationMessagePublisher messagePublisher;

    public HostConfigurationService() {
        this.state = HostConfigurationState.getInstance();
        this.messagePublisher = HostConfigurationMessagePublisher.getInstance();
    }

    public static HostConfigurationService getInstance() {
        return ApplicationManager.getApplication()
            .getService(HostConfigurationService.class);
    }

    /**
     * 获取所有配置
     */
    public List<HostConfiguration> getAllConfigurations() {
        return new ArrayList<>(state.getConfigurations());
    }

    /**
     * 添加新配置
     */
    public void addConfiguration(HostConfiguration configuration) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        configs.add(configuration);
        state.setConfigurations(configs);

        // 发布配置添加消息
        messagePublisher.publishConfigurationAdded(configuration.getId());
    }

    /**
     * 更新配置
     */
    public void updateConfiguration(HostConfiguration configuration) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getId().equals(configuration.getId())) {
                configs.set(i, configuration);
                break;
            }
        }
        state.setConfigurations(configs);

        // 发布配置更新消息
        messagePublisher.publishConfigurationUpdated(configuration.getId());
    }

    /**
     * 删除配置
     */
    public void removeConfiguration(String configurationId) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        configs.removeIf(config -> config.getId().equals(configurationId));
        state.setConfigurations(configs);

        // 如果删除的是当前选中的配置，清空选中状态
        if (configurationId.equals(state.getSelectedConfigurationId())) {
            state.setSelectedConfigurationId(null);
        }

        // 发布配置删除消息
        messagePublisher.publishConfigurationDeleted(configurationId);
    }

    /**
     * 根据ID获取配置
     */
    public HostConfiguration getConfigurationById(String configurationId) {
        return state.getConfigurations().stream()
            .filter(config -> config.getId().equals(configurationId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 选择配置
     */
    public void selectConfiguration(String configurationId) {
        // 验证配置是否存在
        HostConfiguration config = getConfigurationById(configurationId);
        if (config != null) {
            state.setSelectedConfigurationId(configurationId);

            // 发布配置选择消息
            messagePublisher.publishConfigurationSelected(configurationId);
        }
    }

    /**
     * 取消选择配置
     */
    public void deselectConfiguration() {
        state.setSelectedConfigurationId(null);

        // 发布配置取消选择消息
        messagePublisher.publishConfigurationSelected(null);
    }

    /**
     * 获取当前选中的配置
     */
    public HostConfiguration getSelectedConfiguration() {
        return state.getSelectedConfiguration();
    }

    /**
     * 检查是否有选中的配置
     */
    public boolean hasSelectedConfiguration() {
        return state.hasSelectedConfiguration();
    }

    /**
     * 验证配置名称是否重复
     */
    public boolean isNameDuplicate(String name, String excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return state.getConfigurations().stream()
            .anyMatch(config ->
                name.equals(config.getName()) &&
                !config.getId().equals(excludeId)
            );
    }

    /**
     * 获取配置数量
     */
    public int getConfigurationCount() {
        return state.getConfigurations().size();
    }

    /**
     * 清空所有配置
     */
    public void clearAllConfigurations() {
        state.setConfigurations(new ArrayList<>());
        state.setSelectedConfigurationId(null);
    }
}