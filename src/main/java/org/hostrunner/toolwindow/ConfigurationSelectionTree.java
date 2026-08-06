package org.hostrunner.toolwindow;

import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.springboot.HostsFileManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 配置选择树 — 只读，点击叶子选中配置
 */
public class ConfigurationSelectionTree extends JTree {

    private final ConfigurationTreeModel configTreeModel;
    private final HostConfigurationService service;
    private Consumer<HostConfiguration> selectionCallback;

    public ConfigurationSelectionTree() {
        this.service = HostConfigurationService.getInstance();
        this.configTreeModel = new ConfigurationTreeModel();

        setModel(configTreeModel.getTreeModel());
        setCellRenderer(new ConfigurationTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData) {
                        ConfigurationTreeModel.TreeNodeData data = (ConfigurationTreeModel.TreeNodeData) node.getUserObject();
                        if (ConfigurationTreeModel.CONFIG_NODE_TYPE.equals(data.getType())) {
                            HostConfiguration config = data.getConfiguration();
                            if (config != null) {
                                service.selectConfiguration(config.getId());
                                try {
                                    HostsFileManager.updateHostsFile(config.getHostsContent());
                                } catch (Exception ex) {
                                    System.err.println("更新hosts文件失败: " + ex.getMessage());
                                }
                                if (selectionCallback != null) {
                                    selectionCallback.accept(config);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void setSelectionCallback(Consumer<HostConfiguration> callback) {
        this.selectionCallback = callback;
    }

    public void refresh() {
        configTreeModel.refresh();
        configTreeModel.collapseAll(this);
        getModel(); // trigger repaint
    }

    public void refreshAndExpandToSelected() {
        configTreeModel.refresh();
        configTreeModel.collapseAll(this);

        HostConfiguration selected = service.getSelectedConfiguration();
        if (selected != null) {
            configTreeModel.expandToConfiguration(this, selected.getId());
        }
    }

    public void filterTree(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            configTreeModel.refresh();
            configTreeModel.collapseAll(this);
            return;
        }

        String lower = searchText.toLowerCase();
        configTreeModel.refresh();
        // 展开所有包含匹配项的分组，折叠不匹配的
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) configTreeModel.getTreeModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
            ConfigurationTreeModel.TreeNodeData groupData = (ConfigurationTreeModel.TreeNodeData) groupNode.getUserObject();
            boolean groupMatches = groupData != null && groupData.toString().toLowerCase().contains(lower);
            boolean childMatches = false;

            for (int j = 0; j < groupNode.getChildCount(); j++) {
                DefaultMutableTreeNode configNode = (DefaultMutableTreeNode) groupNode.getChildAt(j);
                ConfigurationTreeModel.TreeNodeData configData = (ConfigurationTreeModel.TreeNodeData) configNode.getUserObject();
                if (configData != null && configData.toString().toLowerCase().contains(lower)) {
                    childMatches = true;
                    break;
                }
            }

            TreePath groupPath = new TreePath(configTreeModel.getTreeModel().getPathToRoot(groupNode));
            if (groupMatches || childMatches) {
                expandPath(groupPath);
            } else {
                collapsePath(groupPath);
            }
        }
    }
}
