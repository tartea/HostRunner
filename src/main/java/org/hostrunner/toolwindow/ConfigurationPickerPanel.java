package org.hostrunner.toolwindow;

import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可复用的配置选择面板 — 搜索框 + 配置卡片列表
 */
public class ConfigurationPickerPanel extends JPanel {

    private final Consumer<HostConfiguration> selectionCallback;
    private JPanel cardsPanel;
    private JTextField searchField;
    private ButtonGroup selectionGroup;
    private List<HostConfiguration> allConfigurations;
    private HostConfigurationService service;
    private String groupFilter;
    private boolean showGroupLabels;

    public ConfigurationPickerPanel(Consumer<HostConfiguration> selectionCallback) {
        this.selectionCallback = selectionCallback;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        refreshConfigurations();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 搜索框
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("搜索:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterConfigurations(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterConfigurations(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterConfigurations(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // 卡片面板 — 垂直排列行，每行3张卡片
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshConfigurations() {
        if (groupFilter != null && !groupFilter.isEmpty()) {
            allConfigurations = service.getConfigurationsByGroup(groupFilter);
        } else {
            allConfigurations = service.getAllConfigurations();
        }
        filterConfigurations();
    }

    private void filterConfigurations() {
        String searchText = searchField.getText().trim();
        cardsPanel.removeAll();
        selectionGroup = new ButtonGroup();

        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (allConfigurations.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无配置，请先在配置管理中添加配置");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            cardsPanel.add(emptyLabel);
        } else {
            List<HostConfiguration> filtered = allConfigurations;
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                filtered = allConfigurations.stream()
                    .filter(config -> {
                        String name = config.getName() != null ? config.getName().toLowerCase() : "";
                        String hosts = config.getHostsContent() != null ? config.getHostsContent().toLowerCase() : "";
                        String vm = config.getVmOptions() != null ? config.getVmOptions().toLowerCase() : "";
                        return name.contains(searchLower) || hosts.contains(searchLower) || vm.contains(searchLower);
                    })
                    .collect(java.util.stream.Collectors.toList());
            }

            if (filtered.isEmpty() && !searchText.isEmpty()) {
                JLabel noResults = new JLabel("未找到匹配的配置");
                noResults.setHorizontalAlignment(SwingConstants.CENTER);
                noResults.setForeground(UIManager.getColor("Label.disabledForeground"));
                cardsPanel.add(noResults);
            } else {
                final int COLS = 3;
                final int CARD_WIDTH = 280;
                List<HostConfiguration> configList = filtered;
                for (int i = 0; i < configList.size(); i += COLS) {
                    JPanel rowPanel = new JPanel();
                    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
                    rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                    for (int j = 0; j < COLS; j++) {
                        if (i + j < configList.size()) {
                            HostConfiguration config = configList.get(i + j);
                            boolean isSelected = selectedConfig != null && selectedConfig.getId().equals(config.getId());
                            ConfigurationCard card = new ConfigurationCard(config, isSelected, selectionGroup, selectionCallback, showGroupLabels);
                            card.setPreferredSize(new Dimension(CARD_WIDTH, 50));
                            card.setMaximumSize(new Dimension(CARD_WIDTH, 50));
                            rowPanel.add(card);
                        } else {
                            // 占位面板保持列对齐
                            JPanel placeholder = new JPanel();
                            placeholder.setPreferredSize(new Dimension(CARD_WIDTH, 1));
                            placeholder.setMaximumSize(new Dimension(CARD_WIDTH, Integer.MAX_VALUE));
                            rowPanel.add(placeholder);
                        }
                        if (j < COLS - 1) {
                            rowPanel.add(Box.createHorizontalStrut(8));
                        }
                    }

                    rowPanel.setAlignmentX(LEFT_ALIGNMENT);
                    cardsPanel.add(rowPanel);
                    cardsPanel.add(Box.createVerticalStrut(5));
                }
                cardsPanel.add(Box.createVerticalGlue());
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public void setGroupFilter(String groupName) {
        this.groupFilter = groupName;
        this.showGroupLabels = false;
    }

    public void setShowGroupLabels(boolean show) {
        this.showGroupLabels = show;
    }

    public void clearGroupFilter() {
        this.groupFilter = null;
        this.showGroupLabels = false;
    }

    /**
     * 刷新配置列表（供外部调用）
     */
    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    /**
     * 获取搜索框组件（供外部设置焦点）
     */
    public JTextField getSearchField() {
        return searchField;
    }
}
