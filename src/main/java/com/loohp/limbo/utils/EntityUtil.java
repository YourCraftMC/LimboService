package com.loohp.limbo.utils;

import com.loohp.limbo.entity.DataWatcher;
import com.loohp.limbo.entity.Entity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Pose;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.*;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class EntityUtil {
    public static ClientboundSetEntityDataPacket metadata(Entity entity, boolean allFields, Field... fields) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ByteBuf output = Unpooled.buffer();
        MinecraftTypes.writeVarInt(output, entity.getEntityId());
        Collection<DataWatcher.WatchableObject> watches;
        if (allFields) {
            watches = new HashSet<>(entity.getDataWatcher().getWatchableObjects().values());
        } else {
            watches = new HashSet<>();
            Map<Field, DataWatcher.WatchableObject> entries = entity.getDataWatcher().getWatchableObjects();
            for (Field field : fields) {
                DataWatcher.WatchableObject watch = entries.get(field);
                if (watch != null) {
                    watches.add(watch);
                }
            }
        }

        Map<Integer, Integer> bitmasks = new HashMap<>();
        Iterator<DataWatcher.WatchableObject> itr = watches.iterator();
        while (itr.hasNext()) {
            DataWatcher.WatchableObject watch = itr.next();
            if (watch.isBitmask()) {
                itr.remove();
                Integer bitmask = bitmasks.get(watch.getIndex());
                if (bitmask == null) {
                    bitmask = 0;
                }
                if ((boolean) watch.getValue()) {
                    bitmask |= watch.getBitmask();
                } else {
                    bitmask &= ~watch.getBitmask();
                }
                bitmasks.put(watch.getIndex(), bitmask);
            }
        }
        for (Map.Entry<Integer, Integer> entry : bitmasks.entrySet()) {
            watches.add(new DataWatcher.WatchableObject(entry.getValue().byteValue(), entry.getKey(), DataWatcher.WatchableObjectType.BYTE));
        }

        List<EntityMetadata<?, ?>> list = new ArrayList<>();
        for (DataWatcher.WatchableObject watch : watches) {
            if (watch.getValue() != null) {
                switch (watch.getType()) {
                    //case BLOCKID:
                    //	break;
                    case POSITION:
                        if (watch.isOptional()) {
                            list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.OPTIONAL_POSITION, Optional.ofNullable(((Vector3i) watch.getValue()))));
                        } else {
                            list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.POSITION, ((Vector3i) watch.getValue())));
                        }
                        break;
                    case BOOLEAN:
                        list.add(new BooleanEntityMetadata(watch.getIndex(), MetadataType.BOOLEAN, ((boolean) watch.getValue())));
                        break;
                    case BYTE:
                        list.add(new ByteEntityMetadata(watch.getIndex(), MetadataType.BYTE, ((byte) watch.getValue())));
                        break;
                    case CHAT:
                        if (watch.isOptional()) {
                            list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.OPTIONAL_CHAT, Optional.ofNullable(((Component) watch.getValue()))));
                        } else {
                            list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.CHAT, ((Component) watch.getValue())));
                        }
                        break;
                    //case DIRECTION:
                    //	break;
                    case FLOAT:
                        list.add(new FloatEntityMetadata(watch.getIndex(), MetadataType.FLOAT, (float) watch.getValue()));
                        break;
                    //case NBT:
                    //	break;
                    //case PARTICLE:
                    //	break;
                    case POSE:
                        list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.POSE, (Pose) watch.getValue()));
                        break;
                    case ROTATION:
                        list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.ROTATION, (Vector3f) watch.getValue()));
                        break;
                    //case SLOT:
                    //	break;
                    case STRING:
                        list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.STRING, ((String) watch.getValue())));
                        break;
                    case UUID:
                        list.add(new ObjectEntityMetadata<>(watch.getIndex(), MetadataType.OPTIONAL_UUID, Optional.ofNullable(((UUID) watch.getValue()))));
                        break;
                    case VARINT:
                        list.add(new IntEntityMetadata(watch.getIndex(), MetadataType.INT, (int) watch.getValue()));
                        break;
                    //case VILLAGER_DATA:
                    //	break;
                    default:
                        break;
                }
            }
        }
        return new ClientboundSetEntityDataPacket(entity.getEntityId(), list.toArray(new EntityMetadata[0]));
    }
}
