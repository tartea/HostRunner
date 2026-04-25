package org.hostrunner.toolwindow;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBScrollPane;
import org.hostrunner.model.HostConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 配置详情查看对话框
 */
public class ConfigurationDetailDialog extends DialogWrapper {

    private final HostConfiguration configuration;

    public ConfigurationDetailDialog(HostConfiguration configuration) {
        super(true);
        this.configuration = configuration;
        init();
        setTitle("配置详情 - " + configuration.getName());
        setOKButtonText("关闭");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        // 配置名称
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("配置名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField nameField = new JTextField(configuration.getName());
        nameField.setEditable(false);
        nameField.setBackground(UIManager.getColor("TextField.background"));
        panel.add(nameField, gbc);
        row++;

        // JDK版本
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("JDK版本:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField jdkField = new JTextField(configuration.isJdk9OrAbove() ? "JDK 9+" : "JDK 9-");
        jdkField.setEditable(false);
        jdkField.setBackground(UIManager.getColor("TextField.background"));
        panel.add(jdkField, gbc);
        row++;


        // Hosts内容
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Hosts内容:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JBTextArea hostsArea = new JBTextArea(8, 40);
        hostsArea.setText(configuration.getHostsContent() != null ? configuration.getHostsContent() : "");
        hostsArea.setEditable(false);
        hostsArea.setLineWrap(true);
        hostsArea.setWrapStyleWord(true);
        JBScrollPane hostsScroll = new JBScrollPane(hostsArea);
        panel.add(hostsScroll, gbc);
        row++;

        // VM选项
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("VM选项:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JBTextArea vmOptionsArea = new JBTextArea(5, 40);
        vmOptionsArea.setText(configuration.getVmOptions() != null ? configuration.getVmOptions() : "");
        vmOptionsArea.setEditable(false);
        vmOptionsArea.setLineWrap(true);
        vmOptionsArea.setWrapStyleWord(true);
        JBScrollPane vmOptionsScroll = new JBScrollPane(vmOptionsArea);
        panel.add(vmOptionsScroll, gbc);

        panel.setPreferredSize(new Dimension(600, 500));
        return panel;
    }

    @Override
    protected String getHelpId() {
        return "hostrunner.configuration.detail";
    }
}