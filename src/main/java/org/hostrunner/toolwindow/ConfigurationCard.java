package org.hostrunner.toolwindow;

import org.hostrunner.model.HostConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 配置卡片组件
 */
public class ConfigurationCard extends JPanel {

    private final HostConfiguration configuration;
    private final Consumer<HostConfiguration> selectionCallback;
    private JRadioButton selectionButton;
    private boolean isSelected;

    public ConfigurationCard(
            HostConfiguration configuration,
            boolean isSelected,
            ButtonGroup buttonGroup,
            Consumer<HostConfiguration> selectionCallback) {
        this(configuration, isSelected, buttonGroup, selectionCallback, false);
    }

    public ConfigurationCard(
            HostConfiguration configuration,
            boolean isSelected,
            ButtonGroup buttonGroup,
            Consumer<HostConfiguration> selectionCallback,
            boolean showGroupLabel) {
        this.configuration = configuration;
        this.isSelected = isSelected;
        this.selectionCallback = selectionCallback;

        initializeComponents(buttonGroup, showGroupLabel);
        updateAppearance();
    }

    private void initializeComponents(ButtonGroup buttonGroup, boolean showGroupLabel) {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        // 选择按钮
        selectionButton = new JRadioButton();
        selectionButton.setSelected(isSelected);
        buttonGroup.add(selectionButton);
        selectionButton.addActionListener(e -> {
            if (selectionButton.isSelected()) {
                selectionCallback.accept(configuration);
            }
        });

        // 添加鼠标监听器以支持点击卡片选择
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectionButton.setSelected(true);
                selectionCallback.accept(configuration);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // 移除鼠标悬停效果
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 移除鼠标悬停效果
            }
        });

        // 内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 配置名称
        JLabel nameLabel = new JLabel(configuration.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        contentPanel.add(nameLabel);

        // 分组标签（仅在搜索平铺模式下显示）
        if (showGroupLabel && configuration.getGroupName() != null && !configuration.getGroupName().isEmpty()) {
            JLabel groupLabel = new JLabel(configuration.getGroupName());
            groupLabel.setFont(groupLabel.getFont().deriveFont(9f));
            groupLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            contentPanel.add(groupLabel);
        }

        // 添加间距
        contentPanel.add(Box.createVerticalStrut(3));

        // Hosts内容预览（只显示第一行）
        if (configuration.getHostsContent() != null && !configuration.getHostsContent().isEmpty()) {
            String[] lines = configuration.getHostsContent().split("\n");
            String firstLine = lines.length > 0 ? lines[0].trim() : "";
            if (firstLine.length() > 50) {
                firstLine = firstLine.substring(0, 50) + "...";
            }
            JLabel hostsLabel = new JLabel(firstLine);
            hostsLabel.setFont(hostsLabel.getFont().deriveFont(9f));
            hostsLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            contentPanel.add(hostsLabel);
        }

        // 布局
        add(selectionButton, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setPreferredSize(new Dimension(300, 70));
    }

    private Color getBorderColor() {
        return isSelected ? UIManager.getColor("RadioButton.focus") : UIManager.getColor("Panel.background");
    }

    private void updateAppearance() {
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        selectionButton.setSelected(selected);
        updateAppearance();
    }

    public void updateSelectionBorder(boolean selected) {
        this.isSelected = selected;
        selectionButton.setSelected(selected);
        updateAppearance();
    }

    public HostConfiguration getConfiguration() {
        return configuration;
    }
}