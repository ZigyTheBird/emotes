package io.github.kosmx.emotes.common.network.payloads.type;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface HasPlayerPayload<T extends CustomPacketPayload> {
    T removePlayerID();
}
