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
     * Publish a configuration change message to all subscribers via the application-level message bus.
     */
    public void publishConfigurationChange(@NotNull String changeType, String configurationId) {
        String projectName = getActiveProjectName();
        ApplicationManager.getApplication()
            .getMessageBus()
            .syncPublisher(HostConfigurationMessageHandler.TOPIC)
            .onConfigurationChanged(changeType, configurationId, projectName);
    }

    private static String getActiveProjectName() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            if (!project.isDisposed()) {
                return project.getName();
            }
        }
        return "Unknown";
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