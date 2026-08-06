package org.hostrunner.toolwindow;

import com.intellij.openapi.ui.Messages;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.settings.HostConfigurationForm;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 配置管理树 — 支持拖拽移动、右键菜单、双击编辑
 */
public class ConfigurationManagementTree extends JTree {

    private final ConfigurationTreeModel configTreeModel;
    private final HostConfigurationService service;
    private Consumer<Void> refreshCallback;

    public ConfigurationManagementTree() {
        this.service = HostConfigurationService.getInstance();
        this.configTreeModel = new ConfigurationTreeModel();

        setModel(configTreeModel.getTreeModel());
        setCellRenderer(new ConfigurationTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        setDragEnabled(true);
        setTransferHandler(new ConfigTreeTransferHandler());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // 双击编辑
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData) {
                            ConfigurationTreeModel.TreeNodeData data = (ConfigurationTreeModel.TreeNodeData) node.getUserObject();
                            if (ConfigurationTreeModel.CONFIG_NODE_TYPE.equals(data.getType())) {
                                editConfiguration(data.getConfiguration());
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
        });
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }

    public void refresh() {
        configTreeModel.refresh();
        configTreeModel.expandAll(this);
    }

    private void showPopup(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path == null) return;

        setSelectionPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (!(node.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData)) return;

        ConfigurationTreeModel.TreeNodeData data = (ConfigurationTreeModel.TreeNodeData) node.getUserObject();
        JPopupMenu popup = new JPopupMenu();

        if (ConfigurationTreeModel.GROUP_NODE_TYPE.equals(data.getType())) {
            JMenuItem newConfigItem = new JMenuItem("新建配置");
            newConfigItem.addActionListener(ev -> addConfigurationToGroup(data.getId()));
            popup.add(newConfigItem);
        } else if (ConfigurationTreeModel.CONFIG_NODE_TYPE.equals(data.getType())) {
            JMenuItem editItem = new JMenuItem("编辑");
            editItem.addActionListener(ev -> editConfiguration(data.getConfiguration()));
            popup.add(editItem);

            JMenuItem deleteItem = new JMenuItem("删除");
            deleteItem.addActionListener(ev -> deleteConfiguration(data.getConfiguration()));
            popup.add(deleteItem);
        }

        if (popup.getComponentCount() > 0) {
            popup.show(this, e.getX(), e.getY());
        }
    }

    public void addConfigurationToGroup(String groupName) {
        HostConfigurationForm form = new HostConfigurationForm(null);
        if (form.showAndGet()) {
            HostConfiguration config = form.getConfiguration();
            config.setGroupName(groupName);
            if (validateConfiguration(config, null)) {
                service.addConfiguration(config);
                refresh();
                fireRefresh();
            }
        }
    }

    public void addConfiguration() {
        addConfigurationToGroup("未分组");
    }

    private void editConfiguration(HostConfiguration config) {
        HostConfigurationForm form = new HostConfigurationForm(config);
        if (form.showAndGet()) {
            HostConfiguration updated = form.getConfiguration();
            if (validateConfiguration(updated, config.getId())) {
                service.updateConfiguration(updated);
                refresh();
                fireRefresh();
            }
        }
    }

    private void deleteConfiguration(HostConfiguration config) {
        HostConfiguration selectedConfig = service.getSelectedConfiguration();
        if (selectedConfig != null && selectedConfig.getId().equals(config.getId())) {
            Messages.showErrorDialog(
                "无法删除配置 '" + config.getName() + "'，因为该配置当前已被选中使用。\n" +
                "请先切换到其他配置或取消选择后，再尝试删除。",
                "配置删除失败"
            );
            return;
        }

        int result = Messages.showYesNoDialog(
            "确定要删除配置 '" + config.getName() + "' 吗？",
            "删除配置",
            Messages.getQuestionIcon()
        );

        if (result == Messages.YES) {
            service.removeConfiguration(config.getId());
            refresh();
            fireRefresh();
        }
    }

    private boolean validateConfiguration(HostConfiguration config, String excludeId) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            Messages.showErrorDialog("配置名称不能为空", "验证错误");
            return false;
        }
        if (service.isNameDuplicate(config.getName(), excludeId)) {
            Messages.showErrorDialog("配置名称 '" + config.getName() + "' 已存在", "验证错误");
            return false;
        }
        return true;
    }

    private void fireRefresh() {
        if (refreshCallback != null) {
            refreshCallback.accept(null);
        }
    }

    /**
     * 拖拽处理器 — 拖动配置节点到其他分组节点以移动分组
     */
    private class ConfigTreeTransferHandler extends TransferHandler {

        private final DataFlavor nodeFlavor = new DataFlavor(DefaultMutableTreeNode.class, "TreeNode");

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            TreePath path = getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData) {
                    ConfigurationTreeModel.TreeNodeData data = (ConfigurationTreeModel.TreeNodeData) node.getUserObject();
                    if (ConfigurationTreeModel.CONFIG_NODE_TYPE.equals(data.getType())) {
                        return new TreeNodeTransferable(node);
                    }
                }
            }
            return null;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) return false;
            try {
                Transferable t = support.getTransferable();
                DefaultMutableTreeNode draggedNode = (DefaultMutableTreeNode) t.getTransferData(nodeFlavor);
                if (!(draggedNode.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData)) return false;

                JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
                TreePath targetPath = dl.getPath();
                if (targetPath == null) return false;

                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) targetPath.getLastPathComponent();
                if (!(targetNode.getUserObject() instanceof ConfigurationTreeModel.TreeNodeData)) return false;

                ConfigurationTreeModel.TreeNodeData targetData = (ConfigurationTreeModel.TreeNodeData) targetNode.getUserObject();
                return ConfigurationTreeModel.GROUP_NODE_TYPE.equals(targetData.getType());
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                Transferable t = support.getTransferable();
                DefaultMutableTreeNode draggedNode = (DefaultMutableTreeNode) t.getTransferData(nodeFlavor);
                ConfigurationTreeModel.TreeNodeData draggedData = (ConfigurationTreeModel.TreeNodeData) draggedNode.getUserObject();

                JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
                ConfigurationTreeModel.TreeNodeData targetData = (ConfigurationTreeModel.TreeNodeData) targetNode.getUserObject();

                HostConfiguration config = draggedData.getConfiguration();
                if (config != null) {
                    config.setGroupName(targetData.getId());
                    service.updateConfiguration(config);
                    refresh();
                    fireRefresh();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private class TreeNodeTransferable implements Transferable {
            private final DefaultMutableTreeNode node;

            TreeNodeTransferable(DefaultMutableTreeNode node) {
                this.node = node;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{nodeFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodeFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
                return node;
            }
        }
    }
}
