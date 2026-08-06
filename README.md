# HostRunner - IntelliJ IDEA Hosts 配置管理插件

HostRunner 是一个 IntelliJ IDEA 插件，帮助开发者快速管理和切换不同的 hosts 配置与 VM 选项，让开发环境配置变得简单高效。

## ✨ 主要功能

| 功能 | 说明 |
|------|------|
| **配置管理** | 在设置页面管理多组 hosts 和 VM 选项配置 |
| **模板功能** | 提供开发、测试、生产环境预设模板，开箱即用 |
| **快速切换** | 通过工具窗口卡片或快捷键弹窗快速选择配置 |
| **快捷键支持** | 默认 `Ctrl+Shift+H` 打开快速选择弹窗，支持自定义 |
| **自动注入** | 启动 Spring Boot / Application 时自动注入 VM 选项 |
| **Hosts 更新** | 选中配置后自动更新系统 hosts 文件 |
| **状态栏显示** | 底部状态栏实时显示当前选中的配置名称 |

## 🚀 安装

1. 下载项目代码并构建插件：
   ```bash
   ./gradlew buildPlugin
   ```
2. 构建产物位于 `build/distributions/` 目录下。
3. 打开 IntelliJ IDEA，进入 `Settings` > `Plugins` > `⚙️` > `Install Plugin from Disk`。
4. 选择构建好的 `.zip` 插件文件。
5. 重启 IDEA。

## 📖 使用指南

### 1. 管理配置

在设置页面中添加和管理你的配置：

1. 打开 `Settings` > `Tools` > `Host Configuration`。
2. 点击 **`Add from Template`** 使用预设模板（开发 / 测试 / 生产环境）。
3. 或点击 **`Add`** 手动创建配置，填写：
   - **配置名称** — 唯一标识
   - **Hosts 内容** — 如 `127.0.0.1 dev-api.example.com`
   - **VM 选项** — 如 `-Xmx512m -Dspring.profiles.active=dev`

### 2. 快速切换配置（工具窗口）

1. 在 IDEA 右侧工具窗口中找到 **"Host Configuration"**。
2. 点击选择任意配置卡片。
3. 配置立即生效 — hosts 文件自动更新，状态栏同步显示。

### 3. 快速切换配置（快捷键弹窗）

1. 按下快捷键 **`Ctrl+Shift+H`** 打开快速选择弹窗。
2. 在搜索框中输入配置名称进行过滤。
3. 点击选中目标配置，弹窗自动关闭，配置立即生效。

### 4. 配置快捷键

如果你想修改默认的快捷键绑定，按以下步骤操作：

1. 打开 `Settings` > `Keymap`。
2. 在搜索框中输入 **`HostRunner`** 或 **`Quick Pick Configuration`**。
3. 找到 **`HostRunner: Quick Pick Configuration`** 条目。
4. 右键点击 > **Add Keyboard Shortcut**。
5. 按下你想要的快捷键组合（例如 `Ctrl+Alt+H`）。
6. 点击 **OK** 保存。

> **提示**：如果新快捷键与现有快捷键冲突，IDEA 会提示你，可以选择移除冲突或重新选择快捷键。

### 5. 启动应用

1. 创建 Spring Boot 或 Application 运行配置。
2. 选中一个 HostRunner 配置（通过工具窗口或快捷键弹窗）。
3. 启动应用 — 插件自动将 VM 选项注入到 Java 启动参数中。

## 📋 预设模板

插件内置三种环境模板，可通过 `Settings` > `Tools` > `Host Configuration` > `Add from Template` 使用：

| 模板 | Hosts 示例 | VM 选项示例 |
|------|-----------|------------|
| **开发环境** | `127.0.0.1 dev-api.example.com` | `-Xmx512m -Dspring.profiles.active=dev -Dspring.devtools.restart.enabled=true` |
| **测试环境** | `192.168.1.100 test-api.example.com` | `-Xmx1024m -Dspring.profiles.active=test -Dlogging.level.root=DEBUG` |
| **生产环境（调试）** | 最小化 hosts | `-Xmx2048m -XX:+UseG1GC` |

## 🔧 配置参数说明

| 参数 | 说明 |
|------|------|
| **配置名称** | 配置的唯一标识，不可重复 |
| **Hosts 内容** | 写入系统 hosts 文件的内容，支持多行 |
| **VM 选项** | Java 虚拟机参数，启动应用时自动注入 |

## 🎯 使用场景

- **开发环境切换** — 在不同 API 地址之间快速切换
- **测试环境配置** — 一键指向测试服务器
- **生产环境调试** — 临时修改 hosts 进行问题排查
- **团队协作** — 统一开发环境配置，减少 "在我机器上能跑" 问题

## 🛠️ 技术说明

- **Hosts 文件管理**：插件通过标记注释（`# === HOSTRUNNER START ===` / `# === HOSTRUNNER END ===`）管理 hosts 文件中的内容，不会影响已有的 hosts 条目。
- **权限提升**：写入 hosts 文件需要管理员权限。Windows 下使用 PowerShell `Start-Process -Verb RunAs`，macOS 下使用 `osascript` 提权。
- **IDE 兼容**：支持 IntelliJ IDEA 2024.3 ~ 2026.1（Community Edition）。

---

让开发环境配置变得更加简单！ 🎯
