# HostRunner - IntelliJ IDEA Hosts配置管理插件

HostRunner是一个IntelliJ IDEA插件，帮助开发者快速管理和切换不同的hosts配置。

## ✨ 主要功能

- **配置管理** - 在设置中管理多组hosts和VM选项配置
- **模板功能** - 提供开发、测试、生产环境预设模板
- **快速切换** - 通过工具窗口卡片快速选择配置
- **自动注入** - 启动应用时自动注入VM选项和更新hosts文件
- **智能适配** - 根据JDK版本自动选择正确的VM参数格式

## 🚀 快速使用

### 安装
1. 下载项目代码并构建插件
2. `Settings` > `Plugins` > `Install Plugin from Disk`
3. 选择构建好的插件文件
4. 重启IDEA

### 配置管理
1. 打开 `Settings` > `Tools` > `Host Configuration`
2. 点击 `Add from Template` 使用预设模板
3. 或点击 `Add` 手动创建配置
4. 填写配置名称、hosts内容、VM选项

### 快速切换
1. 在IDEA右侧工具窗口找到 "Host Configuration"
2. 点击选择配置卡片
3. 配置立即生效

### 启动应用
1. 创建Spring Boot或Application运行配置
2. 启动应用，插件自动注入VM选项
3. 应用使用新的hosts配置运行

## 📋 预设模板

- **开发环境** - 本地地址 + 开发调试参数
- **测试环境** - 测试服务器 + 测试配置
- **生产环境** - 生产调试 + 性能优化

## 🔧 配置参数

- **配置名称** - 配置的唯一标识
- **Hosts内容** - hosts文件内容
- **VM选项** - Java虚拟机参数
- **JDK版本** - JDK 9+ 或 JDK 9-

## 🎯 使用场景

- 开发环境切换不同API地址
- 测试环境配置测试服务器
- 生产环境调试时临时修改hosts
- 团队协作统一开发环境配置

让开发环境配置变得更加简单！ 🎯