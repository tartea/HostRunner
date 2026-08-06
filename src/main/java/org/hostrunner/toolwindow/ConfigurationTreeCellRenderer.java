package org.hostrunner.toolwindow;

import com.intellij.icons.AllIcons;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * 配置树节点渲染器 — 文件夹图标（分组），配置图标（叶子）
 */
public class ConfigurationTreeCellRenderer extends DefaultTreeCellRenderer {

    private final HostConfigurationService service;

    public ConfigurationTreeCellRenderer() {
        this.service = HostConfigurationService.getInstance();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData) {
            ConfigurationTreeModel.TreeNodeData data = (ConfigurationTreeModel.TreeNodeData) node.getUserObject();

            if (ConfigurationTreeModel.GROUP_NODE_TYPE.equals(data.getType())) {
                setIcon(AllIcons.Nodes.Folder);
                HostConfiguration selectedConfig = service.getSelectedConfiguration();
                if (selectedConfig != null) {
                    // 如果分组下有当前选中的配置，加粗分组节点
                    String selectedGroup = selectedConfig.getGroupName();
                    if (selectedGroup == null || selectedGroup.trim().isEmpty()) {
                        selectedGroup = "未分组";
                    }
                    if (selectedGroup.equals(data.getId())) {
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
            } else if (ConfigurationTreeModel.CONFIG_NODE_TYPE.equals(data.getType())) {
                setIcon(AllIcons.Nodes.Plugin);
                HostConfiguration selectedConfig = service.getSelectedConfiguration();
                if (selectedConfig != null && selectedConfig.getId().equals(data.getId())) {
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }
        }

        return this;
    }
}
