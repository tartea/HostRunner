package org.hostrunner.settings;

import org.hostrunner.model.HostConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置模板管理
 */
public class ConfigurationTemplate {

    public enum TemplateType {
        DEVELOPMENT("开发环境"),
        TESTING("测试环境"),
        PRODUCTION("生产环境（调试）");

        private final String displayName;

        TemplateType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 获取所有预设模板
     */
    public static List<HostConfiguration> getPredefinedTemplates() {
        List<HostConfiguration> templates = new ArrayList<>();

        // 开发环境模板
        HostConfiguration devTemplate = new HostConfiguration();
        devTemplate.setName("开发环境");
        devTemplate.setHostsContent(
            "127.0.0.1 localhost\n" +
            "127.0.0.1 dev-api.example.com\n" +
            "127.0.0.1 dev-db.example.com"
        );
        devTemplate.setVmOptions(
            "-Xmx512m -Dspring.profiles.active=dev -Dspring.devtools.restart.enabled=true"
        );
        templates.add(devTemplate);

        // 测试环境模板
        HostConfiguration testTemplate = new HostConfiguration();
        testTemplate.setName("测试环境");
        testTemplate.setHostsContent(
            "127.0.0.1 localhost\n" +
            "192.168.1.100 test-api.example.com\n" +
            "192.168.1.101 test-db.example.com"
        );
        testTemplate.setVmOptions(
            "-Xmx1024m -Dspring.profiles.active=test -Dlogging.level.com.example=DEBUG"
        );
        templates.add(testTemplate);

        // 生产环境模板
        HostConfiguration prodTemplate = new HostConfiguration();
        prodTemplate.setName("生产环境（调试）");
        prodTemplate.setHostsContent("127.0.0.1 localhost");
        prodTemplate.setVmOptions(
            "-Xmx2048m -XX:+UseG1GC -Dspring.profiles.active=prod"
        );
        templates.add(prodTemplate);

        return templates;
    }

    /**
     * 根据类型获取模板
     */
    public static HostConfiguration getTemplateByType(TemplateType type) {
        return getPredefinedTemplates().stream()
            .filter(template -> template.getName().equals(type.getDisplayName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据名称获取模板
     */
    public static HostConfiguration getTemplateByName(String name) {
        return getPredefinedTemplates().stream()
            .filter(template -> template.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * 创建基于模板的新配置
     */
    public static HostConfiguration createFromTemplate(HostConfiguration template, String newName) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }

        HostConfiguration newConfig = new HostConfiguration();
        newConfig.setName(newName);
        newConfig.setHostsContent(template.getHostsContent());
        newConfig.setVmOptions(template.getVmOptions());
        return newConfig;
    }

    /**
     * 检查是否为预设模板名称
     */
    public static boolean isPredefinedTemplateName(String name) {
        return getPredefinedTemplates().stream()
            .anyMatch(template -> template.getName().equals(name));
    }

    /**
     * 获取模板预览信息
     */
    public static String getTemplatePreview(HostConfiguration template) {
        if (template == null) {
            return "无预览信息";
        }

        StringBuilder preview = new StringBuilder();
        preview.append("配置名称: ").append(template.getName()).append("\n");
        preview.append("Hosts内容预览: ").append(template.getHostsContent() != null ?
            template.getHostsContent().split("\n")[0] + "..." : "空").append("\n");
        preview.append("VM选项预览: ").append(template.getVmOptions() != null ?
            (template.getVmOptions().length() > 50 ?
                template.getVmOptions().substring(0, 50) + "..." :
                template.getVmOptions()) : "空");

        return preview.toString();
    }
}