package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Host配置状态栏组件
 */
public class HostConfigurationStatusWidget implements StatusBarWidget {

    public static final String ID = "HostConfigurationStatus";
    private final Project project;
    private JLabel label;

    public HostConfigurationStatusWidget(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        // 安装到状态栏
        updateStatus(this.project);
    }

    @Override
    public void dispose() {
        // 清理资源
        if (label != null) {
            label.removeAll();
            label = null;
        }
    }

    @Nullable
    public JComponent getComponent() {
        if (label == null) {
            label = new JLabel();
            label.setFont(label.getFont().deriveFont(11f));
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            // 添加点击事件
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        showConfigurationDetail();
                    }
                }
            });

            updateStatus(this.project);
        }
        return label;
    }


    private void showConfigurationDetail() {
        HostConfigurationService service = HostConfigurationService.getInstance();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (selectedConfig != null) {
            ConfigurationDetailDialog dialog = new ConfigurationDetailDialog(selectedConfig);
            dialog.show();
        }
    }

    /**
     * 更新状态栏显示
     */
    public static void updateStatus(Project project) {
        if (project == null) {
            return;
        }
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar == null) {
            return;
        }

        // 尝试获取widget实例
        statusBar.updateWidget(ID);
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return new TextPresentation() {
            @Override
            public @Nullable String getTooltipText() {
                return "";
            }

            @Override
            public float getAlignment() {
                return 0;
            }

            @Override
            public @NotNull String getText() {
                HostConfigurationService service = HostConfigurationService.getInstance();
                HostConfiguration selectedConfig = service.getSelectedConfiguration();
                if (selectedConfig != null) {
                    return "Host配置: " + selectedConfig.getName();
                } else {
                    return "Host配置: 无";
                }
            }
        };
    }

}