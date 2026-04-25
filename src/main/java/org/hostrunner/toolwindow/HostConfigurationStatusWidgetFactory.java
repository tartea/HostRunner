package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Host配置状态栏组件工厂
 */
public class HostConfigurationStatusWidgetFactory implements StatusBarWidgetFactory {

    public static final String ID = "HostConfigurationStatus";

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Host配置状态";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new HostConfigurationStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // 清理资源
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}