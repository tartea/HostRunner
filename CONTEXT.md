# HostRunner

A JetBrains IDE plugin for managing and switching between host configurations — sets of `/etc/hosts` entries and JVM options that define how local development tools connect to remote environments.

## Language

### Configuration

**Host Configuration（配置）**:
A named set of hosts entries and VM options that the plugin applies to the system.
_Avoid_: Profile, preset, environment

**Group（分组）**:
An organizational label for configurations. A simple string on the configuration — not an independent entity. A configuration belongs to exactly one group. Groups exist only as the set of values derived from configurations; they have no independent lifecycle.
_Avoid_: Folder, category, tag

**Ungrouped（未分组）**:
The default group for configurations without an explicit group assignment. Represented by the string `"未分组"`. Always sorts first.
_Avoid_: Default, general, other

### Features

**Quick Pick（快速选择）**:
The floating dialog opened via `Ctrl+Shift+H` for rapid configuration selection. Uses tabs to switch between groups, each tab showing configuration cards for that group only. Selection does not close the dialog.

**Configuration Selection（配置选择）**:
The Tool Window tab for browsing and selecting configurations. Displays groups and configurations in a collapsible two-level tree.

**Configuration Management（配置管理）**:
The Tool Window tab for creating, editing, and deleting configurations. Displays the same two-level tree as Selection, with added create/edit/delete operations via toolbar buttons and right-click menus.

### Status

**Selected Configuration**:
Exactly one configuration can be active at a time. The plugin applies its hosts entries to the system hosts file. Tracked by `selectedConfigurationId` in persistent state. Status bar shows only the configuration name.
_Avoid_: Active, applied, current
