```text
   __   _       __        ____             _
  / /  (_)_ _  / /  ___  / __/__ _____  __(_)______
 / /__/ /  ' \/ _ \/ _ \_\ \/ -_) __/ |/ / / __/ -_)
/____/_/_/_/_/_.__/\___/___/\__/_/  |___/_/\__/\__/
```
README LANGUAGES [ [English](README.md) | [**中文**](README_CN.md)  ]
# LimboService

<img src=".doc/images/map.png" width=200px align="right" alt="Map image">

[![Version](https://img.shields.io/github/v/release/YourCraftMC/LimboService?style=flat-square)](https://github.com/YourCraftMC/LimboService/releases)
[![License](https://img.shields.io/github/license/YourCraftMC/LimboService?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://img.shields.io/github/actions/workflow/status/YourCraftMC/LimboService/maven.yml?style=flat-square)](https://github.com/YourCraftMC/LimboService/actions/workflows/maven.yml)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/YourCraftMC/LimboService/total?style=flat-square)
![Java version](https://img.shields.io/badge/Java-17-red?logo=openjdk&style=flat-square)

🌆 专为 Minecraft 打造的轻量级独立 LIMBO 服务，基于 [**MCProtocolLib**](https://github.com/GeyserMC/MCProtocolLib)。

> [!NOTE]
> 本项目基于 [Limbo](https://github.com/LOOHP/Limbo) 项目开发，
> 后续将由 [YourCraftMC](https://github.com/YourCraftMC) 团队进行重大改动与维护。

## 使用指南

请从[此处](https://github.com/YourCraftMC/LimboService/releases/latest)下载最新服务端 Jar 文件，
或通过[发布页](https://github.com/YourCraftMC/LimboService/releases)获取历史版本。

1. 将下载的服务器 Jar 文件放置于目标运行目录。
    - 可将世界模板文件放置于服务器世界目录（与 Jar 文件同级）。
2. 使用如下命令启动服务器（与常规 Minecraft 服务端启动方式一致）：
   ```shell
   java -Xms64M -Xmx512M -jar LimboService-<VERSION>-<MINECRAFT>.jar
   ```
3. 在 config.yml 中配置服务器参数，在 messages.yml 中自定义消息。
4. 通过您配置的服务器地址加入游戏，体验 LIMBO 世界！

其他开发者可通过 LimboService 提供的 API 开发第三方插件，
您可将编译后的插件置于 `plugins/` 目录下，
插件将在LimboService启动时加载。

如您也想开发 LimboService 的第三方插件，请移步 [开发指南](#开发指南) 。

> [!CAUTION]
> LimboService 未实现也绝不计划实现任何 Bukkit/Spigot/BungeeCord/Velocity 的 API，
> 且**完全无法兼容**这些平台的插件！
>
> **永远不要提交任何关于与其他平台插件兼容性问题的工单！**

### 内置命令

```text
# version
@ limbo.command.version
- 显示服务器版本信息

# spawn [player]
@ limbo.command.spawn (自用权限)
@ limbo.command.spawn.others
- 将玩家传送至出生点

# say <message>
@ limbo.command.say
- 向全体玩家广播消息

# gamemode <mode> [player]
@ limbo.command.gamemode (自用权限)
@ limbo.command.gamemode.others
- 切换玩家游戏模式

# allowlist toggle
@ limbo.command.allowlist
- 切换名单模式（允许名单/拒绝名单）

# allowlist <add|remove> <player>
@ limbo.command.allowlist
- 添加/移除名单中的玩家

# allowlist reload
@ limbo.command.allowlist
- 重载名单配置

# kick <player> [reason]
@ limbo.command.kick
- 踢出指定玩家

# stop
@ limbo.command.stop
- 关闭服务器
```

## 统计数据

> LimboService 使用 [bStats](https://bstats.org/) 进行匿名数据统计。

![bStats](https://bstats.org/signatures/server-implementation/LimboService.svg)

## 开发指南

LimboService 可作为开发库使用，其插件开发模式与 Bukkit/Spigot 类似。

以下为插件开发的最小化示例：

```java
package com.example.myplugin;

import com.loohp.limbo.plugins.LimboPlugin;

public class MyPlugin extends LimboPlugin {
    @Override
    public void onLoad() {
        // 插件加载时触发
    }

    @Override
    public void onEnable() {
        // 插件启用时触发
    }

    @Override
    public void onDisable() {
        // 插件停用时触发
    }
}
```

同时，您需在 `src/resources/` 目录下创建 plugin.yml 或 limbo.yml 文件：

```yaml
main: com.example.myplugin.MyPlugin
name: MyPlugin
version: 1.0
author: MyCoolName
description: LimboService 基础插件模板
```

至此，您已可使用 LimboService API 进行插件开发！

> [!NOTE]
> 您可从 [GitHub Packages](https://github.com/orgs/YourCraftMC/packages?repo_name=LimboService)
> 查看、获取Maven的依赖包，还可以在 [这里](https://yourcraftmc.github.io/LimboService/) 找到本项目的 Javadoc。
>
> 使用时记得将下方配置中的 VERSION 替换为实际版本号。

<details> <summary><b>Maven 依赖配置</b></summary>

```xml
<project>
    <repositories>
        <repository>
            <!-- 推荐使用 Maven 中央仓库获取稳定版本（需注意同步延迟） -->
            <id>maven</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
        </repository>

        <repository>
            <!-- 使用 GitHub 仓库获取实时更新（需配置认证） -->
            <id>limboservice-repo</id>
            <name>GitHub Packages for LimboService</name>
            <url>https://maven.pkg.github.com/YourCraftMC/LimboService</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>cn.ycraft</groupId>
            <artifactId>limboservice</artifactId>
            <version>[VERSION]</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

</details>

<details> <summary><b>Gradle 依赖配置</b></summary>

```groovy
repositories {
    // 推荐使用 Maven 中央仓库获取稳定版本（需注意同步延迟）
    mavenCentral()

    // 使用 GitHub 仓库获取实时更新（需配置认证）
    maven { url 'https://maven.pkg.github.com/YourCraftMC/LimboService' }
}

dependencies {
    api "cn.ycraft:limboservice:[LATEST RELEASE]"
}
```
</details>

## 第三方开源库依赖
- [**MCProtocolLib**](https://github.com/GeyserMC/MCProtocolLib): 用于实现基本的 MineCraft 游戏功能。
- [**adventure**](https://github.com/KyoriPowered/adventure): 消息组件功能支持。
- [**configured**](https://github.com/CarmJos/EasyConfiguration/pull/101): 用于实现配置文件相关功能。
- [**LiteCommands**](https://github.com/Rollczi/LiteCommands): 用于实现现有的指令系统。
- [**EasyPlugin-Color**](https://github.com/CarmJos/EasyPlugin): 通用颜色代码解析支持。

完整依赖列表请参见 [依赖关系图](https://github.com/YourCraftMC/LimboService/network/dependencies)。

## 致谢与支持
特别感谢 [LoohpJames(@LOOHP)](https://github.com/LOOHP)
与 [其他贡献者](https://github.com/LOOHP/Limbo/graphs/contributors) 对原项目的卓越贡献。

衷心感谢 JetBrains 为本项目及其他开源项目提供开发工具授权。

[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/YourCraftMC/LimboService)

本项目目前由 [YourCraftMC(你的世界)](https://www.ycraft.cn) 团队主导维护。

<img src="https://raw.githubusercontent.com/YourCraftMC/.github/refs/heads/main/imgs/text_1440p.png" alt="Team logo" width="400px">

## 开源协议
本项目源代码遵循 [GNU 宽通用公共许可证(LGPL) 3.0](https://www.gnu.org/licenses/lgpl-3.0.html) 发布。
