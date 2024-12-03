package io.github.kosmx.emotes.common.network.payloads;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.payloads.type.HasPlayerPayload;
import io.github.kosmx.emotes.common.network.utils.KeyframeAnimationUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Sent by the client when it plays
 * Sent by the server to play another player's emote
 *
 * @param emoteData Animation to play
 * @param tick A tick to start with
 * @param playerId Player who plays the animation (empty if sent from the client)
 */
public record EmotePlayPayload(KeyframeAnimation emoteData, int tick, Optional<UUID> playerId, boolean isForced) implements CustomPacketPayload, HasPlayerPayload<EmotePlayPayload> {
    public static final CustomPacketPayload.Type<EmotePlayPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("play"));

    public static final StreamCodec<ByteBuf, EmotePlayPayload> STREAM_CODEC = StreamCodec.composite(
            KeyframeAnimationUtils.STREAM_CODEC, EmotePlayPayload::emoteData,
            ByteBufCodecs.INT, EmotePlayPayload::tick,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), EmotePlayPayload::playerId,
            ByteBufCodecs.BOOL, EmotePlayPayload::isForced,
            EmotePlayPayload::new
    );

    public EmotePlayPayload(KeyframeAnimation emoteData, int tick, UUID playerId, boolean isForced) {
        this(emoteData, tick, Optional.ofNullable(playerId), isForced);
    }

    public EmotePlayPayload(KeyframeAnimation emoteData, int tick, UUID playerId) {
        this(emoteData, tick, playerId, false);
    }

    public EmotePlayPayload(KeyframeAnimation emoteData, UUID playerId) {
        this(emoteData, 0, playerId);
    }

    public EmotePlayPayload(KeyframeAnimation emoteData) {
        this(emoteData, null);
    }

    @Override
    public @NotNull Type<EmotePlayPayload> type() {
        return EmotePlayPayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("EmotePlayPayload{emoteData=%s, startingAt=%s, player=%s, isForced=%s}", emoteData(), tick(), getPlayerID(), isForced());
    }

    @Override
    public UUID getPlayerID() {
        return playerId().orElse(null);
    }

    public boolean valid() {
        return (boolean) emoteData().extraData.getOrDefault("valid", false);
    }

    @Override
    public EmotePlayPayload removePlayerID() {
        return new EmotePlayPayload(emoteData(), tick(), Optional.empty(), isForced());
    }

    @Override
    public EmotePlayPayload setPlayerID(UUID player) {
        return new EmotePlayPayload(emoteData(), tick(), player, isForced());
    }
}
