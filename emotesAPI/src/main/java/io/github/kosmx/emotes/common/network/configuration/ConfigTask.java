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

    private final boolean doesServerTrackEmotePlay;
    private final int maxDataSize;

    public ConfigTask(boolean doesServerTrackEmotePlay, int maxDataSize) {
        this.doesServerTrackEmotePlay = doesServerTrackEmotePlay;
        this.maxDataSize = maxDataSize;
    }

    public ConfigTask(int maxDataSize) {
        this(true, maxDataSize);
    }

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) {
        consumer.accept(new ClientboundCustomPayloadPacket(new DiscoveryPayload(
                AnimationBinary.getCurrentVersion(), // Animator version
                false, // send player uuid?
                this.doesServerTrackEmotePlay,  // track player state
                true, // Aloow emote sync?
                this.maxDataSize
        )));
    }

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
