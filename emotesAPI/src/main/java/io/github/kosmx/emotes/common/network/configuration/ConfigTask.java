package io.github.kosmx.emotes.common.network.configuration;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ConfigTask implements ConfigurationTask {
    public static final Type TYPE = new Type("emotes:config");

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) {
        consumer.accept(new ClientboundCustomPayloadPacket(new DiscoveryPayload(
                AnimationBinary.getCurrentVersion(), // Animator version
                true,  // track player state
                true, // Songs enabled
                true, // Allow emote stream
                true // Aloow emote sync
        )));
    }

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
