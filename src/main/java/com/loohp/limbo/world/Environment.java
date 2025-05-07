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

package com.loohp.limbo.world;

import com.loohp.limbo.registry.RegistryCustom;
import net.kyori.adventure.key.Key;

import java.util.HashSet;
import java.util.Set;

public class Environment {

    public static final Environment NORMAL = new Environment(Key.key("minecraft:overworld"), true, 0);
    public static final Environment NETHER = new Environment(Key.key("minecraft:the_nether"), false, 3);
    public static final Environment END = new Environment(Key.key("minecraft:the_end"), false, 2);

    public static final Set<Environment> REGISTERED_ENVIRONMENTS = new HashSet<>();

    public static Environment fromKey(Key key) {
        if (key.equals(NORMAL.getKey())) {
            return NORMAL;
        } else if (key.equals(NETHER.getKey())) {
            return NETHER;
        } else if (key.equals(END.getKey())) {
            return END;
        }
        return null;
    }

    @Deprecated
    public static Environment createCustom(Key key, int id) {
        return createCustom(key, true, id);
    }

    public static Environment createCustom(Key key, boolean hasSkyLight, int id) {
        if (REGISTERED_ENVIRONMENTS.stream().anyMatch(each -> each.getKey().equals(key))) {
            throw new IllegalArgumentException("An Environment is already created with this Key");
        }
        return new Environment(key, hasSkyLight, id);
    }

    public static Environment getCustom(Key key) {
        return REGISTERED_ENVIRONMENTS.stream().filter(each -> each.getKey().equals(key)).findFirst().orElse(null);
    }

    //=========================

    private final Key key;
    private final boolean hasSkyLight;
    private final int id;

    private Environment(Key key, boolean hasSkyLight, int id) {
        this.key = key;
        this.hasSkyLight = hasSkyLight;
        this.id = id;
    }

    public Key getKey() {
        return key;
    }

    public boolean hasSkyLight() {
        return hasSkyLight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (hasSkyLight ? 1231 : 1237);
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Environment other = (Environment) obj;
        if (hasSkyLight != other.hasSkyLight) {
            return false;
        }
        if (key == null) {
            return other.key == null;
        } else return key.equals(other.key);
    }

    public int getId() {
        return id;
    }
}
