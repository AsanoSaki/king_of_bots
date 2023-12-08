# 基于SpringBoot的BOT贪吃蛇对战小游戏

项目前后端分离，后端基于 SpringBoot 3.2.0 框架开发，项目由 Maven 管理，使用 MyBatis-Plus 对接并操作 MySQL 数据库，通过 Spring Security 实现用户授权认证，并用 JWT 验证替代传统 Session 验证，通过 WebSocket 与多线程（防止每回合等待玩家的 I/O 操作导致整个程序阻塞）实现联机对战功能，匹配系统微服务由 Spring Cloud 构建，匹配系统后端与 Web 后端利用 WebClient 进行通信；前端基于 Vue 3 框架与 Bootstrap 开发，通过 Vuex 管理所有组件的状态，游戏界面使用 Canvas 绘制渲染，集成了 Vue Ace Editor 代码编辑器供用户编写自己的 Bot 代码。

## 1. 游戏介绍

 - 利用 Canvas 逐帧绘制渲染地图与玩家（两条蛇），由 JS 的 `requestAnimationFrame` 实现逐帧刷新的运动目标基类。
 - 地图大小为13×14，最外围一圈为障碍物，地图内部每局随机生成障碍物，满足中心对称规律。
 - 随机地图保证玩家出生地不为障碍物，两名玩家必定连通，且不会在同一回合走到同一个格子上。
 - 玩家若下一回合移动的目标位置为障碍物或某条蛇的身体部位则去世。
 - 目前实现两名玩家的匹配联机对战，使用键盘 `W/S/A/D` 操作移动，每回合超过5秒不输入判定为自杀。
 - 每名玩家初始天梯分为1500，匹配时优先匹配分值接近的玩家，若匹配时间较长则会匹配分值差距较大的玩家。
 - 用户可以在 My Bots 页面查看并管理自己的 Bot（包括名称、创建时间、简介、代码等信息），可以创建、修改或删除 Bot。
 - 需要注册并登录后才能访问各个页面。

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

### 2.2 后端依赖配置

后端依赖使用 Maven 管理（Maven 仓库官网：[Maven Repository](https://mvnrepository.com/)），在 IDEA 的右侧 Maven 选项卡中重新加载所有 Maven 项目即可。

#### 2.2.1 Spring Cloud项目：`backendcloud`

无主入口，拥有2个子项目：`backend`、`matchingsystem`。

Maven 依赖：

 - `Spring Boot`（初始化项目时设置）：3.2.0
 - `JDK`（初始化项目时设置）：Corretto-17.0.9
 - `Spring Cloud Dependencies`：2022.0.4

#### 2.2.2 Web后端项目：`backend`

主入口为 `backend/src/main/java/com.kob.backend` 下的 `BackendApplication`。

Maven 依赖：

 - `spring-boot-starter-jdbc`：3.2.0
 - `lombok`：1.18.30
 - `mysql-connector-j`：8.2.0
 - `mybatis-plus-boot-starter`：3.5.4.1（其中包含的 `mybatis-spring` 版本太低，需要用 `<exclusion>` 排除，且自行添加 `mybatis-spring` 3.0.3 版本的依赖）
 - `mybatis-plus-generator`：3.5.4.1
 - `spring-boot-starter-security`：3.2.0
 - `jjwt-api`：0.12.3
 - `jjwt-impl`：0.12.3
 - `jjwt-jackson`：0.12.3
 - `annotations`：24.1.0
 - `spring-boot-starter-websocket`：3.2.0
 - `fastjson2`：2.0.42
 - `jakarta.websocket-api`：2.2.0-M1
 - `jakarta.websocket-client-api`：2.2.0-M1
 - `spring-boot-starter-webflux`：3.2.0

#### 2.2.3 匹配系统微服务：`matchingsystem`

主入口为 `matchingsystem/src/main/java/com.kob.matchingsystem` 下的 `MatchingSystemApplication`。

Maven 依赖：

 - `spring-boot-starter-security`：3.2.0
 - `lombok`：1.18.30
 - `spring-boot-starter-webflux`：3.2.0

### 2.3 数据库配置

数据库使用 MySQL 8.0.35，总体配置如下：

 - 数据库：`kob`
 - 用户名：`root`
 - 密码：`saki520`

（1）`user` 表详细信息：

 - `id: int`（非空、自增、主键）
 - `username: varchar(100)`（非空）
 - `password: varchar(100)`（非空）
 - `photo: varchar(1000)`
 - `rating: int`（默认值为1500）

创建 `user` 表的 SQL 语句如下：

```sql
CREATE TABLE `kob`.`user` (
    `id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(100) NOT NULL,
    `password` varchar(100) NOT NULL,
    `photo` varchar(1000) NULL,
    `rating` int NULL DEFAULT 1500,
    PRIMARY KEY (`id`)
);
```

（2）`bot` 表详细信息：

 - `id: int`（非空、自增、主键）
 - `user_id: int`（非空，注意：在 `pojo` 中需要定义成 `userId`，在 `queryWrapper` 中的名称仍然为 `user_id`）
 - `title: varchar(100)`
 - `description: varchar(300)`
 - `content：varchar(10000)`
 - `createtime: datetime`（注意：在 `pojo` 中定义日期格式的注解为 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`）
 - `modifytime: datetime`

创建 `bot` 表的 SQL 语句如下：

```sql
CREATE TABLE `kob`.`bot` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `title` varchar(100) NULL,
    `description` varchar(300) NULL,
    `content` varchar(10000) NULL,
    `createtime` datetime NULL,
    `modifytime` datetime NULL,
    PRIMARY KEY (`id`)
);
```

（3）`record` 表详细信息：

 - `id: int`（非空、自增、主键）
 - `a_id: int`
 - `a_sx: int`
 - `a_sy: int`
 - `b_id: int`
 - `b_sx: int`
 - `b_sy: int`
 - `a_steps: varchar(1000)`
 - `b_steps: varchar(1000)`
 - `map: varchar(1000)`
 - `loser: varchar(10)`
 - `createtime: datetime`

创建 `record` 表的 SQL 语句如下：

```sql
CREATE TABLE `kob`.`record` (
    `id` int NOT NULL AUTO_INCREMENT,
    `a_id` int NULL,
    `a_sx` int NULL,
    `a_sy` int NULL,
    `b_id` int NULL,
    `b_sx` int NULL,
    `b_sy` int NULL,
    `a_steps` varchar(1000) NULL,
    `b_steps` varchar(1000) NULL,
    `map` varchar(1000) NULL,
    `loser` varchar(10) NULL,
    `createtime` datetime NULL,
    PRIMARY KEY (`id`)
);
```
