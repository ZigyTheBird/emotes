package io.github.kosmx.emotes.arch.network.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Don't forget to fire events:
 * - on player disconnect
 * - receive message (3x for 3 channels)
 * - handle configuration
 */
public final class ClientNetwork extends AbstractNetworkInstance {
    public static ClientNetwork INSTANCE = new ClientNetwork();

    @Override
    public boolean isActive() {
        return isServerChannelOpen(EmotePlayPayload.TYPE.id());
    }

    @Override
    public void sendMessage(CustomPacketPayload payload, @Nullable UUID target) {
        /*if (target != null) {
            builder.configureTarget(target);
        }*/

        Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(
                new ServerboundCustomPayloadPacket(payload)
        );
    }

    @ExpectPlatform
    @Contract
    public static boolean isServerChannelOpen(ResourceLocation id) {
        throw new AssertionError();
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE - 16; // channel ID is 12, one extra int makes it 16 (string)
        // this way we have 3 byte error
    }
}
