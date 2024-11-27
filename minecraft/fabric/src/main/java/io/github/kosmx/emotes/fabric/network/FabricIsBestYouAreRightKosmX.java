package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FabricIsBestYouAreRightKosmX {
    public static void init() {
        // Config
        PayloadTypeRegistry.configurationS2C().register(EmoteFilePayload.TYPE, EmoteFilePayload.STREAM_CODEC);
        PayloadTypeRegistry.configurationS2C().register(DiscoveryPayload.TYPE, DiscoveryPayload.STREAM_CODEC);
        PayloadTypeRegistry.configurationC2S().register(DiscoveryPayload.TYPE, DiscoveryPayload.STREAM_CODEC);

        // Player
        PayloadTypeRegistry.playS2C().register(EmotePlayPayload.TYPE, EmotePlayPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(EmotePlayPayload.TYPE, EmotePlayPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(EmoteStopPayload.TYPE, EmoteStopPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(EmoteStopPayload.TYPE, EmoteStopPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(GeyserEmotePacket.TYPE, GeyserEmotePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(GeyserEmotePacket.TYPE, GeyserEmotePacket.STREAM_CODEC);

        // TODO stream
    }
}
