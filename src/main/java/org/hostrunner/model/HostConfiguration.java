package org.hostrunner.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Host配置数据模型
 */
public class HostConfiguration {
    private String id;              // 唯一标识符
    private String name;            // 配置名称
    private String hostsContent;    // hosts文件内容
    private String vmOptions;       // 用户自定义VM选项

    // 构造函数
    public HostConfiguration() {
        this.id = UUID.randomUUID().toString();
    }

    public HostConfiguration(String name, String hostsContent, String vmOptions) {
        this();
        this.name = name;
        this.hostsContent = hostsContent;
        this.vmOptions = vmOptions;
    }

    // getter和setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getHostsContent() { return hostsContent; }
    public void setHostsContent(String hostsContent) {
        this.hostsContent = hostsContent;
    }

    public String getVmOptions() { return vmOptions; }
    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
    }


    // 序列化方法
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("hostsContent", hostsContent);
        map.put("vmOptions", vmOptions);
        return map;
    }

    public static HostConfiguration fromMap(Map<String, Object> map) {
        HostConfiguration config = new HostConfiguration();
        config.id = (String) map.get("id");
        config.name = (String) map.get("name");
        config.hostsContent = (String) map.get("hostsContent");
        config.vmOptions = (String) map.get("vmOptions");
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HostConfiguration that = (HostConfiguration) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "HostConfiguration{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}