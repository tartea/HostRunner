# Host Configuration Message Bus Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement IntelliJ IDEA message bus communication to enable real-time cross-project synchronization of host configuration changes.

**Architecture:** Create a message interface for configuration change events, integrate publishing into the HostConfigurationService, and add subscribers to UI components for automatic refresh across projects.

**Tech Stack:** IntelliJ IDEA Message Bus API, Java Swing UI components, IntelliJ Platform SDK

---

## File Structure

- **Create:** `src/main/java/org/hostrunner/messaging/HostConfigurationMessageHandler.java` - Message interface definition
- **Create:** `src/main/java/org/hostrunner/messaging/HostConfigurationMessagePublisher.java` - Message publishing service
- **Modify:** `src/main/java/org/hostrunner/service/HostConfigurationService.java` - Add message publishing to CRUD operations
- **Modify:** `src/main/java/org/hostrunner/toolwindow/ConfigurationManagementPanel.java` - Add message subscription
- **Modify:** `src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java` - Add message subscription
- **Modify:** `src/main/java/org/hostrunner/toolwindow/HostConfigurationPanel.java` - Add message subscription

## Task Breakdown

### Task 1: Create Message Handler Interface

**Files:**
- Create: `src/main/java/org/hostrunner/messaging/HostConfigurationMessageHandler.java`

- [ ] **Step 1: Write the message handler interface**

```java
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
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/messaging/HostConfigurationMessageHandler.java
git commit -m "feat: add message handler interface for configuration changes"
```

### Task 2: Create Message Publisher Service

**Files:**
- Create: `src/main/java/org/hostrunner/messaging/HostConfigurationMessagePublisher.java`

- [ ] **Step 1: Write the message publisher service**

```java
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
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/messaging/HostConfigurationMessagePublisher.java
git commit -m "feat: add message publisher service for configuration changes"
```

### Task 3: Integrate Message Publishing into HostConfigurationService

**Files:**
- Modify: `src/main/java/org/hostrunner/service/HostConfigurationService.java`

- [ ] **Step 1: Add message publisher dependency and integrate publishing**

```java
package org.hostrunner.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.persistent.HostConfigurationState;
import org.hostrunner.messaging.HostConfigurationMessagePublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置管理服务
 */
@Service
public final class HostConfigurationService {

    private final HostConfigurationState state;
    private final HostConfigurationMessagePublisher messagePublisher;

    public HostConfigurationService() {
        this.state = HostConfigurationState.getInstance();
        this.messagePublisher = HostConfigurationMessagePublisher.getInstance();
    }

    public static HostConfigurationService getInstance() {
        return ApplicationManager.getApplication()
            .getService(HostConfigurationService.class);
    }

    // ... existing methods unchanged until addConfiguration ...

    /**
     * 添加新配置
     */
    public void addConfiguration(HostConfiguration configuration) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        configs.add(configuration);
        state.setConfigurations(configs);

        // 发布配置添加消息
        messagePublisher.publishConfigurationAdded(configuration.getId());
    }

    /**
     * 更新配置
     */
    public void updateConfiguration(HostConfiguration configuration) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getId().equals(configuration.getId())) {
                configs.set(i, configuration);
                break;
            }
        }
        state.setConfigurations(configs);

        // 发布配置更新消息
        messagePublisher.publishConfigurationUpdated(configuration.getId());
    }

    /**
     * 删除配置
     */
    public void removeConfiguration(String configurationId) {
        List<HostConfiguration> configs = new ArrayList<>(state.getConfigurations());
        configs.removeIf(config -> config.getId().equals(configurationId));
        state.setConfigurations(configs);

        // 如果删除的是当前选中的配置，清空选中状态
        if (configurationId.equals(state.getSelectedConfigurationId())) {
            state.setSelectedConfigurationId(null);
        }

        // 发布配置删除消息
        messagePublisher.publishConfigurationDeleted(configurationId);
    }

    /**
     * 选择配置
     */
    public void selectConfiguration(String configurationId) {
        // 验证配置是否存在
        HostConfiguration config = getConfigurationById(configurationId);
        if (config != null) {
            state.setSelectedConfigurationId(configurationId);

            // 发布配置选择消息
            messagePublisher.publishConfigurationSelected(configurationId);
        }
    }

    /**
     * 取消选择配置
     */
    public void deselectConfiguration() {
        state.setSelectedConfigurationId(null);

        // 发布配置取消选择消息
        messagePublisher.publishConfigurationSelected(null);
    }

    // ... rest of existing methods unchanged ...
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/service/HostConfigurationService.java
git commit -m "feat: integrate message publishing into HostConfigurationService"
```

### Task 4: Add Message Subscription to ConfigurationManagementPanel

**Files:**
- Modify: `src/main/java/org/hostrunner/toolwindow/ConfigurationManagementPanel.java`

- [ ] **Step 1: Add message subscription to the panel**

```java
package org.hostrunner.toolwindow;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.settings.HostConfigurationForm;
import org.hostrunner.settings.HostConfigurationTableModel;
import org.hostrunner.toolwindow.HostConfigurationStatusWidget;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 配置管理标签页
 */
public class ConfigurationManagementPanel extends JPanel {

    private JBTable configurationTable;
    private HostConfigurationTableModel tableModel;
    private final HostConfigurationService service;
    private final Consumer<Void> refreshCallback;
    private final com.intellij.openapi.project.Project project;
    private MessageBusConnection messageBusConnection;

    public ConfigurationManagementPanel(com.intellij.openapi.project.Project project, Consumer<Void> refreshCallback) {
        this.service = HostConfigurationService.getInstance();
        this.refreshCallback = refreshCallback;
        this.project = project;
        initializeComponents();
        setupMessageBusSubscription();
    }

    private void setupMessageBusSubscription() {
        // 订阅消息总线以接收配置变更通知
        messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                // 避免处理自己发送的消息（可选优化）
                if (projectName.equals(project.getName())) {
                    return;
                }

                // 在EDT中执行UI更新
                SwingUtilities.invokeLater(() -> {
                    refreshTable();
                    // 通知其他标签页刷新
                    if (refreshCallback != null) {
                        refreshCallback.accept(null);
                    }
                    // 更新状态栏显示
                    HostConfigurationStatusWidget.updateStatus(project);
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // 创建表格模型
        tableModel = new HostConfigurationTableModel();
        configurationTable = new JBTable(tableModel);
        configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 创建工具栏装饰器
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(configurationTable);
        decorator.setAddAction(anActionButton -> addConfiguration());
        decorator.setEditAction(anActionButton -> editConfiguration());
        decorator.setRemoveAction(anActionButton -> removeConfiguration());

        add(decorator.createPanel(), BorderLayout.CENTER);

        // 初始刷新表格
        refreshTable();
    }

    // ... existing methods unchanged ...

    @Override
    public void removeNotify() {
        super.removeNotify();
        // 清理消息总线连接
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationManagementPanel.java
git commit -m "feat: add message subscription to ConfigurationManagementPanel"
```

### Task 5: Add Message Subscription to ConfigurationSelectionPanel

**Files:**
- Modify: `src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java`

- [ ] **Step 1: Add message subscription to existing ConfigurationSelectionPanel**

```java
package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.toolwindow.HostConfigurationStatusWidget;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 配置选择标签页
 */
public class ConfigurationSelectionPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JPanel cardsPanel;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton viewCurrentButton;
    private ButtonGroup selectionGroup;
    private MessageBusConnection messageBusConnection;

    public ConfigurationSelectionPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        setupMessageBusSubscription();
        refreshConfigurations();
    }

    private void setupMessageBusSubscription() {
        // 订阅消息总线以接收配置变更通知
        messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                // 避免处理自己发送的消息（可选优化）
                if (projectName.equals(project.getName())) {
                    return;
                }

                // 在EDT中执行UI更新
                SwingUtilities.invokeLater(() -> {
                    refreshConfigurations();
                    validateSelection();
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // 刷新按钮
        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshConfigurations());
        refreshButton.setFocusPainted(false);
        topPanel.add(refreshButton);

        // 清空选择按钮
        clearButton = new JButton("清空选择");
        clearButton.addActionListener(e -> clearAllSelections());
        clearButton.setFocusPainted(false);
        topPanel.add(clearButton);

        // 查看当前配置按钮
        viewCurrentButton = new JButton("查看当前配置");
        viewCurrentButton.addActionListener(e -> showCurrentConfigurationDetail());
        viewCurrentButton.setFocusPainted(false);
        topPanel.add(viewCurrentButton);

        add(topPanel, BorderLayout.NORTH);

        // 卡片面板
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ... existing methods remain unchanged until refresh() ...

    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // 清理消息总线连接
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java
git commit -m "feat: add message subscription to ConfigurationSelectionPanel"
```

### Task 6: Add Message Subscription to HostConfigurationPanel

**Files:**
- Modify: `src/main/java/org/hostrunner/toolwindow/HostConfigurationPanel.java`

- [ ] **Step 1: Add message subscription to HostConfigurationPanel**

```java
package org.hostrunner.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 工具窗口主面板 - 双标签页结构
 */
public class HostConfigurationPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JTabbedPane tabbedPane;
    private ConfigurationManagementPanel managementPanel;
    private ConfigurationSelectionPanel selectionPanel;
    private MessageBusConnection messageBusConnection;

    public HostConfigurationPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        setupMessageBusSubscription();
    }

    private void setupMessageBusSubscription() {
        // 订阅消息总线以接收配置变更通知
        messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                // 避免处理自己发送的消息（可选优化）
                if (projectName.equals(project.getName())) {
                    return;
                }

                // 在EDT中执行UI更新
                SwingUtilities.invokeLater(() -> {
                    refresh();
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建标签页容器
        tabbedPane = new JTabbedPane();

        // 创建配置管理标签页
        managementPanel = new ConfigurationManagementPanel(project, this::onConfigurationChanged);

        // 创建配置选择标签页
        selectionPanel = new ConfigurationSelectionPanel(project);

        // 添加标签页（配置选择作为默认第一个标签页）
        tabbedPane.addTab("配置选择", selectionPanel);
        tabbedPane.addTab("配置管理", managementPanel);

        // 添加标签页切换监听器
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 当切换到配置选择标签页时，验证当前选择
                if (tabbedPane.getSelectedIndex() == 0) { // 配置选择标签页是第一个
                    selectionPanel.validateSelection();
                }
                // 更新状态栏显示
                HostConfigurationStatusWidget.updateStatus(project);
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void onConfigurationChanged(Void unused) {
        // 当配置管理标签页发生更改时，刷新选择标签页
        selectionPanel.refresh();
        // 更新状态栏显示
        HostConfigurationStatusWidget.updateStatus(project);
    }

    public void refresh() {
        // 刷新所有标签页
        managementPanel.refreshTable();
        selectionPanel.refresh();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // 清理消息总线连接
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: Compilation successful

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/hostrunner/toolwindow/HostConfigurationPanel.java
git commit -m "feat: add message subscription to HostConfigurationPanel"
```

### Task 7: Test Cross-Project Message Synchronization

**Files:**
- Test: Manual testing procedure

- [ ] **Step 1: Build the plugin**

Run: `./gradlew buildPlugin`
Expected: Build successful

- [ ] **Step 2: Install plugin in test IDE**

Run: Install the built plugin in IntelliJ IDEA
Expected: Plugin installs without errors

- [ ] **Step 3: Test cross-project synchronization**

Manual Test Procedure:
1. Open two IntelliJ IDEA projects with the plugin installed
2. In Project A, open the Host Configuration tool window
3. In Project B, open the Host Configuration tool window
4. In Project A, add a new configuration
5. Verify that Project B's UI automatically refreshes to show the new configuration
6. In Project A, edit an existing configuration
7. Verify that Project B's UI automatically refreshes to show the updated configuration
8. In Project A, delete a configuration
9. Verify that Project B's UI automatically refreshes and removes the deleted configuration
10. In Project A, select a different configuration
11. Verify that Project B's UI automatically refreshes to reflect the selection change

Expected: All cross-project synchronization works correctly

- [ ] **Step 4: Test message filtering (optional optimization)**

Verify that projects don't process their own messages by checking that:
1. Changes made in Project A don't cause duplicate refreshes in Project A
2. Changes made in Project B don't cause duplicate refreshes in Project B

Expected: No duplicate refreshes occur

- [ ] **Step 5: Commit test results**

```bash
git commit -m "test: verify cross-project message synchronization works correctly"
```

### Task 8: Final Integration Test

**Files:**
- Test: Complete integration test

- [ ] **Step 1: Run complete integration test**

Run: `./gradlew test`
Expected: All tests pass

- [ ] **Step 2: Verify plugin functionality**

Manual verification checklist:
- [ ] Configuration addition works in single project
- [ ] Configuration editing works in single project
- [ ] Configuration deletion works in single project
- [ ] Configuration selection works in single project
- [ ] Cross-project synchronization works for additions
- [ ] Cross-project