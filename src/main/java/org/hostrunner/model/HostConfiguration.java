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
    private boolean jdk9OrAbove;    // true: JDK9+, false: JDK9-
    private long createTime;        // 创建时间
    private long updateTime;        // 更新时间

    // 构造函数
    public HostConfiguration() {
        this.id = UUID.randomUUID().toString();
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public HostConfiguration(String name, String hostsContent, String vmOptions, boolean jdk9OrAbove) {
        this();
        this.name = name;
        this.hostsContent = hostsContent;
        this.vmOptions = vmOptions;
        this.jdk9OrAbove = jdk9OrAbove;
    }

    // getter和setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updateTime = System.currentTimeMillis();
    }

    public String getHostsContent() { return hostsContent; }
    public void setHostsContent(String hostsContent) {
        this.hostsContent = hostsContent;
        this.updateTime = System.currentTimeMillis();
    }

    public String getVmOptions() { return vmOptions; }
    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
        this.updateTime = System.currentTimeMillis();
    }

    public boolean isJdk9OrAbove() { return jdk9OrAbove; }
    public void setJdk9OrAbove(boolean jdk9OrAbove) {
        this.jdk9OrAbove = jdk9OrAbove;
        this.updateTime = System.currentTimeMillis();
    }

    public long getCreateTime() { return createTime; }
    public long getUpdateTime() { return updateTime; }

    // 序列化方法
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("hostsContent", hostsContent);
        map.put("vmOptions", vmOptions);
        map.put("jdk9OrAbove", jdk9OrAbove);
        map.put("createTime", createTime);
        map.put("updateTime", updateTime);
        return map;
    }

    public static HostConfiguration fromMap(Map<String, Object> map) {
        HostConfiguration config = new HostConfiguration();
        config.id = (String) map.get("id");
        config.name = (String) map.get("name");
        config.hostsContent = (String) map.get("hostsContent");
        config.vmOptions = (String) map.get("vmOptions");
        config.jdk9OrAbove = map.get("jdk9OrAbove") != null ? (Boolean) map.get("jdk9OrAbove") : true;
        config.createTime = map.get("createTime") != null ? (Long) map.get("createTime") : System.currentTimeMillis();
        config.updateTime = map.get("updateTime") != null ? (Long) map.get("updateTime") : System.currentTimeMillis();
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
                ", jdk9OrAbove=" + jdk9OrAbove +
                '}';
    }
}