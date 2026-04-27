package org.hostrunner.messaging;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

/**
 * Service for publishing host configuration change messages
 */
@Service
public final class HostConfigurationMessagePublisher {

    public static HostConfigurationMessagePublisher getInstance() {
        return ApplicationManager.getApplication()
            .getService(HostConfigurationMessagePublisher.class);
    }

    /**
     * Publish a configuration change message to all projects
     */
    public void publishConfigurationChange(@NotNull String changeType, String configurationId) {
        // Get current project name to include in message
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project currentProject = null;

        // Find the current project context
        for (Project project : openProjects) {
            if (project.isDisposed()) continue;
            currentProject = project;
            break;
        }

        String projectName = currentProject != null ? currentProject.getName() : "Unknown";

        // Publish to all open projects
        for (Project project : openProjects) {
            if (!project.isDisposed()) {
                HostConfigurationMessageHandler publisher =
                    project.getMessageBus().syncPublisher(HostConfigurationMessageHandler.TOPIC);
                publisher.onConfigurationChanged(changeType, configurationId, projectName);
            }
        }
    }

    /**
     * Publish configuration added event
     */
    public void publishConfigurationAdded(String configurationId) {
        publishConfigurationChange("ADD", configurationId);
    }

    /**
     * Publish configuration updated event
     */
    public void publishConfigurationUpdated(String configurationId) {
        publishConfigurationChange("UPDATE", configurationId);
    }

    /**
     * Publish configuration deleted event
     */
    public void publishConfigurationDeleted(String configurationId) {
        publishConfigurationChange("DELETE", configurationId);
    }

    /**
     * Publish configuration selection changed event
     */
    public void publishConfigurationSelected(String configurationId) {
        publishConfigurationChange("SELECT", configurationId);
    }
}