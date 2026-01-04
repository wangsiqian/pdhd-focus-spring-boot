_这份文档是基于我们经过多轮深度迭代、重构后的最终代码逻辑整理而成的。它完全替代了最初的 PRD，反映了当前系统的真实形态。

---

# PDHD Focus - 个人精力与日程管理系统 (v2.0)

**文档版本**：2.0 (Post-Dev)
**更新日期**：2026-01-04
**核心理念**：顺应多动/注意力缺失（ADHD）大脑，通过“计划 vs 实际”的强对比与“当下 vs 下一步”的强引导，实现精力管理而非单纯的时间管理。

---

## 1. 产品背景与目标

### 1.1 背景

传统日历（Google Calendar/Outlook）只记录“计划”，无法真实反映“时间去哪了”。对于 PDHD 人群，最大的痛点在于：

1.  **执行偏差**：计划 9 点工作，实际 10 点才开始。
2.  **记录困难**：事后补录非常痛苦，需要极低成本的记录方式。
3.  **迷失当下**：一旦被打断，很难回忆起刚才在做什么，接下来该做什么。

### 1.2 产品目标

打造一个**双列时间轴**（Double-Column Timeline）管理工具：

- **左列（Plan）**：我打算怎么过这一天。
- **右列（Actual）**：这一天实际是怎么过的。
- **强关联**：完成计划自动生成实际记录，直观展示偏差。

---

## 2. 核心功能架构

### 2.1 仪表盘 (Dashboard) - 指挥中心

这是用户停留时间最长的页面，采用 **Sidebar + Main Content** 布局（无顶部 Header，最大化纵向视野）。

#### A. 核心组件：Now & Next（当下与未来）

置于页面最顶端，时刻提醒用户：

- **Now (当前专注)**：
    - **展示逻辑**：优先级为 `正在进行的实际事项` > `正在进行的计划事项`。
    - **UI**：深色背景卡片，带呼吸灯动画。
    - **信息**：标题 + 时间段 + 剩余时间倒计时（如“15 分钟后结束”）。
- **Next (后续安排)**：
    - **展示逻辑**：当前时间之后的第一个计划。
    - **中断恢复**：如果当前正在做“杂事”（实际事项），但时间处于某个“计划”范围内，Next 会显示“**恢复：[原计划标题]**”。
    - **UI**：浅色背景，展示开始时间倒计时（如“2 小时后开始”）。

#### B. 控制栏 (Toolbar)

- **日期导航**：`DateNavigator` 组件，支持 `<` `>` 切换，点击日期弹出 Shadcn Calendar 选择器。
- **视图切换**：日视图 / 周视图 / 月视图。
- **自动定位开关**：Toggle Switch。开启后，进入页面或每隔一分钟，时间轴自动滚动到当前时间红线位置（视线居中偏上）。
- **新建按钮**：触发新建模态框。

#### C. 主视图 (Main View) - 支持三种模式

**1. 日视图 (Day View - 核心)**

- **双列布局**：左侧 `计划 (PLAN)`，右侧 `实际 (ACTUAL)`。
- **全天刻度**：00:00 - 24:00，纵向滚动。
- **当前时间线**：红色横线贯穿左右，`Z-Index` 经过调优，位于网格之上、表头之下。
- **交互创新**：
    - **拖拽创建 (Drag-to-Create)**：在空白处按下鼠标拖拽，生成半透明蓝色“影子选区”，松开后自动弹出新建框，时间自动填入。
    - **点击编辑**：点击卡片弹出编辑框。
    - **悬停吸顶 (Sticky Title)**：当任务卡片很长且超出屏幕时，标题自动吸附在卡片顶部，防止滚动后不知道这是什么任务。

**2. 周视图 (Week View)**

- **网格布局**：7 列（周一至周日），仅展示计划。
- **样式**：白底灰线，当前日期高亮显示。
- **交互**：点击空白处新建当天计划，点击卡片编辑。

**3. 月视图 (Month View)**

- **类 Notion 风格**：大日历网格。
- **交互**：点击日期格子跳转到该日的日视图。

---

### 2.2 数据实体与逻辑规则 (重点)

#### A. 事项类型

1.  **Schedule (计划)**：未来的安排。支持重复规则。
2.  **Activity (实际)**：过去发生的事实。**不可重复**。

#### B. 核心业务逻辑：计划与实际的联动

这是本系统的灵魂功能，确保数据一致性：

1.  **打钩 (Check) 逻辑**：

    - 在计划卡片上点击“圆圈”，状态变为“已完成（绿色钩）”。
    - **自动克隆**：系统立即在右侧“实际”列生成一条同名、同时间、同内容的 Activity。
    - **绑定关系**：该 Activity 带有 `scheduleId` 标记，指向原计划。

2.  **反悔 (Uncheck) 逻辑**：

    - 取消勾选计划，系统根据 `scheduleId` 自动删除右侧对应的实际记录。

3.  **时间切片 (Time Slicing) - 冲突处理**：

    - **场景**：计划 10:00-12:00 写代码。但在 11:00-11:20 插入了一条“实际事项：摸鱼刷手机”。
    - **处理**：当勾选“写代码”计划完成时，系统会自动计算空闲时间槽。
    - **结果**：生成两条实际记录：`10:00-11:00 写代码` 和 `11:20-12:00 写代码`。中间被“摸鱼”占用的时间自动跳过。

4.  **单向同步与只读**：
    - 由计划自动生成的实际事项，**禁止直接编辑/删除**。
    - UI 上会显示“锁定”图标。
    - **原因**：如果要改时间，请修改左侧的“计划”，右侧会自动同步。只有纯手动创建的突发实际事项（如“临时会议”）才允许自由编辑。

#### C. 重复规则

- **类型**：不重复、每天、工作日（周一至周五）、**自定义（周几）**。
- **生成机制**：创建时一次性生成未来 30 天的数据（MVP 方案），确保日历视图有真实数据填充。
- **级联删除**：删除重复任务时，询问“仅删除当前”还是“删除当前及后续所有”。

---

## 3. UI/UX 设计规范 (最新版)

### 3.1 布局 (Layout)

- **去头 (No Header)**：移除了传统的顶部导航栏，所有导航和用户信息收纳在左侧 Sidebar。
- **Sidebar**：
    - 顶部：Logo (PDHD Focus)。
    - 中部：导航菜单 (Dashboard, Goals, Analytics)。
    - 底部：移动端模式入口、用户头像菜单（含退出）。
- **Content**：背景色 `bg-background` (白色)，卡片使用 `border` 和 `shadow-sm` 区分。

### 3.2 弹窗交互 (Modal)

- **Grid 系统**：表单采用双列 Grid 布局，避免字段过长或重叠。
- **Header**：标题左对齐，右侧紧跟“完成状态”切换按钮（Check/Circle）。如果是自动生成的记录，显示“锁定”徽章。
- **时间选择器**：自定义组合组件。
    - 左侧：Button 触发 Shadcn Calendar Popover（支持年月快速切换）。
    - 右侧：Input (type="time")，隐藏原生图标，纯文本输入，高度对齐。
- **富文本**：目前使用 `Textarea` 占位，支持多行输入。
- **标签 (Zone)**：
    - 😌 **舒适区** (Comfort) - 绿色
    - 🔥 **拉伸区** (Stretch) - 橙色
    - 💀 **困难区** (Difficulty) - 红色

### 3.3 视觉细节

- **Z-Index 管理**：
    - `Grid Lines`: 0 (最底层)
    - `Col Background`: 5
    - `Event Card`: 10
    - `Ghost Selection` (拖拽蓝框): 20
    - `Current Time Line` (红线): 30
    - `Sticky Header` (表头): 40
- **颜色系统**：
    - 目标关联：通过左侧细条 (`border-l-4` 或 `absolute bar`) 展示 Goal 的颜色 (Emerald/Blue/Violet)。
    - 完成状态：已完成的任务文字变灰并增加删除线。

---

## 4. 技术栈选型 (Tech Stack)

基于“轻量、现代、高性能”原则，我们采用了以下技术组合：

### 4.1 前端核心

- **构建工具**: [Vite](https://vitejs.dev/) (极速热更新，构建效率高)
- **框架**: [React 18](https://react.dev/) (函数式组件 + Hooks)
- **语言**: JavaScript (ES6+)
- **路由**: `react-router-dom` v6 (SPA 单页应用路由)

### 4.2 UI 与 样式

- **UI 库**: [Shadcn UI](https://ui.shadcn.com/)
    - _特点_：非黑盒组件，直接拷贝源码到项目中，拥有完全的控制权和定制能力。
    - _使用组件_：Button, Card, Dialog, Input, Select, Popover, Calendar, Tabs, Switch, Badge, Label.
- **样式引擎**: [Tailwind CSS](https://tailwindcss.com/)
    - _工具_：`clsx` + `tailwind-merge` (用于处理动态类名合并，解决样式冲突)。
- **图标库**: [Lucide React](https://lucide.dev/) (风格统一、轻量的 SVG 图标)。

### 4.3 状态管理与数据

- **状态管理**: [Zustand](https://github.com/pmndrs/zustand)
    - _原因_：比 Redux 更轻量，无样板代码，且原生支持 Hooks。
- **持久化**: `zustand/middleware/persist`
    - _机制_：自动同步 Store 到浏览器 `LocalStorage`。
    - _当前策略_：全量数据本地存储，模拟数据库行为。
- **时间处理**: [Day.js](https://day.js.org/)
    - _核心插件_：
        - `isSameOrAfter`: 用于时间段比较。
        - `isBetween`: 用于判断“当前专注”和“时间切片”。
        - `weekOfYear` / `isoWeek`: 用于周视图计算。
        - `localizedFormat`: 本地化格式支持。

---

## 5. 核心数据模型 (Schema Design)

虽然目前使用 LocalStorage，但我们保持了关系型数据库的设计思维，以便未来迁移至 MySQL。

### 5.1 Schedule (计划表)

_定义：未来的、预期的安排。_

```json
{
  "id": "number (timestamp)",
  "title": "string",
  "content": "string (Rich Text/Memo)",
  "type": "enum ('WORK', 'INVEST', 'STUDY', 'LIFE')",
  "zone": "enum ('COMFORT', 'STRETCH', 'DIFFICULTY')", // 难度分区
  "goalId": "number (FK -> Goal.id)",
  "startTime": "string (ISO 8601)",
  "endTime": "string (ISO 8601)",
  "status": "0 | 1 (0:未完成, 1:已完成)",
  "repeatRule": "enum ('NONE', 'DAILY', 'WEEKDAY', 'CUSTOM')",
  "customDays": "array [0-6]", // 仅当 repeatRule='CUSTOM' 时有效
  "groupId": "string (UUID)" // 用于标识同一组重复任务
}
```

### 5.2 Activity (实际表)

_定义：过去发生的、真实的时间消耗。_

```json
{
  "id": "number",
  "scheduleId": "number (FK -> Schedule.id, nullable)", // 关键字段：关联来源计划
  "title": "string",
  "content": "string",
  "type": "string",
  "zone": "string",
  "goalId": "number",
  "startTime": "string",
  "endTime": "string"
}
```

### 5.3 Goal (目标表)

```json
{
  "id": "number",
  "title": "string",
  "color": "string (Tailwind Class, e.g., 'bg-blue-500')",
  "status": "1",
  "progress": "number (0-100)"
}
```

---

## 6. 核心算法与业务逻辑

### 6.1 时间切片算法 (Time Slicing)

**解决痛点**：计划 10:00-12:00 工作，但中间 11:00-11:20 插入了“临时会议”。当勾选“工作”完成时，不能简单覆盖，而应智能分割。

**逻辑流程**：

1.  **输入**：计划时间段 `[PlanStart, PlanEnd]`。
2.  **查询**：查找该时间段内所有**已存在**的 Actual Activity（如“临时会议”）。
3.  **计算**：
    - 初始片段 = `[PlanStart, PlanEnd]`。
    - 遍历冲突事件，对初始片段进行“扣除”操作。
    - _Case A_：冲突在中间 -> 分裂为 `[Start, ConflictStart]` 和 `[ConflictEnd, End]`。
    - _Case B_：冲突在头部/尾部 -> 截断片段。
4.  **输出**：生成 1 个或多个不连续的 Activity 记录，写入 Store。

### 6.2 布局计算算法 (Event Layout)

**解决痛点**：日视图中，多个任务时间重叠时，如何优雅展示而不遮挡。

**逻辑流程 (computeLayout)**：

1.  **排序**：按 `startTime` 升序排列所有事件。
2.  **分组 (Columns)**：
    - 遍历事件，尝试放入第 `i` 列。
    - 条件：如果 `Event.startTime >= Column[i].lastEvent.endTime`，则放入该列。
    - 否则：放入第 `i+1` 列。
3.  **坐标计算**：
    - `width = 100% / 总列数`
    - `left = 列索引 * width`
4.  **渲染**：前端使用 absolute positioning (`top`, `height`, `left`, `width`) 进行绘制。

### 6.3 拖拽创建算法 (Drag-to-Create)

**解决痛点**：鼠标在时间轴上滑动选择时间段。

**逻辑流程**：

1.  **基准线**：获取容器的 `BoundingClientRect`。
2.  **坐标修正**：`offsetY = e.clientY - rect.top - HEADER_HEIGHT (36px)`。
    - _注意_：必须减去表头高度，否则时间计算会整体偏移。
3.  **时间映射**：
    - `Minutes = offsetY / PIXELS_PER_MIN`。
    - **吸附 (Snap)**：`Math.floor(Minutes / 15) * 15`，强制对齐到 15 分钟刻度。
4.  **视觉反馈**：渲染一个 `z-index: 20` 的半透明 Ghost Block 跟随鼠标。

### 6.4 Now & Next 优先级逻辑

**展示规则**：

1.  **Now**：
    - 优先展示 `Activity`（如果当前时间处于某个实际事项的 `start-end` 区间）。
    - 其次展示 `Schedule`（如果当前没实际事项，但在计划时间内）。
    - 否则显示“空闲”。
2.  **Next**：
    - 检测 **中断恢复**：如果当前正在做 Activity A，但也处于 Schedule B 的时间内，Next 展示 **“恢复：Schedule B”**。
    - 否则：展示 `startTime > Now` 的第一个计划。

---

## 7. UI 架构与图层管理

为了保证交互的流畅和视觉的层级感，我们严格定义了 **Z-Index System**：

| 层级   | Z-Index         | 组件/元素         | 说明                                        |
| :----- | :-------------- | :---------------- | :------------------------------------------ |
| **L0** | `z-0`           | **Grid Lines**    | 背景网格线，最底层，不响应鼠标。            |
| **L1** | `z-5`           | **Column Bg**     | 列背景色。                                  |
| **L2** | `z-10`          | **Event Card**    | 任务卡片，可点击。                          |
| **L3** | `z-20`          | **Ghost Block**   | 拖拽时的蓝色半透明选区。                    |
| **L4** | `z-20`          | **Red Line**      | 当前时间红线 (穿过任务，但在表头下)。       |
| **L5** | `z-30` / `z-40` | **Sticky Header** | “计划/实际”表头，必须遮挡滚动的红线和任务。 |
| **L6** | `z-50`          | **Modal/Popover** | 弹窗、下拉菜单。                            |

---

## 8. 组件树结构

```text
src/
├── components/
│   ├── ui/               # Shadcn 基础组件 (Button, Card, etc.)
│   ├── modals/
│   │   └── ScheduleModal # 核心弹窗 (含 Form, DateTimePicker)
│   ├── views/
│   │   └── MonthView     # 月视图
│   └── DateNavigator     # 日期切换器
├── layout/
│   └── MainLayout        # Sidebar + Outlet
├── pages/
│   └── Dashboard
│       ├── NowNextWidget # 顶部看板
│       ├── DayView       # 核心日视图 (含拖拽、布局算法)
│       └── WeekView      # 周视图
├── store.js              # Zustand Store (所有业务逻辑)
└── lib/
    ├── utils.js          # Tailwind Merge
    └── mock.js           # 初始数据
```

---

## 9. 后续后端对接规划 (Migration Path)

目前系统数据存储在 LocalStorage (`pdhd-storage-v10`)。若要迁移到 Spring Boot 后端，改动极小：

1.  **API 层**：新建 `src/api` 目录，封装 Axios 请求。
2.  **Store 改造**：
    - 移除 `persist` 中间件。
    - 将 `addSchedule` 等 Action 改为 `async` 函数。
    - 流程：UI -> Action -> API Call -> DB -> Update Local State。
3.  **ID 生成**：
    - 目前使用 `Date.now()` 生成 ID。
    - 对接后改用后端返回的数据库 ID (Long/UUID)。
4.  **Repeat 逻辑**：
    - 目前前端循环生成 30 天数据。
    - 对接后可下沉至后端，或者前端只传 `rule`，由后端定时任务生成每日实例。_
