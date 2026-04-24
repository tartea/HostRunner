package org.hostrunner.springboot;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Hosts文件管理器
 */
public class HostsFileManager {

    private static final String HOSTS_FILE_NAME = "project-host.txt";

    /**
     * 更新hosts文件内容
     */
    public static void updateHostsFile(Project project, String hostsContent) throws IOException {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            throw new IOException("项目路径不存在");
        }

        File ideaDir = new File(projectBasePath, ".idea");
        if (!ideaDir.exists()) {
            if (!ideaDir.mkdirs()) {
                throw new IOException("无法创建.idea目录");
            }
        }

        File hostsFile = new File(ideaDir, HOSTS_FILE_NAME);

        // 写入hosts内容
        try (FileWriter writer = new FileWriter(hostsFile)) {
            writer.write(hostsContent != null ? hostsContent : "");
        }
    }

    /**
     * 清空hosts文件内容
     */
    public static void clearHostsFile(Project project) throws IOException {
        updateHostsFile(project, "");
    }

    /**
     * 获取hosts文件绝对路径
     */
    public static String getHostsFileAbsolutePath(Project project) {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return null;
        }

        File hostsFile = new File(projectBasePath, ".idea" + File.separator + HOSTS_FILE_NAME);
        return hostsFile.getAbsolutePath();
    }

    /**
     * 检查hosts文件是否存在
     */
    public static boolean hostsFileExists(Project project) {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return false;
        }

        File hostsFile = new File(projectBasePath, ".idea" + File.separator + HOSTS_FILE_NAME);
        return hostsFile.exists();
    }

    /**
     * 读取hosts文件内容
     */
    public static String readHostsFileContent(Project project) throws IOException {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            throw new IOException("项目路径不存在");
        }

        File hostsFile = new File(projectBasePath, ".idea" + File.separator + HOSTS_FILE_NAME);
        if (!hostsFile.exists()) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(hostsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    /**
     * 验证hosts内容格式
     */
    public static boolean isValidHostsContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true; // 空内容也是有效的
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // 空行和注释行跳过
            }

            // 基本的hosts格式验证：IP地址 + 主机名
            if (!line.matches("^\\s*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\s+\\S+.*$")) {
                return false;
            }
        }

        return true;
    }
}