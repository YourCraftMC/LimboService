/*
  ~ This file is part of Limbo.
  ~
  ~ Copyright (C) 2024. YourCraftMC <admin@ycraft.cn>
  ~ Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
  ~ Copyright (C) 2022. Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

package com.loohp.limbo.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class Unsafe {

    @Deprecated
    public static KeyedBossBar a(Key key, BossBar properties) {
        return new KeyedBossBar(key, properties);
    }

    private final KeyedBossBar instance;

    protected Unsafe(KeyedBossBar instance) {
        this.instance = instance;
    }

    @Deprecated
    public KeyedBossBar.LimboBossBarHandler a() {
        return instance.listener;
    }

    @Deprecated
    public void b() {
        instance.valid.set(false);
    }

}
