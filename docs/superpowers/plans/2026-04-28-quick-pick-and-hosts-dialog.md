# 快捷键快速选择配置 + hosts文件弹框放大 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增快捷键触发的配置选择弹框（搜索+选择+立即应用），并放大hosts文件查看弹框

**Architecture:** 将搜索+卡片选择逻辑抽取为独立组件 `ConfigurationPickerPanel`，供工具窗口和弹框共享。新增 `ConfigurationPickerDialog` 作为弹框容器，`QuickPickConfigurationAction` 响应快捷键。hosts弹框直接修改尺寸参数。

**Tech Stack:** Java 17, Swing (JDialog, JPanel, JTextArea), IntelliJ Platform SDK (AnAction, plugin.xml)

---

## 文件结构

| 操作 | 文件 | 职责 |
|------|------|------|
| 新建 | `toolwindow/ConfigurationPickerPanel.java` | 搜索+卡片选择的可复用组件 |
| 新建 | `toolwindow/ConfigurationPickerDialog.java` | 快捷键触发的弹框容器 |
| 新建 | `action/QuickPickConfigurationAction.java` | IntelliJ Action，响应快捷键 |
| 重构 | `toolwindow/ConfigurationSelectionPanel.java` | 嵌入 ConfigurationPickerPanel，保留按钮 |
| 修改 | `toolwindow/ConfigurationSelectionPanel.java` `viewHostsFile()` | hosts弹框尺寸放大 |
| 修改 | `resources/META-INF/plugin.xml` | 注册 action + 快捷键 |

---

## Task 1: 放大hosts文件查看弹框

**Files:**
- Modify: `src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java:290-330`

- [ ] **Step 1: 修改 viewHostsFile() 方法中的 JTextArea 尺寸**

将 `new JTextArea(hostsContent, 20, 60)` 改为 `new JTextArea(hostsContent, 40, 100)`。

```java
JTextArea textArea = new JTextArea(hostsContent, 40, 100);
```

- [ ] **Step 2: 在 pack() 前设置对话框 preferredSize**

在 `dialog.pack();` 之前增加：

```java
dialog.setPreferredSize(new Dimension(900, 700));
```

- [ ] **Step 3: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java
git commit -m "feat: 放大hosts文件查看弹框至40行100列/900x700"
```

---

## Task 2: 新建 ConfigurationPickerPanel

**Files:**
- Create: `src/main/java/org/hostrunner/toolwindow/ConfigurationPickerPanel.java`

- [ ] **Step 1: 新建 ConfigurationPickerPanel.java**

这个组件从 `ConfigurationSelectionPanel` 中抽取搜索 + 卡片选择逻辑。它不依赖 `Project` 对象（不需要消息总线），接收配置列表和选中回调。

```java
package org.hostrunner.toolwindow;

import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可复用的配置选择面板 — 搜索框 + 配置卡片列表
 */
public class ConfigurationPickerPanel extends JPanel {

    private final Consumer<HostConfiguration> selectionCallback;
    private JPanel cardsPanel;
    private JTextField searchField;
    private ButtonGroup selectionGroup;
    private List<HostConfiguration> allConfigurations;
    private HostConfigurationService service;

    public ConfigurationPickerPanel(Consumer<HostConfiguration> selectionCallback) {
        this.selectionCallback = selectionCallback;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        refreshConfigurations();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 搜索框
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("搜索:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterConfigurations(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterConfigurations(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterConfigurations(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // 卡片面板
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshConfigurations() {
        allConfigurations = service.getAllConfigurations();
        filterConfigurations();
    }

    private void filterConfigurations() {
        String searchText = searchField.getText().trim();
        cardsPanel.removeAll();
        selectionGroup = new ButtonGroup();

        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (allConfigurations.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无配置，请先在配置管理中添加配置");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            cardsPanel.add(emptyLabel);
        } else {
            List<HostConfiguration> filtered = allConfigurations;
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                filtered = allConfigurations.stream()
                    .filter(config -> {
                        String name = config.getName().toLowerCase();
                        String hosts = config.getHostsContent() != null ? config.getHostsContent().toLowerCase() : "";
                        String vm = config.getVmOptions() != null ? config.getVmOptions().toLowerCase() : "";
                        return name.contains(searchLower) || hosts.contains(searchLower) || vm.contains(searchLower);
                    })
                    .collect(java.util.stream.Collectors.toList());
            }

            if (filtered.isEmpty() && !searchText.isEmpty()) {
                JLabel noResults = new JLabel("未找到匹配的配置");
                noResults.setHorizontalAlignment(SwingConstants.CENTER);
                noResults.setForeground(UIManager.getColor("Label.disabledForeground"));
                cardsPanel.add(noResults);
            } else {
                for (HostConfiguration config : filtered) {
                    boolean isSelected = selectedConfig != null && selectedConfig.getId().equals(config.getId());
                    ConfigurationCard card = new ConfigurationCard(config, isSelected, selectionGroup, selectionCallback);
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createVerticalStrut(5));
                }
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    /**
     * 刷新配置列表（供外部调用）
     */
    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    /**
     * 获取搜索框组件（供外部设置焦点）
     */
    public JTextField getSearchField() {
        return searchField;
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationPickerPanel.java
git commit -m "feat: 新建 ConfigurationPickerPanel 可复用配置选择组件"
```

---

## Task 3: 新建 ConfigurationPickerDialog

**Files:**
- Create: `src/main/java/org/hostrunner/toolwindow/ConfigurationPickerDialog.java`

- [ ] **Step 1: 新建 ConfigurationPickerDialog.java**

弹框容器，嵌入 `ConfigurationPickerPanel`，选中后立即应用配置，弹框不关闭。

```java
package org.hostrunner.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;
import org.hostrunner.springboot.HostsFileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 快速选择配置弹框 — 快捷键触发的模态对话框
 */
public class ConfigurationPickerDialog extends JDialog {

    private final ConfigurationPickerPanel pickerPanel;
    private final HostConfigurationService service;

    public ConfigurationPickerDialog() {
        // 使用当前活跃项目的窗口作为 owner
        Project project = getActiveProject();
        Frame owner = project != null ? JOptionPane.getRootFrame() : null;
        super(owner, "快速选择配置", true);

        this.service = HostConfigurationService.getInstance();

        // 选中回调：立即应用配置，不关闭弹框
        this.pickerPanel = new ConfigurationPickerPanel(this::onConfigurationSelected);

        initializeUI();
    }

    private Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0];
        }
        return null;
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        add(pickerPanel, BorderLayout.CENTER);

        // 底部关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ESC 键关闭
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setPreferredSize(new Dimension(500, 600));
        pack();
        setLocationRelativeTo(null);

        // 弹框打开后自动聚焦搜索框
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                pickerPanel.getSearchField().requestFocusInWindow();
            }
        });
    }

    private void onConfigurationSelected(HostConfiguration config) {
        if (config == null) return;

        // 选择配置
        service.selectConfiguration(config.getId());

        // 立即更新 hosts 文件
        try {
            HostsFileManager.updateHostsFile(config.getHostsContent());
        } catch (Exception e) {
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }

        // 更新状态栏
        Project project = getActiveProject();
        if (project != null) {
            HostConfigurationStatusWidget.updateStatus(project);
        }

        // 刷新卡片选中状态（不关闭弹框）
        pickerPanel.refresh();
    }
}
```

注意：需要确保 `HostsFileManager` 和 `HostConfigurationStatusWidget` 的 import 路径正确：
- `org.hostrunner.springboot.HostsFileManager`
- `org.hostrunner.toolwindow.HostConfigurationStatusWidget`

- [ ] **Step 2: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationPickerDialog.java
git commit -m "feat: 新建 ConfigurationPickerDialog 快速选择配置弹框"
```

---

## Task 4: 新建 QuickPickConfigurationAction

**Files:**
- Create: `src/main/java/org/hostrunner/action/QuickPickConfigurationAction.java`

- [ ] **Step 1: 新建 action 目录和 QuickPickConfigurationAction.java**

```bash
mkdir -p src/main/java/org/hostrunner/action
```

```java
package org.hostrunner.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.hostrunner.toolwindow.ConfigurationPickerDialog;
import org.jetbrains.annotations.NotNull;

/**
 * 快速选择Host配置 — 快捷键触发的Action
 */
public class QuickPickConfigurationAction extends AnAction {

    public QuickPickConfigurationAction() {
        super("快速选择Host配置", "通过快捷键弹出配置选择弹框", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConfigurationPickerDialog dialog = new ConfigurationPickerDialog();
        dialog.setVisible(true);
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/hostrunner/action/QuickPickConfigurationAction.java
git commit -m "feat: 新建 QuickPickConfigurationAction 快捷键Action"
```

---

## Task 5: 重构 ConfigurationSelectionPanel

**Files:**
- Modify: `src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java`

将搜索框 + 卡片列表替换为嵌入 `ConfigurationPickerPanel`，保留刷新、清空选择、查看hosts文件按钮。

- [ ] **Step 1: 修改 ConfigurationSelectionPanel.java**

重构后的完整代码：

```java
package org.hostrunner.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.openapi.ui.Messages;
import org.hostrunner.messaging.HostConfigurationMessageHandler;
import org.hostrunner.model.HostConfiguration;
import org.hostrunner.service.HostConfigurationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 配置选择标签页
 */
public class ConfigurationSelectionPanel extends JPanel {

    private final Project project;
    private final HostConfigurationService service;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton viewHostsButton;
    private ConfigurationPickerPanel pickerPanel;
    private MessageBusConnection messageBusConnection;

    public ConfigurationSelectionPanel(Project project) {
        this.project = project;
        this.service = HostConfigurationService.getInstance();
        initializeComponents();
        setupMessageBusSubscription();
        pickerPanel.refresh();
    }

    private void setupMessageBusSubscription() {
        messageBusConnection = ApplicationManager.getApplication()
            .getMessageBus().connect(project);
        messageBusConnection.subscribe(HostConfigurationMessageHandler.TOPIC, new HostConfigurationMessageHandler() {
            @Override
            public void onConfigurationChanged(String changeType, String configurationId, String projectName) {
                if (projectName.equals(project.getName())) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    pickerPanel.refresh();
                    validateSelection();
                });
            }
        });
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshConfigurations());
        refreshButton.setFocusPainted(false);
        buttonPanel.add(refreshButton);

        clearButton = new JButton("清空选择");
        clearButton.addActionListener(e -> clearAllSelections());
        clearButton.setFocusPainted(false);
        buttonPanel.add(clearButton);

        viewHostsButton = new JButton("查看hosts文件");
        viewHostsButton.addActionListener(e -> viewHostsFile());
        viewHostsButton.setFocusPainted(false);
        buttonPanel.add(viewHostsButton);

        add(buttonPanel, BorderLayout.NORTH);

        // 嵌入 ConfigurationPickerPanel
        pickerPanel = new ConfigurationPickerPanel(this::onConfigurationSelected);
        add(pickerPanel, BorderLayout.CENTER);
    }

    private void refreshConfigurations() {
        pickerPanel.refresh();
    }

    private void onConfigurationSelected(HostConfiguration configuration) {
        if (configuration != null) {
            service.selectConfiguration(configuration.getId());
            updateHostsFileImmediately(configuration);
            updateClearButtonState();
            HostConfigurationStatusWidget.updateStatus(project);
        } else {
            service.deselectConfiguration();
            clearHostsFileImmediately();
            updateClearButtonState();
            HostConfigurationStatusWidget.updateStatus(project);
        }
    }

    private void clearAllSelections() {
        service.deselectConfiguration();
        clearHostsFileImmediately();
        pickerPanel.refresh();
    }

    private void updateClearButtonState() {
        clearButton.setEnabled(true);
        clearButton.setToolTipText("清空所有选择");
    }

    private void updateHostsFileImmediately(HostConfiguration configuration) {
        try {
            org.hostrunner.springboot.HostsFileManager.updateHostsFile(configuration.getHostsContent());
        } catch (Exception e) {
            System.err.println("更新hosts文件失败: " + e.getMessage());
        }
    }

    private void clearHostsFileImmediately() {
        try {
            org.hostrunner.springboot.HostsFileManager.clearHostsFile();
        } catch (Exception e) {
            System.err.println("清空hosts文件失败: " + e.getMessage());
        }
    }

    private void viewHostsFile() {
        try {
            String hostsContent = org.hostrunner.springboot.HostsFileManager.readCurrentHosts();

            JTextArea textArea = new JTextArea(hostsContent, 40, 100);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            JDialog dialog = new JDialog();
            dialog.setTitle("本地hosts文件内容");
            dialog.setModal(true);
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setPreferredSize(new Dimension(900, 700));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            Messages.showErrorDialog(
                project,
                "读取hosts文件失败: " + e.getMessage(),
                "读取失败"
            );
        }
    }

    public void refresh() {
        SwingUtilities.invokeLater(this::refreshConfigurations);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
            messageBusConnection = null;
        }
    }

    /**
     * 验证当前选中的配置是否存在
     */
    public void validateSelection() {
        List<HostConfiguration> configurations = service.getAllConfigurations();
        HostConfiguration selectedConfig = service.getSelectedConfiguration();

        if (selectedConfig != null) {
            final String selectedConfigId = selectedConfig.getId();
            boolean configExists = configurations.stream()
                .anyMatch(config -> config.getId().equals(selectedConfigId));

            if (!configExists) {
                service.deselectConfiguration();
                clearHostsFileImmediately();
                SwingUtilities.invokeLater(() -> pickerPanel.refresh());
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL。注意检查是否有 `selectionGroup`、`searchField`、`cardsPanel` 等已删除变量的残留引用导致编译错误。

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/hostrunner/toolwindow/ConfigurationSelectionPanel.java
git commit -m "refactor: 重构 ConfigurationSelectionPanel 嵌入 ConfigurationPickerPanel"
```

---

## Task 6: 注册 Action 到 plugin.xml

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: 在 `</idea-plugin>` 前添加 `<actions>` 段**

在 `</extensions>` 之后、`</idea-plugin>` 之前添加：

```xml
    <actions>
        <action id="org.hostrunner.QuickPickConfiguration"
                class="org.hostrunner.action.QuickPickConfigurationAction"
                text="快速选择Host配置"
                description="通过快捷键弹出配置选择弹框">
            <keyboard-shortcut keymap="$default" first-keystroke="ctrl shift H"/>
        </action>
    </actions>
```

完整的 plugin.xml `</extensions>` 之后部分：

```xml
    </extensions>

    <actions>
        <action id="org.hostrunner.QuickPickConfiguration"
                class="org.hostrunner.action.QuickPickConfigurationAction"
                text="快速选择Host配置"
                description="通过快捷键弹出配置选择弹框">
            <keyboard-shortcut keymap="$default" first-keystroke="ctrl shift H"/>
        </action>
    </actions>

</idea-plugin>
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 完整构建验证**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/resources/META-INF/plugin.xml
git commit -m "feat: 注册 QuickPickConfigurationAction 到 plugin.xml，默认快捷键 Ctrl+Shift+H"
```

---

## 验收标准

1. **hosts弹框放大**: 点击「查看hosts文件」，弹框明显更大（约 900×700），文本区域 40 行 × 100 列
2. **快捷键弹框**: 在 IntelliJ 中按 `Ctrl+Shift+H`，弹出「快速选择配置」对话框
3. **搜索功能**: 弹框中输入关键词，配置列表实时过滤
4. **选中即应用**: 点击某个配置卡片，hosts 文件立即更新，弹框不关闭
5. **关闭弹框**: 点击「关闭」按钮或按 ESC，弹框关闭
6. **工具窗口正常**: 原有的「配置选择」Tab 功能不变（搜索 + 卡片 + 三个按钮）
