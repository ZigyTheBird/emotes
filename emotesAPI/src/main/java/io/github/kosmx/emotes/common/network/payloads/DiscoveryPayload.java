package io.github.kosmx.emotes.common.network.payloads;

import com.google.common.collect.Maps;
import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The client and server exchange this packet to synchronize each other's versions
 * @param versions The actual versions
 */
public record DiscoveryPayload(Map<Byte, Byte> versions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DiscoveryPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonData.newIdentifier("discovery"));

    public static final StreamCodec<ByteBuf, DiscoveryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.BYTE, ByteBufCodecs.BYTE), DiscoveryPayload::versions,
            DiscoveryPayload::new
    );

    @Override
    public @NotNull Type<DiscoveryPayload> type() {
        return DiscoveryPayload.TYPE;
    }

    @Override
    public String toString() {
        return String.format("DiscoveryPayload{purpose=%s}", versions());
    }

    public byte getVersion(byte key, byte version) {
        if (!this.versions.containsKey(key)) {
            throw new IllegalArgumentException("Versions should contain it's id");
        }
        return (byte) Math.min(version, this.versions.get(key));
    }

    public HashMap<Byte, Byte> cloneVersions() {
        return new HashMap<>(Objects.requireNonNull(versions())); // TODO need clone?
    }
}
