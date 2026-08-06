package org.hostrunner.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.springboot.HostsFileManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * 配置选择标签页 — 树形展示（分组节点 → 配置节点），只读选择
 */
public class ConfigurationSelectionPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton viewHostsButton;
    private ConfigurationSelectionTree selectionTree;
    private JTextField searchField;
    private MessageBusConnection messageBusConnection;

    public ConfigurationSelectionPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        setupMessageBusSubscription();
        selectionTree.refreshAndExpandToSelected();
    }

    private void setupMessageBusSubscription() {
        messageBusConnection = ApplicationManager.getApplication()
            .getMessageBus().connect(project);
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                if (!"SELECT".equals(changeType) && projectName.equals(project.getName())) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    selectionTree.refreshAndExpandToSelected();
                    validateSelection();
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshConfigurations());
        refreshButton.setFocusPainted(false);
        buttonPanel.add(refreshButton);

        clearButton = new JButton("清空选择");
        clearButton.addActionListener(e -> clearAllSelections());
        clearButton.setFocusPainted(false);
        buttonPanel.add(clearButton);

        viewHostsButton = new JButton("查看hosts文件");
        viewHostsButton.addActionListener(e -> viewHostsFile());
        viewHostsButton.setFocusPainted(false);
        buttonPanel.add(viewHostsButton);

        add(buttonPanel, BorderLayout.NORTH);

        // 搜索框
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("搜索:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTree(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTree(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTree(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(0, 5));
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // 树形配置选择器
        selectionTree = new ConfigurationSelectionTree();
        selectionTree.setSelectionCallback(this::onConfigurationSelected);

        JScrollPane scrollPane = new JScrollPane(selectionTree);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void filterTree() {
        selectionTree.filterTree(searchField.getText().trim());
    }

    private void refreshConfigurations() {
        selectionTree.refreshAndExpandToSelected();
    }

    private void onConfigurationSelected(HostConfiguration configuration) {
        if (configuration != null) {
            service.selectConfiguration(configuration.getId());
            updateHostsFileImmediately(configuration);
            HostConfigurationStatusWidget.updateStatus(project);
        }
    }

    private void clearAllSelections() {
        service.deselectConfiguration();
        clearHostsFileImmediately();
        selectionTree.refresh();
        HostConfigurationStatusWidget.updateStatus(project);
    }

    private void updateHostsFileImmediately(HostConfiguration configuration) {
        try {
            HostsFileManager.updateHostsFile(configuration.getHostsContent());
        } catch (Exception e) {
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }
    }

    private void clearHostsFileImmediately() {
        try {
            HostsFileManager.clearHostsFile();
        } catch (Exception e) {
            System.err.println("清空hosts文件失败: " + e.getMessage());
        }
    }

    private void viewHostsFile() {
        try {
            String hostsContent = HostsFileManager.readCurrentHosts();

            JTextArea textArea = new JTextArea(hostsContent, 40, 100);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            JDialog dialog = new JDialog();
            dialog.setTitle("本地hosts文件内容");
            dialog.setModal(true);
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dialog.dispose());
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.add(closeButton);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            dialog.setPreferredSize(new Dimension(900, 700));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            Messages.showErrorDialog(
                project,
                "读取hosts文件失败: " + e.getMessage(),
                "读取失败"
            );
        }
    }

    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }

    public void validateSelection() {
        List<HostConfiguration> configurations = service.getAllConfigurations();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (selectedConfig != null) {
            final String selectedConfigId = selectedConfig.getId();
            boolean configExists = configurations.stream()
                .anyMatch(config -> config.getId().equals(selectedConfigId));

            if (!configExists) {
                service.deselectConfiguration();
                clearHostsFileImmediately();
                SwingUtilities.invokeLater(() -> selectionTree.refresh());
            }
        }
    }
}
