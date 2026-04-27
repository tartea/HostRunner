package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.toolwindow.HostConfigurationStatusWidget;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 配置选择标签页
 */
public class ConfigurationSelectionPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JPanel cardsPanel;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton viewCurrentButton;
    private ButtonGroup selectionGroup;
    private MessageBusConnection messageBusConnection;

    public ConfigurationSelectionPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        setupMessageBusSubscription();
        refreshConfigurations();
    }

    private void setupMessageBusSubscription() {
        // 订阅消息总线以接收配置变更通知
        messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                // 避免处理自己发送的消息（可选优化）
                if (projectName.equals(project.getName())) {
                    return;
                }

                // 在EDT中执行UI更新
                SwingUtilities.invokeLater(() -> {
                    refreshConfigurations();
                    validateSelection();
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // 刷新按钮
        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshConfigurations());
        refreshButton.setFocusPainted(false);
        topPanel.add(refreshButton);

        // 清空选择按钮
        clearButton = new JButton("清空选择");
        clearButton.addActionListener(e -> clearAllSelections());
        clearButton.setFocusPainted(false);
        topPanel.add(clearButton);

        // 查看当前配置按钮
        viewCurrentButton = new JButton("查看当前配置");
        viewCurrentButton.addActionListener(e -> showCurrentConfigurationDetail());
        viewCurrentButton.setFocusPainted(false);
        topPanel.add(viewCurrentButton);

        add(topPanel, BorderLayout.NORTH);

        // 卡片面板
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshConfigurations() {
        // 清空现有卡片
        cardsPanel.removeAll();
        selectionGroup = new ButtonGroup();

        List<HostConfiguration> configurations = service.getAllConfigurations();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        // 检查当前选中的配置是否存在，如果不存在则清除选择
        if (selectedConfig != null) {
            final String selectedConfigId = selectedConfig.getId();
            boolean configExists = configurations.stream()
                .anyMatch(config -> config.getId().equals(selectedConfigId));

            if (!configExists) {
                // 选中的配置不存在，清除选择并清空hosts文件
                service.deselectConfiguration();
                clearHostsFileImmediately();
                selectedConfig = null; // 更新本地引用
            }
        }

        // 清空按钮现在始终可用，不需要更新状态

        if (configurations.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无配置，请先在配置管理中添力配置");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            cardsPanel.add(emptyLabel);
        } else {
            for (HostConfiguration config : configurations) {
                ConfigurationCard card = new ConfigurationCard(
                    config,
                    selectedConfig != null && selectedConfig.getId().equals(config.getId()),
                    selectionGroup,
                    this::onConfigurationSelected
                );
                cardsPanel.add(card);
                cardsPanel.add(Box.createVerticalStrut(5));
            }
        }

        // 强制更新选择状态
        updateSelectionState();

        // 重新绘制
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }


    private void onConfigurationSelected(HostConfiguration configuration) {
        if (configuration != null) {
            service.selectConfiguration(configuration.getId());
            // 立即更新文件内容
            updateHostsFileImmediately(configuration);
            // 更新清空按钮状态
            updateClearButtonState();
            // 更新状态显示
            // 更新状态栏显示
            HostConfigurationStatusWidget.updateStatus(project);
        } else {
            service.deselectConfiguration();
            // 清空文件内容
            clearHostsFileImmediately();
            // 更新清空按钮状态
            updateClearButtonState();
            // 更新状态显示
            // 更新状态栏显示
            HostConfigurationStatusWidget.updateStatus(project);
        }
        // 立即更新UI状态
        SwingUtilities.invokeLater(this::updateSelectionState);
    }

    private void clearAllSelections() {
        // 取消所有选择
        service.deselectConfiguration();
        // 立即清空hosts文件
        clearHostsFileImmediately();
        // 清除按钮组的选择状态
        if (selectionGroup != null) {
            selectionGroup.clearSelection();
        }
        // 强制刷新整个配置列表以确保UI状态正确
        refreshConfigurations();
    }

    private void updateClearButtonState() {
        // 清空按钮始终可用，不需要根据选择状态来启用/禁用
        clearButton.setEnabled(true);
        clearButton.setToolTipText("清空所有选择");
    }

    private void updateSelectionState() {
        // 更新所有卡片的选中状态显示
        HostConfiguration selectedConfig = service.getSelectedConfiguration();
        boolean hasSelection = selectedConfig != null;

        for (Component comp : cardsPanel.getComponents()) {
            if (comp instanceof ConfigurationCard) {
                ConfigurationCard card = (ConfigurationCard) comp;
                boolean isSelected = hasSelection && selectedConfig.getId().equals(card.getConfiguration().getId());
                card.setSelected(isSelected);
            }
        }

        // 更新清空按钮状态
        updateClearButtonState();
        // 更新状态栏显示
        HostConfigurationStatusWidget.updateStatus(project);
    }

    private void updateHostsFileImmediately(HostConfiguration configuration) {
        try {
            org.hostrunner.springboot.HostsFileManager.updateHostsFile(project, configuration.getHostsContent());
        } catch (Exception e) {
            // 静默处理错误，避免影响用户体验
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }
    }

    private void clearHostsFileImmediately() {
        try {
            org.hostrunner.springboot.HostsFileManager.clearHostsFile(project);
        } catch (Exception e) {
            // 静默处理错误
            System.err.println("清空hosts文件失败: " + e.getMessage());
        }
    }

    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // 清理消息总线连接
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }

    /**
     * 验证当前选中的配置是否存在，用于标签页切换时调用
     */
    public void validateSelection() {
        List<HostConfiguration> configurations = service.getAllConfigurations();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (selectedConfig != null) {
            final String selectedConfigId = selectedConfig.getId();
            boolean configExists = configurations.stream()
                .anyMatch(config -> config.getId().equals(selectedConfigId));

            if (!configExists) {
                // 选中的配置不存在，清除选择并清空hosts文件
                service.deselectConfiguration();
                clearHostsFileImmediately();
                // 更新UI状态
                SwingUtilities.invokeLater(this::updateSelectionState);
            }
        }
    }

    private void showCurrentConfigurationDetail() {
        // 显示工具窗口内容的完整对话框
        ToolWindowContentDialog dialog = new ToolWindowContentDialog(project);
        dialog.show();
    }

}