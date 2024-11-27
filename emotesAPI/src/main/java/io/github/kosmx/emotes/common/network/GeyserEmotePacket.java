package io.github.kosmx.emotes.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GeyserEmotePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GeyserEmotePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath("geyser", "emote")
    );

    public static final StreamCodec<ByteBuf, GeyserEmotePacket> STREAM_CODEC = CustomPacketPayload.codec(
            GeyserEmotePacket::write, GeyserEmotePacket::read
    );

    private long runtimeEntityID;
    private UUID emoteID;

    private static GeyserEmotePacket read(ByteBuf buf) {
        GeyserEmotePacket packet = new GeyserEmotePacket();

        byte[] str = new byte[buf.readInt()];
        buf.readBytes(str);
        packet.setEmoteID(UUID.fromString(new String(str, StandardCharsets.UTF_8)));

        packet.setRuntimeEntityID(buf.readLong());

        return packet;
    }

    private void write(ByteBuf buf) {
        byte[] bytes = emoteID.toString().getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeLong(runtimeEntityID);
    }

    public long getRuntimeEntityID() {
        return runtimeEntityID;
    }

    public void setEmoteID(UUID emoteID) {
        this.emoteID = emoteID;
    }

    public UUID getEmoteID() {
        return emoteID;
    }

    public void setRuntimeEntityID(long runtimeEntityID) {
        this.runtimeEntityID = runtimeEntityID;
    }

    @Override
    public @NotNull Type<GeyserEmotePacket> type() {
        return GeyserEmotePacket.TYPE;
    }
}
