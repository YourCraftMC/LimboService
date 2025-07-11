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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.loohp.limbo.Limbo;
import net.kyori.adventure.key.Key;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GeneratedBlockDataMappings {

    private static JsonObject globalPalette = new JsonObject();
    private static final Gson gson = new Gson();

    static {
        String block = "reports/blocks.json";
        InputStream inputStream = Limbo.class.getClassLoader().getResourceAsStream(block);
        if (inputStream == null) {
            throw new RuntimeException("Failed to load " + block + " from jar!");
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            globalPalette = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getGlobalPaletteIDFromKey(Key key) {
        String blockId = key.asString();
        JsonObject data = globalPalette.get(blockId).getAsJsonObject();
        Object obj = data.get("properties");
        if (obj == null) {
            return data.get("states").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
        }

        for (JsonElement entry : data.get("states").getAsJsonArray()) {
            final JsonObject element = entry.getAsJsonObject();
            if (element.has("default") && element.get("default").getAsBoolean()) {
                return element.get("id").getAsInt();
            }
        }

        throw new IllegalStateException();
    }


    @SuppressWarnings("unchecked")
    public static int getGlobalPaletteIDFromState(CompoundTag tag) {
        try {
            String blockname = tag.getString("Name");

            JsonObject data = globalPalette.get(blockname).getAsJsonObject();
            Object obj = data.get("properties");
            if (obj == null) {
                return data.get("states").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
            }

            if (tag.containsKey("Properties")) {
                CompoundTag blockProp = tag.get("Properties", CompoundTag.class);
                Map<String, String> blockstate = new HashMap<>();
                for (String key : blockProp.keySet()) {
                    blockstate.put(key, blockProp.getString(key));
                }

                for (JsonElement entry : data.get("states").getAsJsonArray()) {
                    JsonObject jsonobj = entry.getAsJsonObject();
                    if (jsonobj.get("properties").getAsJsonObject().keySet().stream().allMatch(key -> Objects.equals(blockstate.get(key), jsonobj.get("properties").getAsJsonObject().get(key).getAsString()))) {
                        return jsonobj.get("id").getAsInt();
                    }
                }
            }

            for (JsonElement entry : data.get("states").getAsJsonArray()) {
                final JsonObject element = entry.getAsJsonObject();
                if (element.has("default") && element.get("default").getAsBoolean()) {
                    return element.get("id").getAsInt();
                }
            }

            throw new IllegalStateException();
        } catch (Throwable e) {
            String snbt;
            try {
                snbt = SNBTUtil.toSNBT(tag);
            } catch (IOException e1) {
                snbt = tag.valueToString();
            }
            new IllegalStateException("Unable to get global palette id for " + snbt + " (Is this scheme created in the same Minecraft version as Limbo?)", e).printStackTrace();
        }
        return 0;
    }

}
