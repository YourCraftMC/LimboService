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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.geysermc.mcprotocollib.auth.GameProfile;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MojangAPIUtils {
    private static final Gson gson = new Gson();

    public static class SkinResponse {

        String skin;
        String signature;

        public SkinResponse(String skin, String signature) {
            this.skin = skin;
            this.signature = signature;
        }

        public String getSkin() {
            return skin;
        }

        public String getSignature() {
            return signature;
        }

    }

    public static UUID getOnlineUUIDOfPlayerFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()).replace(" ", "");
                if (!reply.contains("\"error\":\"BadRequestException\"")) {
                    String uuid = reply.split("\"id\":\"")[1].split("\"")[0];
                    return UUID.fromString(uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                } else {
                    return null;
                }
            } else {
                System.err.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SkinResponse getSkinFromMojangServer(String username) {
        UUID uuid = getOnlineUUIDOfPlayerFromMojang(username);
        if (uuid == null) {
            return null;
        }
        return getSkinFromMojangServer(uuid);
    }

    public static SkinResponse getSkinFromMojangServer(UUID uuid) {
        final List<GameProfile.Property> properties = getPropertiesFromMojangServer(uuid);
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        final GameProfile.Property textures = properties.stream().filter(property -> property.getName().equals("textures")).findFirst().orElse(null);
        if (textures == null) {
            return null;
        }
        return new SkinResponse(textures.getValue(), textures.getSignature());
    }

    public static List<GameProfile.Property> getPropertiesFromMojangServer(UUID uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String reply = String.join("", new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.toList())).replace(" ", "");
                final ArrayList<GameProfile.Property> list = new ArrayList<>();
                final JsonObject jsonObject = gson.fromJson(reply, JsonObject.class);
                if (jsonObject.has("properties")) {
                    final JsonArray properties = jsonObject.getAsJsonArray("properties");
                    for (int i = 0; i < properties.size(); i++) {
                        final JsonObject property = properties.get(i).getAsJsonObject();
                        list.add(new GameProfile.Property(
                            property.get("name").getAsString(),
                            property.get("value").getAsString(),
                            property.get("signature").getAsString()
                        ));
                    }
                }
                return list;
            } else {
                System.err.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
