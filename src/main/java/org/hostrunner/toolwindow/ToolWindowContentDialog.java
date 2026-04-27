package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 工具窗口内容对话框 - 在弹窗中显示完整的工具窗口内容
 */
public class ToolWindowContentDialog extends DialogWrapper {

    private final Project project;
    private JTabbedPane tabbedPane;
    private ConfigurationSelectionPanel selectionPanel;
    private ConfigurationManagementPanel managementPanel;

    public ToolWindowContentDialog(Project project) {
        super(project, true);
        this.project = project;
        init();
        setTitle("Host配置工具窗口");
        setOKButtonText("关闭");
        setSize(800, 600);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建标签页容器
        tabbedPane = new JTabbedPane();

        // 创建配置管理标签页
        managementPanel = new ConfigurationManagementPanel(project, this::onConfigurationChanged);

        // 创建配置选择标签页
        selectionPanel = new ConfigurationSelectionPanel(project);

        // 添加标签页
        tabbedPane.addTab("配置选择", selectionPanel);
        tabbedPane.addTab("配置管理", managementPanel);

        // 添加标签页切换监听器
        tabbedPane.addChangeListener(e -> {
            // 当切换到配置选择标签页时，验证当前选择
            if (tabbedPane.getSelectedIndex() == 0) {
                selectionPanel.validateSelection();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void onConfigurationChanged(Void unused) {
        // 当配置管理标签页发生更改时，刷新选择标签页
        if (selectionPanel != null) {
            selectionPanel.refresh();
        }
    }

    @Override
    protected String getHelpId() {
        return "hostrunner.toolwindow.content";
    }
}