package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Host配置工具窗口工厂
 */
public class HostConfigurationToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板
        HostConfigurationPanel panel = new HostConfigurationPanel(project);

        // 添加到工具窗口
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

        // 使用组件监听器检测工具窗口显示
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            private boolean hasShownDialog = false;

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (!hasShownDialog) {
                    hasShownDialog = true;
                    // 延迟执行以确保UI准备就绪
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ToolWindowContentDialog dialog = new ToolWindowContentDialog(project);
                        dialog.show();
                        // 隐藏工具窗口
                        toolWindow.hide();
                    });
                }
            }
        });
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // 始终可用
        return true;
    }
}