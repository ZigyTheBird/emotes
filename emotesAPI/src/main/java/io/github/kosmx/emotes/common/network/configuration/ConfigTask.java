package io.github.kosmx.emotes.common.network.configuration;

import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

public class ConfigTask implements ConfigurationTask {
    public static final Type TYPE = new Type("emotes:configuration");

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) { // TODO
        DiscoveryPayload discovery = new DiscoveryPayload(new HashMap<>());
        discovery.versions().put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)0x01); // track player state
        consumer.accept(new ClientboundCustomPayloadPacket(discovery)); // Config init
    }

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
