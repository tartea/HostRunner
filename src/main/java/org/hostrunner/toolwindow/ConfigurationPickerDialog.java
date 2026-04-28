package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.springboot.HostsFileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 快速选择配置弹框 — 快捷键触发的模态对话框
 */
public class ConfigurationPickerDialog extends JDialog {

    private final ConfigurationPickerPanel pickerPanel;
    private final HostConfigurationService service;

    public ConfigurationPickerDialog() {
        super((Frame) null, "快速选择配置", true);

        this.service = HostConfigurationService.getInstance();

        // 选中回调：立即应用配置，不关闭弹框
        this.pickerPanel = new ConfigurationPickerPanel(this::onConfigurationSelected);

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        add(pickerPanel, BorderLayout.CENTER);

        // 底部关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ESC 键关闭
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setPreferredSize(new Dimension(500, 600));
        pack();
        setLocationRelativeTo(null);

        // 弹框打开后自动聚焦搜索框
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                pickerPanel.getSearchField().requestFocusInWindow();
            }
        });
    }

    private Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0];
        }
        return null;
    }

    private void onConfigurationSelected(HostConfiguration config) {
        if (config == null) return;

        // 选择配置
        service.selectConfiguration(config.getId());

        // 立即更新 hosts 文件
        try {
            HostsFileManager.updateHostsFile(config.getHostsContent());
        } catch (Exception e) {
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }

        // 更新状态栏
        Project project = getActiveProject();
        if (project != null) {
            HostConfigurationStatusWidget.updateStatus(project);
        }

        // 刷新卡片选中状态（不关闭弹框）
        pickerPanel.refresh();
    }
}
