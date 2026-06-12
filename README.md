# 藏物志

`藏物志` 是一个基于 `Kotlin + Jetpack Compose + Material 3` 的 Android 本地离线应用，用于记录家庭物品的位置、价格、购入时间、使用频率、失效时间、分类和个人评分，并通过统计和排行发现高价格、低频率、低评分的低实用性物品。

## 当前已落地

- 物品新增、编辑、删除
- 手动录入名称、价格、购入日期、失效日期、使用频率、分类、房间、收纳位置、备注和评分
- 拍照留档与相册图片选择
- Apple 官网风格参考的极简界面：大留白、圆角输入框、半透明玻璃卡片、柔和阴影、渐变新增按钮
- 多种现代美学风格：深空玻璃、银白极简、流光渐变、HelloKitty、Notion
- 支持跟随系统、亮色、深色模式
- 空状态卡片点击 `+` 可直接添加物品
- 日历选择购入日期和失效日期
- 分类、房间、收纳位置支持轮盘选取，也支持手动添加
- 使用频率支持预设和自定义每月次数
- 本地 `SharedPreferences` JSON 持久化
- 物品搜索、弹窗式分类筛选、房间筛选
- 总资产、总日均成本和物品数量概览
- 分类资产统计：物品数、总资产、日均成本
- 实用性风险排行：价格高、使用频率低、评分低的物品更靠前
- 长按物品删除
- 批量选择、批量更改分类、批量删除
- 设置页自定义风格和系统模式
- 数据 JSON 导出
- 数据 JSON 文件备份与恢复
- MIT 开源协议

## 首版不包含

- 账号、云同步、多人协作
- 自动图像识别、语音录入、NFC、书籍扫码
- 通知提醒、应用商店上架、月供/订阅成本、数据导入恢复

## 项目结构

- `app/src/main/java/com/codex/focusflow/MainActivity.kt`
- `app/src/main/java/com/codex/focusflow/FocusFlowViewModel.kt`
- `app/src/main/java/com/codex/focusflow/ui/FocusFlowApp.kt`
- `app/src/main/java/com/codex/focusflow/ui/theme/Theme.kt`
- `app/src/main/res/xml/file_paths.xml`
