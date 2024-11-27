package io.github.kosmx.emotes.bukkit.utils;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

public class BukkitUnwrapper {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> ByteBuf encodeCodec(T payload) {
        ByteBuf byteBuf = Unpooled.buffer();

        ((StreamCodec<ByteBuf, T>) PLUGIN.payloadsToClient.get(payload.type().id()))
                .encode(byteBuf, payload);

        return byteBuf;
    }

    public static void sendPayloadAsPluginMessage(Player player, CustomPacketPayload payload) {
        ByteBuf byteBuf = encodeCodec(payload);

        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array);

        player.sendPluginMessage(PLUGIN, payload.type().id().toString(), array);
    }

    public static Packet<?> wrapPayload(Packet<?> packet) {
        if (packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)) {
            return new ClientboundCustomPayloadPacket(new DiscardedPayload(
                    payload.type().id(), Objects.requireNonNull(encodeCodec(payload))));
        }

        // Serverbound?

        return packet;
    }

    public static ServerPlayer getNormalPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
