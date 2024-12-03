package io.github.kosmx.emotes.common.network.payloads;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.payloads.type.EmoteDataPayload;
import io.github.kosmx.emotes.common.network.utils.KeyframeAnimationUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Sent by the server after discovery to add animations to the player's list
 * @param emoteData Animation to add (one per packet)
 */
public record EmoteFilePayload(KeyframeAnimation emoteData) implements CustomPacketPayload, EmoteDataPayload {
    public static final CustomPacketPayload.Type<EmoteFilePayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("file"));

    public static final StreamCodec<ByteBuf, EmoteFilePayload> STREAM_CODEC = StreamCodec.composite(
            KeyframeAnimationUtils.STREAM_CODEC_WITH_HEADER, EmoteFilePayload::emoteData,

            EmoteFilePayload::new
    );

    @Override
    public @NotNull Type<EmoteFilePayload> type() {
        return EmoteFilePayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("EmoteFilePayload{emoteData=%s}", emoteData());
    }
}
