package io.github.kosmx.emotes.common.network.payloads;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * The client and server exchange this packet to synchronize each other's versions
 * @param animationFormat Animator's version {@link AnimationBinary#getCurrentVersion()}
 */
public record DiscoveryPayload(int animationFormat, boolean sendPlayerID, boolean doesServerTrackEmotePlay, boolean disableNBS, boolean allowStream, boolean allowSync) implements CustomPacketPayload {
    public static final DiscoveryPayload DEFAULT = new DiscoveryPayload(AnimationBinary.getCurrentVersion(), false, true, false, true, true);

    public static final CustomPacketPayload.Type<DiscoveryPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("discovery"));

    public static final StreamCodec<ByteBuf, DiscoveryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DiscoveryPayload::animationFormat,
            ByteBufCodecs.BOOL, DiscoveryPayload::sendPlayerID,
            ByteBufCodecs.BOOL, DiscoveryPayload::doesServerTrackEmotePlay,
            ByteBufCodecs.BOOL, DiscoveryPayload::disableNBS,
            ByteBufCodecs.BOOL, DiscoveryPayload::allowStream,
            ByteBufCodecs.BOOL, DiscoveryPayload::allowSync,
            DiscoveryPayload::new
    );

    @Override
    public @NotNull Type<DiscoveryPayload> type() {
        return DiscoveryPayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("DiscoveryPayload{animationFormat=%s, doesServerTrackEmotePlay=%s, disableNBS=%s}", animationFormat(), doesServerTrackEmotePlay(), disableNBS());
    }
}
