/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.utils;

import com.loohp.limbo.Limbo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ForwardingUtils {

    public static final Key VELOCITY_FORWARDING_CHANNEL = Key.key("velocity", "player_info");

    public static boolean validateVelocityModernResponse(byte[] data) {
        ByteBuf input = Unpooled.wrappedBuffer(data);

        byte[] signature = new byte[32];
        input.readBytes(signature);

        boolean foundValid = false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            for (String secret : Limbo.getInstance().getServerProperties().getForwardingSecrets()) {
                SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                mac.init(key);
                mac.update(data, 32, data.length - 32);
                byte[] sig = mac.doFinal();
                if (Arrays.equals(signature, sig)) {
                    foundValid = true;
                    break;
                }
            }
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Unable to authenticate data", e);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
            throw new AssertionError(e);
        } finally {
            input.release();
        }

        return foundValid;
    }

    public static VelocityModernForwardingData getVelocityDataFrom(byte[] data) {
        ByteBuf input = Unpooled.wrappedBuffer(data);

        input.skipBytes(32);

        int velocityVersion = MinecraftTypes.readVarInt(input);
        if (velocityVersion != 1) {
            System.out.println("Unsupported Velocity version! Stopping Execution");
            throw new AssertionError("Unknown Velocity Packet");
        }
        String address = MinecraftTypes.readString(input);
        UUID uuid = MinecraftTypes.readUUID(input);
        String username = MinecraftTypes.readString(input);

        int count = MinecraftTypes.readVarInt(input);
        List<GameProfile.Property> properties = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            String propertyName = MinecraftTypes.readString(input);
            String propertyValue = MinecraftTypes.readString(input);
            String propertySignature = "";
            boolean signatureIncluded = input.readBoolean();
            if (signatureIncluded) {
                propertySignature = MinecraftTypes.readString(input);
            }
            properties.add(new GameProfile.Property(propertyName, propertyValue, propertySignature));
        }

        return new VelocityModernForwardingData(velocityVersion, address, uuid, username, properties);
    }

    public static class VelocityModernForwardingData {

        public final int version;
        public final String ipAddress;
        public final UUID uuid;
        public final String username;
        public final List<GameProfile.Property> properties;

        public VelocityModernForwardingData(int version, String ipAddress, UUID uuid, String username, List<GameProfile.Property> properties) {
            this.version = version;
            this.ipAddress = ipAddress;
            this.uuid = uuid;
            this.username = username;
            this.properties = properties;
        }
    }
}