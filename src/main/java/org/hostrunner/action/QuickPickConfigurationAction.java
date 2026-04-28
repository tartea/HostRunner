package org.hostrunner.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.hostrunner.toolwindow.ConfigurationPickerDialog;
import org.jetbrains.annotations.NotNull;

/**
 * 快速选择Host配置 — 快捷键触发的Action
 */
public class QuickPickConfigurationAction extends AnAction {

    public QuickPickConfigurationAction() {
        super("快速选择Host配置", "通过快捷键弹出配置选择弹框", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConfigurationPickerDialog dialog = new ConfigurationPickerDialog();
        dialog.setVisible(true);
    }
}
