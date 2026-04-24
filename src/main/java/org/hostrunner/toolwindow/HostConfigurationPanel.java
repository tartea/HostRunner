package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.settings.HostConfigurationConfigurable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 工具窗口主面板
 */
public class HostConfigurationPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JPanel cardsPanel;
    private JButton refreshButton;
    private ButtonGroup selectionGroup;

    public HostConfigurationPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        refreshConfigurations();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // 刷新按钮
        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshConfigurations());
        refreshButton.setFocusPainted(false); // 移除焦点边框
        topPanel.add(refreshButton);

        // 设置按钮
        JButton settingsButton = new JButton("设置");
        settingsButton.addActionListener(e -> openSettings());
        settingsButton.setFocusPainted(false); // 移除焦点边框
        topPanel.add(settingsButton);

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

        if (configurations.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无配置，请先在设置中添加配置");
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

    private void openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Host Configuration");
        // 设置页面关闭后刷新配置列表
        refreshConfigurations();
    }

    private void onConfigurationSelected(HostConfiguration configuration) {
        if (configuration != null) {
            service.selectConfiguration(configuration.getId());
            // 立即更新文件内容
            updateHostsFileImmediately(configuration);
        } else {
            service.deselectConfiguration();
            // 清空文件内容
            clearHostsFileImmediately();
        }
        // 立即更新UI状态
        SwingUtilities.invokeLater(this::updateSelectionState);
    }

    private void updateSelectionState() {
        // 更新所有卡片的选中状态显示
        for (Component comp : cardsPanel.getComponents()) {
            if (comp instanceof ConfigurationCard) {
                ConfigurationCard card = (ConfigurationCard) comp;
                HostConfiguration selectedConfig = service.getSelectedConfiguration();
                boolean isSelected = selectedConfig != null &&
                    selectedConfig.getId().equals(card.getConfiguration().getId());
                card.setSelected(isSelected);
            }
        }
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
}