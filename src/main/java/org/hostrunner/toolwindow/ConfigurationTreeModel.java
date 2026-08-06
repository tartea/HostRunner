package org.hostrunner.toolwindow;

import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.List;

/**
 * 配置分组树模型 — 两级树（分组节点 → 配置节点）
 */
public class ConfigurationTreeModel {

    public static final String GROUP_NODE_TYPE = "group";
    public static final String CONFIG_NODE_TYPE = "config";

    private final HostConfigurationService service;
    private DefaultTreeModel treeModel;

    public ConfigurationTreeModel() {
        this.service = HostConfigurationService.getInstance();
        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public void refresh() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        List<String> groups = service.getGroups();

        for (String groupName : groups) {
            int count = service.getGroupCount(groupName);
            if (count == 0) continue;

            String label = groupName + " (" + count + ")";
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeNodeData(GROUP_NODE_TYPE, groupName, label));
            root.add(groupNode);

            List<HostConfiguration> configs = service.getConfigurationsByGroup(groupName);
            for (HostConfiguration config : configs) {
                DefaultMutableTreeNode configNode = new DefaultMutableTreeNode(new TreeNodeData(CONFIG_NODE_TYPE, config.getId(), config.getName(), config));
                groupNode.add(configNode);
            }
        }

        treeModel.setRoot(root);
    }

    public void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void collapseAll(JTree tree) {
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.collapseRow(i);
        }
    }

    public void expandToConfiguration(JTree tree, String configId) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = 0; j < groupNode.getChildCount(); j++) {
                DefaultMutableTreeNode configNode = (DefaultMutableTreeNode) groupNode.getChildAt(j);
                TreeNodeData data = (TreeNodeData) configNode.getUserObject();
                if (data != null && configId.equals(data.getId())) {
                    TreeNode[] path = treeModel.getPathToRoot(configNode);
                    tree.expandPath(new javax.swing.tree.TreePath(path));
                    tree.setSelectionPath(new javax.swing.tree.TreePath(path));
                    return;
                }
            }
        }
    }

    public static class TreeNodeData {
        private final String type;   // GROUP_NODE_TYPE or CONFIG_NODE_TYPE
        private final String id;     // groupName for groups, configId for configs
        private final String displayName;
        private final HostConfiguration configuration; // null for group nodes

        public TreeNodeData(String type, String id, String displayName) {
            this(type, id, displayName, null);
        }

        public TreeNodeData(String type, String id, String displayName, HostConfiguration configuration) {
            this.type = type;
            this.id = id;
            this.displayName = displayName;
            this.configuration = configuration;
        }

        public String getType() { return type; }
        public String getId() { return id; }
        public HostConfiguration getConfiguration() { return configuration; }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
