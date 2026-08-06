package org.hostrunner.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 配置管理标签页 — 树形展示 + 工具栏按钮 + 右键菜单 + 拖拽移动
 */
public class ConfigurationManagementPanel extends JPanel {

    private final HostConfigurationService service;
    private final Consumer<Void> refreshCallback;
    private final com.intellij.openapi.project.Project project;
    private ConfigurationManagementTree managementTree;
    private MessageBusConnection messageBusConnection;

    public ConfigurationManagementPanel(com.intellij.openapi.project.Project project, Consumer<Void> refreshCallback) {
        this.service = HostConfigurationService.getInstance();
        this.refreshCallback = refreshCallback;
        this.project = project;
        initializeComponents();
        setupMessageBusSubscription();
    }

    private void setupMessageBusSubscription() {
        messageBusConnection = ApplicationManager.getApplication()
            .getMessageBus().connect(project);
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                if (projectName.equals(project.getName())) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    refreshTree();
                    if (refreshCallback != null) {
                        refreshCallback.accept(null);
                    }
                    HostConfigurationStatusWidget.updateStatus(project);
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // 工具栏按钮
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton addButton = new JButton("新建配置");
        addButton.addActionListener(e -> managementTree.addConfiguration());
        addButton.setFocusPainted(false);
        toolbarPanel.add(addButton);

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshTree());
        refreshButton.setFocusPainted(false);
        toolbarPanel.add(refreshButton);

        add(toolbarPanel, BorderLayout.NORTH);

        // 管理树
        managementTree = new ConfigurationManagementTree();
        managementTree.setRefreshCallback(v -> {
            refreshTree();
            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }
            HostConfigurationStatusWidget.updateStatus(project);
        });

        JScrollPane scrollPane = new JScrollPane(managementTree);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        refreshTree();
    }

    public void refreshTree() {
        managementTree.refresh();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }
}
