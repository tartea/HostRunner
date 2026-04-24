package org.hostrunner.settings;

import org.hostrunner.model.HostConfiguration;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置表格模型
 */
public class HostConfigurationTableModel extends AbstractTableModel {

    private final String[] columnNames = {"名称", "Hosts内容预览", "JDK版本", "创建时间"};
    private List<HostConfiguration> configurations = new ArrayList<>();

    public void refreshData(List<HostConfiguration> newConfigurations) {
        this.configurations = new ArrayList<>(newConfigurations);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return configurations.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= configurations.size()) {
            return null;
        }

        HostConfiguration config = configurations.get(rowIndex);

        switch (columnIndex) {
            case 0: // 名称
                return config.getName();
            case 1: // Hosts内容预览
                if (config.getHostsContent() == null || config.getHostsContent().isEmpty()) {
                    return "<空>";
                }
                String[] lines = config.getHostsContent().split("\n");
                return lines.length > 0 ? lines[0] + (lines.length > 1 ? "..." : "") : "<空>";
            case 2: // JDK版本
                return config.isJdk9OrAbove() ? "JDK 9+" : "JDK 9-";
            case 3: // 创建时间
                return formatTimestamp(config.getCreateTime());
            default:
                return null;
        }
    }

    public HostConfiguration getConfigurationAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= configurations.size()) {
            return null;
        }
        return configurations.get(rowIndex);
    }

    private String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    public void addConfiguration(HostConfiguration config) {
        configurations.add(config);
        fireTableRowsInserted(configurations.size() - 1, configurations.size() - 1);
    }

    public void updateConfiguration(int rowIndex, HostConfiguration config) {
        if (rowIndex >= 0 && rowIndex < configurations.size()) {
            configurations.set(rowIndex, config);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public void removeConfiguration(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < configurations.size()) {
            configurations.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void clear() {
        int size = configurations.size();
        configurations.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
}