package org.hostrunner.settings;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBScrollPane;
import org.hostrunner.model.HostConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 配置编辑表单
 */
public class HostConfigurationForm extends DialogWrapper {

    private HostConfiguration configuration;
    private JTextField nameField;
    private JBTextArea hostsContentArea;
    private JBTextArea vmOptionsArea;
    private JRadioButton jdk9AboveButton;
    private JRadioButton jdk9BelowButton;

    public HostConfigurationForm(HostConfiguration configuration) {
        super(true);
        this.configuration = configuration != null ? configuration : new HostConfiguration();
        init();
        setTitle(configuration != null ? "编辑配置" : "添加配置");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 配置名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("配置名称:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        nameField = new JTextField(30);
        nameField.setText(configuration.getName());
        panel.add(nameField, gbc);

        // Hosts内容
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Hosts内容:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        hostsContentArea = new JBTextArea(8, 40);
        hostsContentArea.setText(configuration.getHostsContent());
        hostsContentArea.setLineWrap(true);
        JBScrollPane hostsScroll = new JBScrollPane(hostsContentArea);
        panel.add(hostsScroll, gbc);

        // VM选项
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("VM选项:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        vmOptionsArea = new JBTextArea(3, 40);
        vmOptionsArea.setText(configuration.getVmOptions());
        vmOptionsArea.setLineWrap(true);
        JBScrollPane vmOptionsScroll = new JBScrollPane(vmOptionsArea);
        panel.add(vmOptionsScroll, gbc);

        // JDK版本选择
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("JDK版本:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JPanel jdkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup jdkGroup = new ButtonGroup();
        jdk9AboveButton = new JRadioButton("JDK 9+");
        jdk9BelowButton = new JRadioButton("JDK 9-");
        jdkGroup.add(jdk9AboveButton);
        jdkGroup.add(jdk9BelowButton);

        if (configuration.isJdk9OrAbove()) {
            jdk9AboveButton.setSelected(true);
        } else {
            jdk9BelowButton.setSelected(true);
        }

        jdkPanel.add(jdk9AboveButton);
        jdkPanel.add(jdk9BelowButton);
        panel.add(jdkPanel, gbc);

        // 提示信息
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel hintLabel = new JLabel("<html><small>提示：VM选项使用空格分隔多个参数，如：-Xmx512m -Dspring.profiles.active=dev</small></html>");
        panel.add(hintLabel, gbc);

        panel.setPreferredSize(new Dimension(600, 500));
        return panel;
    }

    @Override
    protected ValidationInfo doValidate() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return new ValidationInfo("配置名称不能为空", nameField);
        }

        String vmOptions = vmOptionsArea.getText();
        if (vmOptions != null && !vmOptions.trim().isEmpty()) {
            if (!org.hostrunner.springboot.VmOptionsInjector.isValidVmOptions(vmOptions)) {
                return new ValidationInfo("VM选项格式不正确，请检查是否有非法字符", vmOptionsArea);
            }
        }

        return null;
    }

    public HostConfiguration getConfiguration() {
        configuration.setName(nameField.getText().trim());
        configuration.setHostsContent(hostsContentArea.getText());
        configuration.setVmOptions(vmOptionsArea.getText());
        configuration.setJdk9OrAbove(jdk9AboveButton.isSelected());
        return configuration;
    }

    @Override
    protected String getHelpId() {
        return "hostrunner.configuration.form";
    }
}