# 设计文档：快捷键快速选择配置 + hosts文件弹框放大

**日期**: 2026-04-28
**项目**: HostRunner (IntelliJ IDEA Plugin)

---

## 背景

当前 HostRunner 插件的配置选择功能位于右侧工具窗口的「配置选择」Tab 中。用户需要切换到工具窗口才能操作，效率较低。同时，「查看hosts文件」弹框尺寸偏小（20行×60列），查看大量 hosts 条目不便。

---

## 目标

1. 新增快捷键触发的弹框，支持搜索和选择配置，选中后立即应用，弹框不关闭
2. 放大「查看hosts文件」弹框，改为 40行×100列，preferredSize 设为 900×700

---

## 架构决策

**抽取公共选择组件**（方案B）：将搜索 + 卡片选择逻辑从 `ConfigurationSelectionPanel` 抽取为独立组件 `ConfigurationPickerPanel`，供工具窗口和快捷键弹框共享，避免代码重复。

---

## 组件设计

### 1. `ConfigurationPickerPanel`（新建）

**职责**：搜索 + 展示配置卡片 + 选中回调

**文件**: `src/main/java/org/hostrunner/toolwindow/ConfigurationPickerPanel.java`

```
ConfigurationPickerPanel
  ├── searchField (JTextField)         -- 实时过滤
  ├── cardsPanel (JPanel, BoxLayout)   -- 配置卡片列表
  │     └── ConfigurationCard[]        -- 复用现有组件
  └── callback                          -- 选中时回调外部
```

**接口设计**：
```java
public interface ConfigurationSelectionListener {
    void onConfigurationSelected(HostConfiguration config);
}
```

接收 `List<HostConfiguration>` 和 `ConfigurationSelectionListener`，内部处理搜索过滤和卡片渲染，选中时调用回调。

### 2. `ConfigurationPickerDialog`（新建）

**职责**：快捷键触发的弹框容器

**文件**: `src/main/java/org/hostrunner/toolwindow/ConfigurationPickerDialog.java`

- 继承或直接使用 `JDialog`（模态对话框）
- 标题：「快速选择配置」
- 内部嵌入 `ConfigurationPickerPanel`
- 选中配置 → 立即应用（调用 `HostConfigurationService.applyConfiguration()`）
- 弹框**不关闭**，用户可连续切换
- 底部「关闭」按钮 + `ESC` 键关闭
- 尺寸：与工具窗口宽度相当，高度适中（pack() 自适应）

### 3. `QuickPickConfigurationAction`（新建）

**职责**：IntelliJ Action，响应快捷键打开弹框

**文件**: `src/main/java/org/hostrunner/action/QuickPickConfigurationAction.java`

- 继承 `AnAction`
- `actionPerformed` 中创建并显示 `ConfigurationPickerDialog`

### 4. `plugin.xml` 变更

新增 action 注册：

```xml
<action id="org.hostrunner.QuickPickConfiguration"
        class="org.hostrunner.action.QuickPickConfigurationAction"
        text="快速选择Host配置"
        description="通过快捷键弹出配置选择弹框">
  <keyboard-shortcut keymap="$default" first-keystroke="ctrl shift H"/>
</action>
```

默认快捷键 `Ctrl+Shift+H`，用户可在 IntelliJ Keymap 设置中自行修改。

### 5. `ConfigurationSelectionPanel` 重构

- 移除搜索框和卡片列表的代码
- 改为嵌入 `ConfigurationPickerPanel`
- 保留自己独有的按钮：刷新、清空选择、查看hosts文件
- 实现 `ConfigurationSelectionListener` 接口

### 6. hosts 文件弹框尺寸调整

`ConfigurationSelectionPanel.viewHostsFile()` 修改：

```java
// 之前: new JTextArea(hostsContent, 20, 60)
JTextArea textArea = new JTextArea(hostsContent, 40, 100);

// pack() 前增加
dialog.setPreferredSize(new Dimension(900, 700));
```

---

## 数据流

```
快捷键按下
  → QuickPickConfigurationAction.actionPerformed()
    → 创建 ConfigurationPickerDialog
      → 创建 ConfigurationPickerPanel（传入配置列表 + 回调）
        → 用户搜索/选择
          → 回调触发 HostConfigurationService.applyConfiguration()
          → 配置立即生效，弹框保持打开
```

---

## 文件变更清单

| 操作 | 文件 |
|------|------|
| 新建 | `toolwindow/ConfigurationPickerPanel.java` |
| 新建 | `toolwindow/ConfigurationPickerDialog.java` |
| 新建 | `action/QuickPickConfigurationAction.java` |
| 重构 | `toolwindow/ConfigurationSelectionPanel.java` |
| 修改 | `toolwindow/ConfigurationSelectionPanel.java`（hosts弹框尺寸） |
| 修改 | `resources/META-INF/plugin.xml`（注册 action） |

---

## 不做的

- 不在弹框中支持编辑/新增配置（那是「配置管理」Tab 的职责）
- 不在弹框中显示「刷新」「清空选择」「查看hosts文件」按钮
- 不固定快捷键，由用户自行配置
