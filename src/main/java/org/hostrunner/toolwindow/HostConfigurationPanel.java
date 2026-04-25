package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.toolwindow.HostConfigurationStatusWidget;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 工具窗口主面板 - 双标签页结构
 */
public class HostConfigurationPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JTabbedPane tabbedPane;
    private ConfigurationManagementPanel managementPanel;
    private ConfigurationSelectionPanel selectionPanel;

    public HostConfigurationPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建标签页容器
        tabbedPane = new JTabbedPane();

        // 创建配置管理标签页
        managementPanel = new ConfigurationManagementPanel(project, this::onConfigurationChanged);

        // 创建配置选择标签页
        selectionPanel = new ConfigurationSelectionPanel(project);

        // 添加标签页（配置选择作为默认第一个标签页）
        tabbedPane.addTab("配置选择", selectionPanel);
        tabbedPane.addTab("配置管理", managementPanel);

        // 添加标签页切换监听器
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 当切换到配置选择标签页时，验证当前选择
                if (tabbedPane.getSelectedIndex() == 0) { // 配置选择标签页是第一个
                    selectionPanel.validateSelection();
                }
                // 更新状态栏显示
                HostConfigurationStatusWidget.updateStatus(project);
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void onConfigurationChanged(Void unused) {
        // 当配置管理标签页发生更改时，刷新选择标签页
        selectionPanel.refresh();
        // 更新状态栏显示
        HostConfigurationStatusWidget.updateStatus(project);
    }

    public void refresh() {
        // 刷新所有标签页
        managementPanel.refreshTable();
        selectionPanel.refresh();
    }
}