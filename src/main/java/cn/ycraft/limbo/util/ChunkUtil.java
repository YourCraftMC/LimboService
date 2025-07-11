package cn.ycraft.limbo.util;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.registry.BuiltInRegistries;
import com.loohp.limbo.utils.BitsUtils;
import com.loohp.limbo.world.Environment;
import com.loohp.limbo.world.GeneratedBlockDataMappings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.querz.mca.Chunk;
import net.querz.mca.Section;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.HeightmapTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class ChunkUtil {
    public static ClientboundLevelChunkWithLightPacket create(int chunkX, int chunkZ, Chunk chunk, Environment environment, List<Byte[]> skylightArrays, List<Byte[]> blocklightArrays) {

        BitSet skyLightBitSet = new BitSet();
        BitSet skyLightBitSetInverse = new BitSet();
        for (int i = Math.min(17, skylightArrays.size() - 1); i >= 0; i--) {
            skyLightBitSet.set(i, skylightArrays.get(i) != null);
            skyLightBitSetInverse.set(i, skylightArrays.get(i) == null);
        }

        BitSet blockLightBitSet = new BitSet();
        BitSet blockLightBitSetInverse = new BitSet();
        for (int i = Math.min(17, blocklightArrays.size() - 1); i >= 0; i--) {
            blockLightBitSet.set(i, blocklightArrays.get(i) != null);
            blockLightBitSetInverse.set(i, blocklightArrays.get(i) == null);
        }


        ByteBuf dataOut = createSectionData(chunk, environment);

        byte[] chunkData = new byte[dataOut.readableBytes()];
        dataOut.readBytes(chunkData);
        dataOut.release();

        Map<HeightmapTypes, long[]> heightMaps = new HashMap<>();
        // 1
        heightMaps.put(HeightmapTypes.MOTION_BLOCKING, chunk.getHeightMaps().getLongArray("MOTION_BLOCKING"));

        ListTag<CompoundTag> tileEntities = chunk.getTileEntities();
        BlockEntityInfo[] blockEntities = new BlockEntityInfo[tileEntities.size()];
        int index = 0;
        for (CompoundTag each : tileEntities) {
            int x = each.getInt("x") % 16;
            int y = each.getInt("y");
            int z = each.getInt("z") % 16;
            Key blockId = Key.key(chunk.getBlockStateAt(x, y, z).getString("Name"));
            int id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getId(blockId);
            BlockEntityType blockEntityType = BlockEntityType.from(id);
            if (blockEntityType == null) {
                Limbo.getInstance().getConsole().sendMessage("Unknown block entity type: " + blockId);
            }
            blockEntities[index] = new BlockEntityInfo(
                x, y, z,
                blockEntityType,
                (NbtMap) convert(each)
            );

            ++index;
        }
        ArrayList<byte[]> skyUpdates = new ArrayList<>();
        ArrayList<byte[]> blockUpdates = new ArrayList<>();
        for (Byte[] skylightArray : skylightArrays) {
            if (skylightArray == null) continue;
            byte[] bytes = new byte[skylightArray.length];
            for (int i = 0; i < skylightArray.length; i++) {
                bytes[i] = skylightArray[i];
            }
            skyUpdates.add(bytes);
        }
        for (Byte[] blocklightArray : blocklightArrays) {
            if (blocklightArray == null) continue;
            byte[] bytes = new byte[blocklightArray.length];
            for (int i = 0; i < blocklightArray.length; i++) {
                bytes[i] = blocklightArray[i];
            }
            blockUpdates.add(bytes);
        }
        LightUpdateData lightData = new LightUpdateData(skyLightBitSet, blockLightBitSet, skyLightBitSetInverse, blockLightBitSetInverse, skyUpdates, blockUpdates);
        return new ClientboundLevelChunkWithLightPacket(chunkX, chunkZ, chunkData, heightMaps, blockEntities, lightData);
    }

    public static @NotNull ByteBuf createSectionData(Chunk chunk, Environment environment) {
        ByteBuf dataOut = Unpooled.buffer();
        for (int i = 0; i < 16; i++) {
            Section section = chunk.getSection(i);
            if (section != null) {
                short counter = 0;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            CompoundTag tag = section.getBlockStateAt(x, y, z);
                            if (tag != null && !tag.getString("Name").equals("minecraft:air")) {
                                counter++;
                            }
                        }
                    }
                }
                dataOut.writeShort(counter);

                int newBits = 32 - Integer.numberOfLeadingZeros(section.getPalette().size() - 1);
                newBits = Math.max(newBits, 4);
                if (newBits <= 8) { // indirect
                    dataOut.writeByte(newBits);

                    MinecraftTypes.writeVarInt(dataOut, section.getPalette().size()); // Palette Length
                    for (CompoundTag tag : section.getPalette()) {
                        MinecraftTypes.writeVarInt(dataOut, GeneratedBlockDataMappings.getGlobalPaletteIDFromState(tag));
                    }

                    BitSet bits = BitSet.valueOf(section.getBlockStates());
                    int shift = 64 % newBits;
                    int longsNeeded = (int) Math.ceil(4096 / (double) (64 / newBits));
                    for (int u = 64; u <= bits.length(); u += 64) {
                        BitsUtils.shiftAfter(bits, u - shift, shift);
                    }

                    long[] formattedLongs = bits.toLongArray();

                    for (int u = 0; u < longsNeeded; u++) {
                        if (u < formattedLongs.length) {
                            dataOut.writeLong(formattedLongs[u]);
                        } else {
                            dataOut.writeLong(0);
                        }
                    }
                } else {
                    try {
                        dataOut.writeByte(16);
                        section.getBlockStates();
                        int longsNeeded = 1024;
                        List<Integer> list = new LinkedList<>();
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                for (int x = 0; x < 16; x++) {
                                    list.add(GeneratedBlockDataMappings.getGlobalPaletteIDFromState(section.getBlockStateAt(x, y, z)));
                                }
                            }
                        }
                        List<Long> globalLongs = new ArrayList<>();
                        long currentLong = 0;
                        int pos = 0;
                        int u = 0;
                        while (pos < longsNeeded) {
                            if (u == 3) {
                                globalLongs.add(currentLong);
                                currentLong = 0;
                                u = 0;
                                pos++;
                            } else {
                                u++;
                            }
                            int id = list.isEmpty() ? 0 : list.remove(0);
                            currentLong = currentLong << 16;
                            currentLong |= id;
                        }
                        for (int j = 0; j < longsNeeded; j++) {
                            if (j < globalLongs.size()) {
                                dataOut.writeLong(globalLongs.get(j));
                            } else {
                                dataOut.writeLong(0);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                dataOut.writeShort(0);
                dataOut.writeByte(0);
                MinecraftTypes.writeVarInt(dataOut, 0);
            }
            int biome;
            if (environment.equals(Environment.END)) {
                biome = 56; //the_end
            } else if (environment.equals(Environment.NETHER)) {
                biome = 34; //nether_waste
            } else if (environment.equals(Environment.NORMAL)) {
                biome = 40; //plains
            } else {
                biome = 40; //plains
            }
            dataOut.writeByte(0);
            MinecraftTypes.writeVarInt(dataOut, biome);
        }
        return dataOut;
    }

    public static Object convert(CompoundTag original) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             NBTOutputStream stream = new NBTOutputStream(outputStream)) {
            stream.writeTag(original, Integer.MAX_VALUE);
            stream.flush();
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                 DataInputStream input = new DataInputStream(inputStream);
                 NBTInputStream nbtInputStream = new NBTInputStream(input)) {
                return nbtInputStream.readTag(Integer.MAX_VALUE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
