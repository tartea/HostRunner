package org.hostrunner.messaging;

import com.intellij.util.messages.Topic;

/**
 * Message handler interface for host configuration changes
 */
public interface HostConfigurationMessageHandler {
    Topic<HostConfigurationMessageHandler> TOPIC =
        Topic.create("HostConfigurationChanges", HostConfigurationMessageHandler.class);

    /**
     * Called when a configuration change occurs
     * @param changeType Type of change: "ADD", "UPDATE", "DELETE", "SELECT"
     * @param configurationId ID of the affected configuration (null for SELECT changes)
     * @param projectName Name of the project where change originated
     */
    void onConfigurationChanged(String changeType, String configurationId, String projectName);
}