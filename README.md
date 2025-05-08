```text
   __   _       __        ____             _
  / /  (_)_ _  / /  ___  / __/__ _____  __(_)______
 / /__/ /  ' \/ _ \/ _ \_\ \/ -_) __/ |/ / / __/ -_)
/____/_/_/_/_/_.__/\___/___/\__/_/  |___/_/\__/\__/
```
README LANGUAGES [ [**English**](README.md) | [中文](README_CN.md)  ]

# LimboService

<img src=".doc/images/map.png" width=200px align="right" alt="Map image">

[![Version](https://img.shields.io/github/v/release/YourCraftMC/LimboService?style=flat-square)](https://github.com/YourCraftMC/LimboService/releases)
[![License](https://img.shields.io/github/license/YourCraftMC/LimboService?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://img.shields.io/github/actions/workflow/status/YourCraftMC/LimboService/maven.yml?style=flat-square)](https://github.com/YourCraftMC/LimboService/actions/workflows/maven.yml)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/YourCraftMC/LimboService/total?style=flat-square)
![Java version](https://img.shields.io/badge/Java-17-red?logo=openjdk&style=flat-square)

🌆 A lightweight & standalone LIMBO service for Minecraft, based on [**MCProtocolLib**](https://github.com/GeyserMC/MCProtocolLib).

> [!NOTE]
> This project is forked from [LOOHP's Limbo](https://github.com/LOOHP/Limbo),
> but will be breaking changed and maintained by [YourCraftMC](https://github.com/YourCraftMC).

## Usage

Download latest server jars [here](https://github.com/YourCraftMC/LimboService/releases/latest)
or other versions from [Releases](https://github.com/YourCraftMC/LimboService/releases).

1. Put downloaded server jar file at a directory where you want to run the limbo server.
    - Also you can put the world scheme file to the same directory as limbo's world.
2. Use the following command lines to start the limbo server just like any other Minecraft server jars
   ```shell
   java -Xms64M -Xmx512M -jar LimboService-<VERSION>-<MINECRAFT>.jar
   ```
3. Configure settings in `config.yml` and messages in `messages.yml`.
    - Also see `allowlist.yml` for allowing/disallowing players to join.
4. Join the server with configured host and enjoy the limbo!

LimboService also provides API for developers to create plugins for it,
if you have any LimboService plugins, you can put them in the `plugins/` directory.
Or if you want to develop your own plugins, please see the [Development](#development) section.

> [!CAUTION]
> LimboService is **not implemented** any Bukkit/Spigot/BungeeCord/Velocity API,
> and it's **completely not compatible** and will **never compatible** with any plugins of them!
>
> You need to use the provided API to develop your own plugins!
>
> **NEVER CREATE ANY ISSUES ABOUT COMPATIBILITY WITH BUKKIT/SPIGOT/VELOCITY!**

### Built-in commands

```text
# version
@ limbo.command.version
- Show the version of the server.

# spawn [player]
@ limbo.command.spawn (for self)
@ limbo.command.spawn.others
- Teleport a player to the spawn point.

# say <message>
@ limbo.command.say
- Broadcast a message to all players.

# gamemode <mode> [player]
@ limbo.command.gamemode (for self)
# limbo.command.gamemode.others
- Change the gamemode of a player.

# allowlist toggle
@ limbo.command.allowlist
- Toggle the list between "allowlist" and "denylist".

# allowlist <add|remove> <player>
@ limbo.command.allowlist
- Add or remove a player from the allowlist.

# allowlist reload
@ limbo.command.allowlist
- Reload the allowlist.

# kick <player> [reason]
@ limbo.command.kick
- Kick a player from the server.

# stop
@ limbo.command.stop
- Stop the server.
```

## Metrics

> LimboService uses [bStats](https://bstats.org/) to collect anonymous usage statistics.

![bStats](https://bstats.org/signatures/server-implementation/LimboService.svg)

## Development

LimboService also supports to be used as a library to develop your own plugins, just like Bukkit/Spigot does.

Here is a simple and minimal example to create a plugin for LimboService:

```java
package com.example.myplugin;

import com.loohp.limbo.plugins.LimboPlugin;

public class MyPlugin extends LimboPlugin {
    @Override
    public void onLoad() {
        // Called when the plugin is loading
    }

    @Override
    public void onEnable() {
        // Called when the plugin is enabling
    }

    @Override
    public void onDisable() {
        // Called when the plugin is disabling
    }
}
```

And you need to create a `plugin.yml` or `limbo.yml` file in the resources directory:

```yaml
main: com.example.myplugin.MyPlugin
name: MyPlugin
version: 1.0
author: MyCoolName
description: A simple plugin for LimboService.
```

And... that's it! You can now use the LimboService API to develop your own plugins!

> [!NOTE]
> You can find maven packages
> at [GitHub Packages](https://github.com/orgs/YourCraftMC/packages?repo_name=LimboService)
> and Javadocs [HERE](https://yourcraftmc.github.io/LimboService/).
>
> Remember to replace `VERSION` with the version you're using configs down below.

<details>
<summary><b>Maven Dependency</b></summary>

```xml

<project>
    <repositories>

        <repository>
            <!-- Using Maven Central Repository for secure and stable updates, though synchronization might be needed. -->
            <id>maven</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
        </repository>

        <repository>
            <!-- Using GitHub dependencies for real-time updates, configuration required (recommended). -->
            <id>limboservice-repo</id>
            <name>GitHub Packages for LimboService</name>
            <url>https://maven.pkg.github.com/YourCraftMC/LimboService</url>
        </repository>

    </repositories>

    <project>
        <dependencies>
            <dependency>
                <groupId>cn.ycraft</groupId>
                <artifactId>limboservice</artifactId>
                <version>[VERSION]</version>
                <scope>compile</scope>
            </dependency>
        </dependencies>
    </project>
</project>
```

</details>

<details>
<summary><b>Gradle Dependency</b></summary>

```groovy
repositories {
    // Using Maven Central Repository for secure and stable updates, though synchronization might be needed.
    mavenCentral()

    // Using GitHub dependencies for real-time updates, configuration required (recommended).
    maven { url 'https://maven.pkg.github.com/YourCraftMC/LimboService' }
}

dependencies {
    api "cn.ycraft:limboservice:[LATEST RELEASE]"
}
```

</details>

## Dependencies

- [**MCProtocolLib**](https://github.com/GeyserMC/MCProtocolLib): Used to implement base MineCraft Server functions.
- [**adventure**](https://github.com/KyoriPowered/adventure): Used to implement message components.
- [**configured**](https://github.com/CarmJos/configured): Used to implement configurations.
- [**LiteCommands**](https://github.com/Rollczi/LiteCommands): Used to implement command systems.
- [**EasyPlugin-Color**](https://github.com/CarmJos/EasyPlugin): Used to parse generic color codes.

For more dependencies, please see [Dependencies](https://github.com/YourCraftMC/LimboService/network/dependencies) .

## Acknowledgements & Supports

Many thanks to [LoohpJames(@LOOHP)](https://github.com/LOOHP)
and [many other developers](https://github.com/LOOHP/Limbo/graphs/contributors) for their huge contribution to the
original project.

Many thanks to Jetbrains for kindly providing a license for us to work on this and other open-source projects.

[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/YourCraftMC/LimboService)

This project currently is mainly maintained by the  [YourCraftMC(你的世界)](https://www.ycraft.cn) .

<img src="https://raw.githubusercontent.com/YourCraftMC/.github/refs/heads/main/imgs/text_1440p.png" alt="Team logo" width="400px">

## Open Source License

This project's source code is licensed under
the [GNU LESSER GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/lgpl-3.0.html).
