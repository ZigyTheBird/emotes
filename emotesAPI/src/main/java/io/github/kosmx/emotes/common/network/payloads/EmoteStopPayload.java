package io.github.kosmx.emotes.common.network.payloads;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.payloads.type.HasPlayerPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Sent by the client to stop its playback
 * Sent by the server to stop another player's animation
 *
 * @param stopEmoteID Uuid of animation to stop
 * @param playerId Player at whom the animation should stop (empty if sent from the client)
 */
public record EmoteStopPayload(UUID stopEmoteID, Optional<UUID> playerId, boolean isForced) implements CustomPacketPayload, HasPlayerPayload<EmoteStopPayload> {
    public static final CustomPacketPayload.Type<EmoteStopPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("stop"));

    public static final StreamCodec<ByteBuf, EmoteStopPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, EmoteStopPayload::stopEmoteID,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), EmoteStopPayload::playerId,
            ByteBufCodecs.BOOL, EmoteStopPayload::isForced,
            EmoteStopPayload::new
    );

    public EmoteStopPayload(UUID stopEmoteID, UUID playerId, boolean isForced) {
        this(stopEmoteID, Optional.ofNullable(playerId), isForced);
    }

    public EmoteStopPayload(UUID stopEmoteID, UUID playerId) {
        this(stopEmoteID, playerId, false);
    }

    @Override
    public @NotNull Type<EmoteStopPayload> type() {
        return EmoteStopPayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("EmoteStopPayload{stopEmoteID=%s, player=%s, isForced=%s}", stopEmoteID(), getPlayerID(), isForced());
    }

    @Override
    public UUID getPlayerID() {
        return playerId().orElse(null);
    }

    @Override
    public EmoteStopPayload removePlayerID() {
        return new EmoteStopPayload(stopEmoteID(), Optional.empty(), isForced());
    }

    @Override
    public EmoteStopPayload setPlayerID(UUID player) {
        return new EmoteStopPayload(stopEmoteID(), player, isForced());
    }
}
