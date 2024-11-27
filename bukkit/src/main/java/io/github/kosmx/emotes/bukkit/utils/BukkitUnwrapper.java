package io.github.kosmx.emotes.bukkit.utils;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.logging.Level;

public class BukkitUnwrapper {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    public static CustomPacketPayload decodePayload(ResourceLocation id, byte[] bytes) {
        StreamCodec<ByteBuf, ? extends CustomPacketPayload> codec = PLUGIN.payloads.get(id);
        if (codec == null) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to get codec for " + id);
            return null;
        }

        return codec.decode(Unpooled.wrappedBuffer(bytes));
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> ByteBuf encodePayload(T payload) {
        ResourceLocation id = payload.type().id();

        StreamCodec<ByteBuf, T> codec = (StreamCodec<ByteBuf, T>) PLUGIN.payloads.get(id);
        if (codec == null) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to get codec for " + id);
            return null;
        }

        ByteBuf byteBuf = Unpooled.buffer();
        codec.encode(byteBuf, payload);

        return byteBuf;
    }

    public static void sendPayload(Player player, CustomPacketPayload payload) {
        player.sendPluginMessage(PLUGIN, payload.type().id().toString(),
                Objects.requireNonNull(encodePayload(payload)).array()
        );
    }

    public static Packet<?> wrapCustomPayload(Packet<?> packet) {
        if (packet instanceof ServerboundCustomPayloadPacket(CustomPacketPayload payload)) {
            return new ServerboundCustomPayloadPacket(new DiscardedPayload(
                    packet.type().id(), Objects.requireNonNull(encodePayload(payload))
            ));
        }

        if (packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)) {
            return new ClientboundCustomPayloadPacket(new DiscardedPayload(
                    packet.type().id(), Objects.requireNonNull(encodePayload(payload))
            ));
        }

        return packet;
    }

    public static ServerPlayer getNormalPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
