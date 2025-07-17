# 贪吃蛇游戏 - 完整开发文档

## 项目概述

这是一个基于Kotlin Multiplatform Compose开发的贪吃蛇游戏，支持Android和iOS平台。项目采用现代化的架构设计，包含详细的中文注释，非常适合学习Compose Multiplatform开发。

## 技术栈

- **Kotlin Multiplatform**: 2.2.0
- **Compose Multiplatform**: 1.8.2
- **架构模式**: MVVM + StateFlow
- **UI框架**: Jetpack Compose
- **协程**: Kotlin Coroutines
- **目标平台**: Android (API 24+), iOS

## 项目结构

```
composeApp/src/commonMain/kotlin/org/example/project/
├── App.kt                          # 应用入口
├── snake/
│   ├── model/                      # 数据模型层
│   │   ├── Position.kt            # 坐标数据类
│   │   ├── Direction.kt           # 方向枚举
│   │   ├── GameState.kt           # 游戏状态管理
│   │   ├── Snake.kt               # 蛇对象
│   │   └── Food.kt                # 食物对象
│   ├── engine/                     # 游戏引擎层
│   │   ├── SnakeGameEngine.kt     # 核心游戏逻辑
│   │   └── SnakeViewModel.kt      # 状态管理
│   ├── ui/                         # UI组件层
│   │   ├── GameCanvas.kt          # 游戏画布
│   │   ├── GameControls.kt        # 控制面板
│   │   └── SnakeGameScreen.kt     # 主界面
│   └── input/                      # 输入处理层
│       └── GestureHandler.kt      # 手势处理
```

## 核心功能

### 1. 游戏逻辑
- ✅ 蛇的移动和增长
- ✅ 食物随机生成
- ✅ 碰撞检测（墙壁、自身）
- ✅ 分数计算和等级系统
- ✅ 游戏状态管理（进行、暂停、结束）

### 2. 用户界面
- ✅ Canvas绘制游戏区域
- ✅ 实时分数和等级显示
- ✅ 方向控制按钮
- ✅ 游戏状态控制（暂停/继续/重置）
- ✅ 游戏说明和提示

### 3. 交互控制
- ✅ 方向按钮控制
- ✅ 防抖动输入处理
- ✅ 游戏状态切换

## 游戏规则

1. **基本玩法**: 控制蛇移动吃食物，避免撞墙和撞到自己
2. **食物类型**:
  - 🍎 普通食物(红色): +10分
  - 🍊 奖励食物(橙色): +25分
  - ⭐ 超级食物(黄色): +50分
3. **等级系统**: 每100分升一级，游戏速度逐渐加快
4. **游戏结束**: 撞墙或撞到自己时游戏结束

## 架构设计

### MVVM架构
- **Model**: 数据模型（Position, Snake, Food等）
- **View**: UI组件（GameCanvas, GameControls等）
- **ViewModel**: 状态管理（SnakeViewModel）

### 状态管理
- 使用StateFlow进行响应式状态管理
- 游戏状态通过密封类确保类型安全
- 协程处理游戏循环和异步操作

### 渲染系统
- Canvas绘制游戏区域，性能优秀
- Compose组件处理UI控制，开发效率高
- 混合架构平衡性能和可维护性

## 学习要点

### 1. Kotlin Multiplatform
- 共享业务逻辑代码
- 平台特定实现分离
- 跨平台依赖管理

### 2. Compose UI
- 声明式UI开发
- 状态管理和重组
- Canvas自定义绘制

### 3. 架构模式
- MVVM架构实践
- StateFlow响应式编程
- 关注点分离原则

### 4. 游戏开发
- 游戏循环设计
- 碰撞检测算法
- 状态机模式应用

## 运行说明

### Android
1. 打开Android Studio
2. 运行 `./gradlew composeApp:installDebug`
3. 在设备上启动应用

### iOS (需要macOS环境)
1. 打开Xcode
2. 运行iOS项目
3. 在模拟器或设备上测试

## 扩展建议

1. **功能扩展**:
  - 添加音效和背景音乐
  - 实现最高分记录
  - 添加不同难度模式
  - 支持多人游戏

2. **技术优化**:
  - 添加单元测试
  - 实现数据持久化
  - 优化渲染性能
  - 添加动画效果

3. **用户体验**:
  - 支持手势控制
  - 添加主题切换
  - 实现设置界面
  - 优化触摸反馈

## 总结

这个贪吃蛇游戏项目展示了Kotlin Multiplatform Compose的强大能力，通过清晰的架构设计和详细的注释，为学习者提供了一个完整的跨平台游戏开发示例。项目代码质量高，结构清晰，非常适合作为学习和参考的材料。

## LICENSE（开源许可证）

MIT License

Copyright (c) 2025 qicun

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
