package org.hostrunner.toolwindow;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.settings.HostConfigurationForm;
import org.hostrunner.settings.HostConfigurationTableModel;
import org.hostrunner.toolwindow.HostConfigurationStatusWidget;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 配置管理标签页
 */
public class ConfigurationManagementPanel extends JPanel {

    private JBTable configurationTable;
    private HostConfigurationTableModel tableModel;
    private final HostConfigurationService service;
    private final Consumer<Void> refreshCallback;
    private final com.intellij.openapi.project.Project project;

    public ConfigurationManagementPanel(com.intellij.openapi.project.Project project, Consumer<Void> refreshCallback) {
        this.service = HostConfigurationService.getInstance();
        this.refreshCallback = refreshCallback;
        this.project = project;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // 创建表格模型
        tableModel = new HostConfigurationTableModel();
        configurationTable = new JBTable(tableModel);
        configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 创建工具栏装饰器
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(configurationTable);
        decorator.setAddAction(anActionButton -> addConfiguration());
        decorator.setEditAction(anActionButton -> editConfiguration());
        decorator.setRemoveAction(anActionButton -> removeConfiguration());

        add(decorator.createPanel(), BorderLayout.CENTER);

        // 初始刷新表格
        refreshTable();
    }

    private void addConfiguration() {
        HostConfigurationForm form = new HostConfigurationForm(null);
        if (form.showAndGet()) {
            HostConfiguration config = form.getConfiguration();
            if (validateConfiguration(config, null)) {
                service.addConfiguration(config);
                refreshTable();
                // 通知其他标签页刷新
                if (refreshCallback != null) {
                    refreshCallback.accept(null);
                }
                // 更新状态栏显示
                HostConfigurationStatusWidget.updateStatus(project);
            }
        }
    }


    private void editConfiguration() {
        int selectedRow = configurationTable.getSelectedRow();
        if (selectedRow >= 0) {
            HostConfiguration config = tableModel.getConfigurationAt(selectedRow);
            HostConfigurationForm form = new HostConfigurationForm(config);
            if (form.showAndGet()) {
                HostConfiguration updatedConfig = form.getConfiguration();
                if (validateConfiguration(updatedConfig, config.getId())) {
                    service.updateConfiguration(updatedConfig);
                    refreshTable();
                    // 通知其他标签页刷新
                    if (refreshCallback != null) {
                        refreshCallback.accept(null);
                    }
                    // 更新状态栏显示
                    HostConfigurationStatusWidget.updateStatus(project);
                }
            }
        }
    }

    private void removeConfiguration() {
        int selectedRow = configurationTable.getSelectedRow();
        if (selectedRow >= 0) {
            HostConfiguration config = tableModel.getConfigurationAt(selectedRow);
            int result = Messages.showYesNoDialog(
                "确定要删除配置 '" + config.getName() + "' 吗？",
                "删除配置",
                Messages.getQuestionIcon()
            );

            if (result == Messages.YES) {
                service.removeConfiguration(config.getId());
                refreshTable();
                // 通知其他标签页刷新
                if (refreshCallback != null) {
                    refreshCallback.accept(null);
                }
                // 更新状态栏显示
                HostConfigurationStatusWidget.updateStatus(project);
            }
        }
    }

    private boolean validateConfiguration(HostConfiguration config, String excludeId) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            Messages.showErrorDialog("配置名称不能为空", "验证错误");
            return false;
        }

        if (service.isNameDuplicate(config.getName(), excludeId)) {
            Messages.showErrorDialog("配置名称 '" + config.getName() + "' 已存在", "验证错误");
            return false;
        }

        return true;
    }

    public void refreshTable() {
        tableModel.refreshData(service.getAllConfigurations());
    }
}