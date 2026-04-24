package org.hostrunner.springboot;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.openapi.project.Project;
import org.hostrunner.model.HostConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * VM选项注入器
 */
public class VmOptionsInjector {

    /**
     * 注入VM选项到Java参数
     */
    public static void injectVmOptions(
            JavaParameters params,
            Project project,
            HostConfiguration configuration) {

        if (configuration == null) {
            return;
        }

        List<String> vmOptions = new ArrayList<>();

        // 添加hosts文件参数
        String hostsFilePath = HostsFileManager.getHostsFileAbsolutePath(project);
        if (hostsFilePath != null) {
            String hostsParam = configuration.isJdk9OrAbove()
                ? "-Djdk.net.hosts.file=" + hostsFilePath
                : "-Dsun.net.hosts.file=" + hostsFilePath;
            vmOptions.add(hostsParam);
        }

        // 添加用户自定义VM选项
        if (configuration.getVmOptions() != null && !configuration.getVmOptions().trim().isEmpty()) {
            String[] customOptions = configuration.getVmOptions().trim().split("\\s+");
            vmOptions.addAll(Arrays.asList(customOptions));
        }

        // 注入到Java参数
        for (String option : vmOptions) {
            if (!isOptionAlreadyPresent(params, option)) {
                params.getVMParametersList().add(option);
            }
        }
    }

    /**
     * 检查VM选项是否已经存在
     */
    private static boolean isOptionAlreadyPresent(JavaParameters params, String option) {
        return params.getVMParametersList().getParametersString().contains(option);
    }

    /**
     * 从VM参数列表中移除指定的VM选项
     */
    public static void removeVmOptions(JavaParameters params, Project project, HostConfiguration configuration) {
        if (configuration == null) {
            return;
        }

        // 注意：ParametersList没有直接的remove方法，这里只是预留接口
        // 实际应用中可能需要更复杂的逻辑来处理参数移除
        String hostsFilePath = HostsFileManager.getHostsFileAbsolutePath(project);
        if (hostsFilePath != null) {
            String hostsParam = configuration.isJdk9OrAbove()
                ? "-Djdk.net.hosts.file=" + hostsFilePath
                : "-Dsun.net.hosts.file=" + hostsFilePath;

            // 由于ParametersList API限制，这里暂时不实现移除逻辑
            // 实际使用中，注入时会检查是否已存在避免重复
        }
    }

    /**
     * 验证VM选项格式
     */
    public static boolean isValidVmOptions(String vmOptions) {
        if (vmOptions == null || vmOptions.trim().isEmpty()) {
            return true;
        }

        // 基本格式验证
        String trimmed = vmOptions.trim();
        if (trimmed.startsWith(" ") || trimmed.endsWith(" ") || trimmed.contains("  ")) {
            return false;
        }

        // 检查是否包含非法字符
        if (trimmed.contains("\"") || trimmed.contains("'") || trimmed.contains("|")) {
            return false;
        }

        return true;
    }

    /**
     * 格式化VM选项字符串
     */
    public static String formatVmOptions(String vmOptions) {
        if (vmOptions == null || vmOptions.trim().isEmpty()) {
            return "";
        }

        // 移除多余空格，确保格式正确
        return vmOptions.trim().replaceAll("\\s+", " ");
    }
}