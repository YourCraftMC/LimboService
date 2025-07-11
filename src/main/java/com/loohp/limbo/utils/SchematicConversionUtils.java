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

package com.loohp.limbo.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;

public class SchematicConversionUtils {
    private static final Gson gson = new Gson();

    public static CompoundTag toTileEntityTag(CompoundTag tag) {
        int[] pos = tag.getIntArray("Pos");
        tag.remove("Pos");
        tag.remove("Id");
        tag.putInt("x", pos[0]);
        tag.putInt("y", pos[1]);
        tag.putInt("z", pos[2]);
        for (String key : tag.keySet()) {
            Tag<?> v = tag.get(key);
            tag.put(key, fixComponentTag(v).result);
        }
        return tag;
    }

    public static FixedResult fixComponentTag(Tag<?> tag) {
        if (tag instanceof StringTag) {
            String value = ((StringTag) tag).getValue();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return FixedResult.normal(new StringTag(value.substring(1, value.length() - 1)));
            } else if (value.startsWith("{") && value.endsWith("}")) {
                return FixedResult.component(NbtComponentSerializer.jsonComponentToTag(gson.fromJson(value, JsonObject.class)));
            }
        } else if (tag instanceof CompoundTag) {
            CompoundTag newed = new CompoundTag();
            for (String key : ((CompoundTag) tag).keySet()) {
                Tag<?> v = ((CompoundTag) tag).get(key);
                newed.put(key, fixComponentTag(v).result);
            }
            return FixedResult.normal(newed);
        } else if (tag instanceof ListTag<?>) {
            ListTag<?> newed = ListTag.createUnchecked(((ListTag<?>) tag).getTypeClass());
            boolean compound = false;
            for (Tag<?> subTag : (ListTag<?>) tag) {
                FixedResult fixedResult = fixComponentTag(subTag);
                Tag<?> t = fixedResult.result;
                boolean notMatch = t.getClass() != newed.getTypeClass();
                if (notMatch && t.getClass() == CompoundTag.class && fixedResult.isComponent) {
                    compound = true;
                    newed = convertTextComponentCompoundList(newed);
                    newed.addUnchecked(t);
                } else if (notMatch && compound) {
                    newed.addUnchecked(convertCompound(t));
                } else {
                    newed.addUnchecked(t);
                }
            }
            return FixedResult.normal(newed);
        }
        return FixedResult.normal(tag);
    }

    private static ListTag<?> convertTextComponentCompoundList(ListTag<?> old) {
        ListTag<?> listTag = ListTag.createUnchecked(CompoundTag.class);
        for (Tag<?> tag : old) {
            listTag.addUnchecked(convertCompound(tag));
        }
        return listTag;
    }

    private static Tag<?> convertCompound(Tag<?> tag) {
        if (tag instanceof StringTag) {
            String value = ((StringTag) tag).getValue();
            GsonComponentSerializer serializer = GsonComponentSerializer.builder().editOptions(edit -> edit.value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, false)).build();
            Component deserialize = serializer.deserializeFromTree(new JsonPrimitive(value));
            JsonElement element = serializer.serializeToTree(deserialize);
            return NbtComponentSerializer.jsonComponentToTag(element);
        } else if (tag.getClass() == CompoundTag.class) {
            return tag;
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag.getClass());
        }
    }

    public static CompoundTag toBlockTag(String input) {
        int index = input.indexOf("[");
        CompoundTag tag = new CompoundTag();
        if (index < 0) {
            tag.putString("Name", Key.key(input).toString());
            return tag;
        }

        tag.putString("Name", Key.key(input.substring(0, index)).toString());

        String[] states = input.substring(index + 1, input.lastIndexOf("]")).replace(" ", "").split(",");

        CompoundTag properties = new CompoundTag();
        for (String state : states) {
            String key = state.substring(0, state.indexOf("="));
            String value = state.substring(state.indexOf("=") + 1);
            properties.putString(key, value);
        }

        tag.put("Properties", properties);

        return tag;
    }

    public record FixedResult(Tag<?> result, boolean isComponent) {
        public static FixedResult normal(Tag<?> result) {
            return new FixedResult(result, false);
        }

        public static FixedResult component(Tag<?> result) {
            return new FixedResult(result, true);
        }
    }
}
