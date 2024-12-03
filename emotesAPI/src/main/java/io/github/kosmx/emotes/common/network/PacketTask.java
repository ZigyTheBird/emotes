package io.github.kosmx.emotes.common.network;


import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;
import java.util.function.Supplier;

public enum PacketTask {
    UNKNOWN(0, () -> null),
    STREAM(1, () -> new EmotePlayPayload(null, 0, Optional.empty(), false)),
    CONFIG(8, () -> DiscoveryPayload.DEFAULT),
    STOP(10, () -> new EmoteStopPayload(null, Optional.empty(), false)),
    FILE(0x10, () -> new EmoteFilePayload(null));

    public final byte id;
    public final Supplier<CustomPacketPayload> payload;

    PacketTask(byte id, Supplier<CustomPacketPayload> payload) {
        this.id = id;
        this.payload = payload;
    }

    public static PacketTask getTaskFromID(byte b){
        for(PacketTask task:PacketTask.values()){
            if(task.id == b)return task;
        }
        return UNKNOWN;
    }

    public static PacketTask getTaskFromPayload(CustomPacketPayload payload){
        for(PacketTask task:PacketTask.values()){
            if(payload.getClass().isInstance(task.payload.get()))return task;
        }
        return UNKNOWN;
    }

    PacketTask(int i, Supplier<CustomPacketPayload> payload) {
        this((byte) i, payload);
    }
}