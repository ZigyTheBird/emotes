package io.github.kosmx.emotes.common.network.payloads;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Used to send a large packet in batches
 *
 * @param id The type of payload in this batch
 * @param data Packet data in bytes
 * @param last Indicates that the last packet has been sent and handling can continue
 */
public record StreamPayload(ResourceLocation id, byte[] data, boolean last) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StreamPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("stream"));

    public static final StreamCodec<ByteBuf, StreamPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StreamPayload::id,
            ByteBufCodecs.BYTE_ARRAY, StreamPayload::data,
            ByteBufCodecs.BOOL, StreamPayload::last,

            StreamPayload::new
    );

    @Override
    public @NotNull Type<StreamPayload> type() {
        return StreamPayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("StreamPayload{type=%s, isLast=%s}", id(), last());
    }
}
