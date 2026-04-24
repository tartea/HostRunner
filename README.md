# HostRunner - IntelliJ IDEA Hosts配置管理插件

![IntelliJ IDEA Plugin](https://img.shields.io/jetbrains/plugin/v/org.hostrunner.plugin)
![IntelliJ Platform](https://img.shields.io/jetbrains/plugin/d/org.hostrunner.plugin)
![License](https://img.shields.io/github/license/yourname/HostRunner)

HostRunner是一个强大的IntelliJ IDEA插件，专为开发者设计，用于快速管理和切换不同的hosts配置。通过提供直观的配置管理和自动化的VM选项注入，极大地简化了开发环境配置流程。

## ✨ 主要功能

### 🎯 核心功能
- **多组配置管理** - 支持添加、编辑、删除多个hosts配置
- **模板功能** - 提供开发、测试、生产环境预设模板
- **工具窗口** - 通过卡片式界面快速选择和切换配置
- **自动注入** - 启动时自动注入VM选项和更新hosts文件
- **智能适配** - 根据JDK版本自动选择正确的VM参数格式
- **实时更新** - 选中配置后立即生效，无需重启服务

### 🛠 技术特性
- **Spring Boot集成** - 完美支持Spring Boot应用启动配置
- **Application支持** - 同时支持标准Java Application配置
- **全局配置** - 配置存储在IDE全局设置中，所有项目共享
- **项目级生效** - 只在当前项目启动时生效
- **安全操作** - 完整的错误处理和输入验证

## 🚀 快速开始

### 安装方法

#### 方法1：从JetBrains插件市场安装（推荐）
1. 打开IntelliJ IDEA
2. 进入 `Settings` > `Plugins` > `Marketplace`
3. 搜索 "HostRunner"
4. 点击安装并重启IDEA

#### 方法2：从本地文件安装
1. 下载最新的`.zip`发布包
2. 进入 `Settings` > `Plugins` > `Install Plugin from Disk`
3. 选择下载的`.zip`文件
4. 重启IDEA完成安装

### 基本使用

#### 1. 配置管理
1. 打开 `Settings` > `Tools` > `Host Configuration`
2. 点击 `Add from Template` 使用预设模板快速创建配置
3. 或点击 `Add` 手动创建新配置
4. 填写配置名称、hosts内容、VM选项等信息

#### 2. 快速切换
1. 在IDEA右侧工具窗口找到 "Host Configuration"
2. 点击选择要使用的配置卡片
3. 配置立即生效，hosts文件自动更新

#### 3. 启动应用
1. 创建或编辑Spring Boot/Application运行配置
2. 启动应用，插件自动注入VM选项
3. 应用使用新的hosts配置运行

## 📋 配置模板

### 预设模板说明

#### 🟢 开发环境模板
- **适用场景**：本地开发调试
- **Hosts内容**：本地地址和开发服务器
- **VM选项**：开发模式参数，启用热部署
- **JDK版本**：JDK 9+

#### 🟡 测试环境模板
- **适用场景**：测试环境调试
- **Hosts内容**：测试服务器地址
- **VM选项**：测试模式参数，调试日志
- **JDK版本**：JDK 9+

#### 🔴 生产环境模板
- **适用场景**：生产环境调试
- **Hosts内容**：生产环境地址
- **VM选项**：性能优化参数
- **JDK版本**：JDK 9+

## 🔧 详细配置

### 配置参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| 配置名称 | 配置的唯一标识 | `开发环境` |
| Hosts内容 | hosts文件内容 | `127.0.0.1 localhost` |
| VM选项 | Java虚拟机参数 | `-Xmx512m -Dspring.profiles.active=dev` |
| JDK版本 | 选择JDK版本 | `JDK 9+` 或 `JDK 9-` |

### VM选项格式

#### JDK 9+ 格式
```bash
-Djdk.net.hosts.file=/path/to/project-host.txt [用户自定义VM选项]
```

#### JDK 9- 格式
```bash
-Dsun.net.hosts.file=/path/to/project-host.txt [用户自定义VM选项]
```

### Hosts文件格式
```
127.0.0.1 localhost
127.0.0.1 api.example.com
192.168.1.100 test-server.com
```

## 🎨 界面说明

### 设置页面

**位置**：`Settings` > `Tools` > `Host Configuration`

**功能**：
- 查看所有配置的列表
- 添加、编辑、删除配置
- 使用模板快速创建
- 配置验证和错误提示

### 工具窗口

**位置**：IDEA右侧面板

**组件**：
- 🔄 **刷新按钮** - 重新加载配置列表
- ⚙️ **设置按钮** - 快速跳转到设置页面
- 📋 **配置卡片** - 显示配置信息和选择状态
- 👁️ **查看按钮** - 显示配置详情

### 配置卡片

**显示内容**：
- 配置名称（粗体显示）
- Hosts第一行内容（灰色显示）
- 选中状态指示器
- 查看详情按钮

**交互**：
- 点击卡片选择配置
- 点击"查看"按钮显示详情
- 选中后立即生效

## 🔄 工作流程

### 典型使用场景

#### 场景1：开发环境切换
```mermaid
graph TD
    A[打开工具窗口] --> B[选择"开发环境"配置]
    B --> C[插件更新hosts文件]
    B --> D[保存选中状态]
    C --> E[启动Spring Boot应用]
    D --> E
    E --> F[应用使用新配置运行]
```

#### 场景2：临时调试
```mermaid
graph TD
    A[需要调试测试环境] --> B[在工具窗口选择"测试环境"]
    B --> C[hosts文件立即更新]
    C --> D[启动应用进行测试]
    D --> E[测试完成选择其他配置]
    E --> F[恢复原有配置]
```

### 状态管理

**配置状态**：
- ✅ **已选中** - 配置被选中，文件已更新
- ❌ **未选中** - 无配置选中，文件为空
- 🔄 **切换中** - 配置正在更新

**文件状态**：
- 📝 **有内容** - hosts文件包含选中配置的内容
- 🗑️ **已清空** - 无选中配置时文件内容为空

## ⚙️ 技术细节

### 文件存储

**配置存储位置**：
```
~/.IntelliJIdea/config/options/hostrunner-config.xml
```

**项目hosts文件**：
```
${PROJECT_DIR}/.idea/project-host.txt
```

### VM选项注入

**注入时机**：
1. 应用启动时
2. 运行配置扩展被触发
3. 检查是否有选中配置

**注入逻辑**：
- 有选中配置时：注入hosts文件参数 + 用户自定义VM选项
- 无选中配置时：不注入任何参数

### 错误处理

**常见错误**：
- ❌ 文件权限不足
- ❌ 配置名称重复
- ❌ VM选项格式错误
- ❌ 项目路径不存在

**处理策略**：
- 🔕 静默处理非关键错误
- 📢 用户友好的错误提示
- 🛡️ 输入验证和格式检查

## 🔍 故障排除

### 常见问题

#### Q: 配置不生效？
**A**: 检查以下几点：
1. 确保在工具窗口选中了配置
2. 检查`.idea/project-host.txt`文件是否更新
3. 确认运行配置是Spring Boot或Application类型
4. 查看应用启动日志确认VM选项

#### Q: 如何重置所有配置？
**A**: 删除配置文件：
```bash
rm ~/.IntelliJIdea/config/options/hostrunner-config.xml
```

#### Q: 配置会影响其他项目吗？
**A**: 配置是全局的，但只在当前项目启动时生效，不会影响其他项目。

#### Q: 支持哪些运行配置类型？
**A**: 支持Spring Boot Application和Java Application运行配置。

### 日志查看

**插件日志**：
```
Help > Show Log in Finder/Explorer
```

**查找关键词**：
- `[HostRunner]` - 插件相关日志
- `HostConfiguration` - 配置操作日志
- `VmOptionsInjector` - VM选项注入日志

## 🛡️ 安全说明

### 安全特性

- ✅ **输入验证** - 所有输入都经过严格验证
- ✅ **文件权限** - 只在项目目录内操作
- ✅ **沙箱环境** - 遵循IntelliJ安全规范
- ✅ **错误隔离** - 插件错误不影响IDE运行

### 权限要求

- 📁 项目`.idea`目录写入权限
- 📄 `project-host.txt`文件操作权限
- 🔧 IDE全局设置读写权限

## 🤝 贡献指南

### 开发环境

**要求**：
- JDK 17+
- IntelliJ IDEA 2024.3+
- Gradle 8.0+

**设置步骤**：
```bash
# 克隆项目
git clone https://github.com/yourname/HostRunner.git
cd HostRunner

# 导入到IDEA
# 选择 "Open" -> 选择项目目录

# 运行插件
./gradlew runIde

# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test
```

### 代码规范

- 遵循IntelliJ插件开发最佳实践
- 使用Java 17语法特性
- 保持代码简洁和可读性
- 添加必要的注释和文档

### 提交要求

- 清晰的提交信息
- 完整的测试覆盖
- 更新相关文档
- 遵循版本控制规范

## 📄 许可证

本项目采用 MIT License 许可证。

## 🌟 致谢

感谢以下开源项目和资源：
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [IntelliJ Community Edition](https://github.com/JetBrains/intellij-community)

## 📞 联系方式

**问题反馈**：
- 📮 GitHub Issues: [提交问题](https://github.com/yourname/HostRunner/issues)
- 📧 邮箱: your.email@example.com

**功能建议**：
- 💡 GitHub Discussions: [功能讨论](https://github.com/yourname/HostRunner/discussions)

---

**让开发环境配置变得更加简单！** 🎯

如果您觉得这个插件有用，请给个⭐️ Star支持一下！