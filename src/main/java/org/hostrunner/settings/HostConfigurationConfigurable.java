package org.hostrunner.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Host配置设置页面
 */
public class HostConfigurationConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable configurationTable;
    private HostConfigurationTableModel tableModel;
    private final HostConfigurationService service;

    public HostConfigurationConfigurable() {
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());

        // 创建表格模型
        tableModel = new HostConfigurationTableModel();
        configurationTable = new JBTable(tableModel);
        configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 创建工具栏装饰器
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(configurationTable);
        decorator.setAddAction(anActionButton -> addConfiguration());
        decorator.setAddActionName("Add from Template");
        decorator.setEditAction(anActionButton -> editConfiguration());
        decorator.setRemoveAction(anActionButton -> removeConfiguration());

        // 添加模板按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton templateButton = new JButton("Add from Template");
        templateButton.addActionListener(e -> addFromTemplate());
        buttonPanel.add(templateButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
    }

    private void addConfiguration() {
        HostConfigurationForm form = new HostConfigurationForm(null);
        if (form.showAndGet()) {
            HostConfiguration config = form.getConfiguration();
            if (validateConfiguration(config, null)) {
                service.addConfiguration(config);
                refreshTable();
            }
        }
    }

    private void addFromTemplate() {
        TemplateSelectionDialog dialog = new TemplateSelectionDialog();
        if (dialog.showAndGet()) {
            HostConfiguration template = dialog.getSelectedTemplate();
            String newName = dialog.getNewConfigurationName();

            if (newName != null && !newName.trim().isEmpty()) {
                HostConfiguration newConfig = ConfigurationTemplate.createFromTemplate(template, newName);
                if (validateConfiguration(newConfig, null)) {
                    service.addConfiguration(newConfig);
                    refreshTable();
                }
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

    private void refreshTable() {
        tableModel.refreshData(service.getAllConfigurations());
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Host Configuration";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        // 简单实现，总是返回false，因为状态由service管理
        return false;
    }

    @Override
    public void apply() {
        // 不需要实现，因为配置是实时保存的
    }

    @Override
    public void reset() {
        refreshTable();
    }

    @Override
    public void disposeUIResources() {
        // 清理资源
        mainPanel = null;
        configurationTable = null;
        tableModel = null;
    }
}