# Configuration Grouping — flat list becomes two-level hierarchy

Configuration lists were flat, which didn't scale. We introduced a **Group** concept: a simple string label on each configuration (`groupName`), not an independent entity. Groups are derived from the set of configurations that reference them — empty groups vanish, new names automatically create groups. Configurations belong to exactly one group. Configurations without a `groupName` default to `"未分组"`.

**Two display surfaces with different interaction models:**
- **Quick Pick dialog** switches groups via tabs (flat card list per tab, global search flattens results)
- **Tool Window** uses a two-level collapsible tree (groups as parent nodes, configurations as leaves; search filters the tree in-place)

The divergence is deliberate: the dialog is a transient rapid-selection surface where tabs are faster to scan; the Tool Window is a persistent workspace where the tree gives spatial context.

**Why a string, not an entity:**
Groups have no independent lifecycle — no creation, deletion, or renaming operations. Making them entities would add CRUD without adding value. Renaming is a batch edit of `groupName` across the group's configurations; deletion is removing the last configuration. This keeps the model minimal and avoids the ORM-style "manage the management" trap.

**Why tabs for the dialog, tree for the Tool Window:**
The dialog is space-constrained and optimized for rapid selection — tabs let users jump between groups in one click. The Tool Window has room for a tree and benefits from spatial memory (users learn where groups live). Both views share the same search behavior (global filter), the same data model, and the same selection mechanism.

**Why global search flattens results in the dialog:**
When a user searches, they've left the browse-by-group mental model and are looking for a specific configuration by name. Flattening removes the extra click of switching tabs to check each group.

Existing data (configurations with null/empty `groupName`) is migrated on first load: all are assigned to `"未分组"` and persisted.
