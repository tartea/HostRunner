package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.springboot.HostsFileManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 快速选择配置弹框 — 快捷键触发的模态对话框，分组Tab切换
 */
public class ConfigurationPickerDialog extends JDialog {

    private final HostConfigurationService service;
    private JTabbedPane groupTabs;
    private ConfigurationPickerPanel flatSearchPanel;
    private JTextField searchField;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private static final String TAB_CARD = "tabs";
    private static final String SEARCH_CARD = "search";

    public ConfigurationPickerDialog() {
        super((Frame) null, "快速选择配置", true);
        this.service = HostConfigurationService.getInstance();
        initializeUI();
        buildGroupTabs();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 搜索框
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        searchPanel.add(new JLabel("搜索:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // 内容区：CardLayout 切换 Tab 模式 / 搜索平铺模式
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Tab 模式
        JPanel tabsPanel = new JPanel(new BorderLayout());
        groupTabs = new JTabbedPane();
        groupTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabsPanel.add(groupTabs, BorderLayout.CENTER);
        contentPanel.add(tabsPanel, TAB_CARD);

        // 搜索平铺模式
        flatSearchPanel = new ConfigurationPickerPanel(this::onConfigurationSelected);
        flatSearchPanel.setShowGroupLabels(true);
        contentPanel.add(flatSearchPanel, SEARCH_CARD);

        add(contentPanel, BorderLayout.CENTER);

        // 底部关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ESC 关闭
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Ctrl+Tab / Ctrl+Shift+Tab 切换分组
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK), "nextTab");
        getRootPane().getActionMap().put("nextTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int count = groupTabs.getTabCount();
                if (count > 0) {
                    int next = (groupTabs.getSelectedIndex() + 1) % count;
                    groupTabs.setSelectedIndex(next);
                }
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "prevTab");
        getRootPane().getActionMap().put("prevTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int count = groupTabs.getTabCount();
                if (count > 0) {
                    int prev = (groupTabs.getSelectedIndex() - 1 + count) % count;
                    groupTabs.setSelectedIndex(prev);
                }
            }
        });

        setPreferredSize(new Dimension(950, 600));
        pack();
        setLocationRelativeTo(null);

        // 弹框打开后自动聚焦搜索框
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                searchField.requestFocusInWindow();
            }
        });
    }

    private void buildGroupTabs() {
        groupTabs.removeAll();
        List<String> groups = service.getGroups();
        // 只显示有配置的分组
        List<String> nonEmptyGroups = new java.util.ArrayList<>();
        for (String group : groups) {
            if (service.getGroupCount(group) > 0) {
                nonEmptyGroups.add(group);
            }
        }
        if (nonEmptyGroups.isEmpty()) {
            return;
        }
        for (String group : nonEmptyGroups) {
            ConfigurationPickerPanel tabPanel = new ConfigurationPickerPanel(this::onConfigurationSelected);
            tabPanel.setGroupFilter(group);
            tabPanel.refresh();
            groupTabs.addTab(group, tabPanel);
        }
    }

    private void onSearchChanged() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            cardLayout.show(contentPanel, TAB_CARD);
        } else {
            flatSearchPanel.refresh();
            cardLayout.show(contentPanel, SEARCH_CARD);
        }
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

        service.selectConfiguration(config.getId());

        try {
            HostsFileManager.updateHostsFile(config.getHostsContent());
        } catch (Exception e) {
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }

        Project project = getActiveProject();
        if (project != null) {
            HostConfigurationStatusWidget.updateStatus(project);
        }

        // 刷新所有 tab 面板的选中状态
        for (int i = 0; i < groupTabs.getTabCount(); i++) {
            Component comp = groupTabs.getComponentAt(i);
            if (comp instanceof ConfigurationPickerPanel) {
                ((ConfigurationPickerPanel) comp).refresh();
            }
        }
        flatSearchPanel.refresh();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            buildGroupTabs();
            flatSearchPanel.refresh();
        }
        super.setVisible(visible);
    }
}
