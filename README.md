# 基于SpringBoot的AI BOT对战小游戏

项目前后端分离，后端基于 SpringBoot 2.7.X 框架开发，项目由 Maven 管理，使用 MyBatis-Plus 对接并操作 MySQL 数据库，用户认证机制由 Spring Security 实现，并用 JWT 验证替代传统 Session 验证；前端基于 Vue 3 框架与 Bootstrap 开发，游戏界面使用 Canvas 绘制渲染，通过 Vuex 管理所有组件的状态。

## 1. 游戏介绍

 - 利用 Canvas 逐帧绘制渲染地图与玩家（两条蛇），由 JS 的 `requestAnimationFrame` 实现逐帧刷新的运动目标基类。
 - 地图大小为13×14，最外围一圈为障碍物，地图内部每局随机生成障碍物，满足中心对称规律。
 - 随机地图保证玩家出生地不为障碍物，两名玩家必定连通，且不会在同一回合走到同一个格子上。
 - 玩家若下一回合移动的目标位置为障碍物或某条蛇的身体部位则去世。
 - 目前实现两名玩家使用键盘操作移动，分别为 `W/S/A/D` 与 `↑/↓/←/→`。
 - 需要注册并登录后才能进入游戏页面。

## 2. 环境配置

项目前端预计实现 Web 端与 AcApp 端，分别在 `web` 与 `acapp` 目录下；后端位于 `backend` 目录下。

### 2.1 Web端环境配置

在 `web` 目录下安装相关依赖：

```shell
npm install
```

启动前端可以在 Vue UI 中操作：

```shell
vue ui
```

或者用 `npm` 启动：

```shell
npm run serve
```

### 2.2 后端环境配置

后端依赖使用 Maven 管理（Maven 仓库官网：[Maven Repository](https://mvnrepository.com/)），在 IDEA 的右侧 Maven 选项卡中重新加载所有 Maven 项目即可，使用到的依赖版本如下：

 - `Spring Boot Starter JDBC`：3.1.5
 - `Project Lombok`：1.18.30
 - `MySQL Connector/J`：8.2.0
 - `mybatis-plus-boot-starter`：3.5.4.1
 - `mybatis-plus-generator`：3.5.4.1
 - `spring-boot-starter-security`：3.1.5
 - `jjwt-api`：0.12.3
 - `jjwt-impl`：0.12.3
 - `jjwt-jackson`：0.12.3
 - `JetBrains Java Annotations`：24.0.1

数据库配置如下：

 - 数据库：`kob`
 - 用户名：`root`
 - 密码：`saki520`
