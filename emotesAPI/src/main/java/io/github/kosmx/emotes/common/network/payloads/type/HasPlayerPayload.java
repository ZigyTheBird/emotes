package io.github.kosmx.emotes.common.network.payloads.type;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.UUID;

public interface HasPlayerPayload<T extends CustomPacketPayload> {
    @CheckReturnValue
    T removePlayerID();

    @CheckReturnValue
    T setPlayerID(UUID player);

    @CheckReturnValue
    UUID getPlayerID();
}
