package org.hostrunner.springboot;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.project.Project;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.model.HostConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Spring Boot运行配置扩展
 */
public class SpringBootRunConfigurationExtension extends com.intellij.execution.RunConfigurationExtension {

    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(
            @NotNull T configuration,
            @NotNull JavaParameters params,
            @Nullable RunnerSettings runnerSettings) throws ExecutionException {

        Project project = configuration.getProject();

        // 只处理Spring Boot和Application配置
        if (!isSupportedConfiguration(configuration)) {
            return;
        }

        HostConfigurationService service = HostConfigurationService.getInstance();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (selectedConfig == null) {
            // 无选中配置：只注入VM选项，不处理文件（文件已在工具窗口中处理）
            return;
        }

        try {
            // 只注入VM选项，文件更新已在工具窗口中处理
            VmOptionsInjector.injectVmOptions(params, project, selectedConfig);

        } catch (Exception e) {
            throw new ExecutionException("注入VM选项失败: " + e.getMessage(), e);
        }
    }

    private <T extends RunConfigurationBase<?>> boolean isSupportedConfiguration(T configuration) {
        // 检查是否为Spring Boot配置或Application配置
        String configType = configuration.getType().getId();
        String displayName = configuration.getType().getDisplayName();

        return "SpringBootApplicationConfiguration".equals(configType) ||
               "Spring Boot".equals(displayName) ||
               "Application".equals(configType) ||
               "Application".equals(displayName);
    }

    @NotNull
    @Override
    public String getEditorTitle() {
        return "Host Configuration";
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
        return isSupportedConfiguration(configuration);
    }
}