package org.hostrunner.settings;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.hostrunner.model.HostConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 模板选择对话框
 */
public class TemplateSelectionDialog extends DialogWrapper {

    private JBList<HostConfiguration> templateList;
    private JTextArea previewArea;
    private JTextField nameField;
    private HostConfiguration selectedTemplate;

    public TemplateSelectionDialog() {
        super(true);
        init();
        setTitle("选择模板");
        setOKButtonText("创建配置");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 模板列表
        List<HostConfiguration> templates = ConfigurationTemplate.getPredefinedTemplates();
        templateList = new JBList<>(templates.toArray(new HostConfiguration[0]));
        templateList.setCellRenderer(new TemplateListCellRenderer());
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setSelectedIndex(0); // 默认选中第一个
        selectedTemplate = templates.get(0);

        // 添加选择监听器
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedTemplate = templateList.getSelectedValue();
                updatePreview();
            }
        });

        // 双击选择
        templateList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && templateList.getSelectedValue() != null) {
                    selectedTemplate = templateList.getSelectedValue();
                    doOKAction();
                }
            }
        });

        JBScrollPane listScroll = new JBScrollPane(templateList);
        listScroll.setPreferredSize(new Dimension(250, 200));

        // 预览区域
        previewArea = new JTextArea(10, 40);
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        JBScrollPane previewScroll = new JBScrollPane(previewArea);

        // 配置名称输入
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.add(new JLabel("新配置名称:"), BorderLayout.WEST);
        nameField = new JTextField(20);
        nameField.setText(selectedTemplate != null ? selectedTemplate.getName() + " - 副本" : "");
        namePanel.add(nameField, BorderLayout.CENTER);

        // 布局
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("选择模板:"), BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("模板预览:"), BorderLayout.NORTH);
        rightPanel.add(previewScroll, BorderLayout.CENTER);
        rightPanel.add(namePanel, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        updatePreview();
        return panel;
    }

    private void updatePreview() {
        if (selectedTemplate != null) {
            previewArea.setText(ConfigurationTemplate.getTemplatePreview(selectedTemplate));
            nameField.setText(selectedTemplate.getName() + " - 副本");
        } else {
            previewArea.setText("请选择模板");
        }
    }

    public HostConfiguration getSelectedTemplate() {
        return selectedTemplate;
    }

    public String getNewConfigurationName() {
        return nameField.getText().trim();
    }

    @Override
    protected String getHelpId() {
        return "hostrunner.template.selection";
    }

    private static class TemplateListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof HostConfiguration) {
                HostConfiguration template = (HostConfiguration) value;
                setText(template.getName());
                setIcon(null);
            }
            return this;
        }
    }
}