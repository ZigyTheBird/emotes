package io.github.kosmx.emotes.neoforge.network;

import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.configuration.ConfigTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;

import java.util.logging.Level;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CommonData.MOD_ID)
public class ForgeNetwork {
    @SubscribeEvent
    public static void registerPlay(final RegisterPayloadHandlersEvent event) {
        event.registrar(CommonData.MOD_ID).optional()
                // Config
                .configurationBidirectional(DiscoveryPayload.TYPE, DiscoveryPayload.STREAM_CODEC, new DirectionalPayloadHandler<>(
                        (message, context) -> {
                            ClientNetwork.INSTANCE.receiveMessage(message);
                            ClientNetwork.INSTANCE.sendC2SConfig(context::reply);
                        },
                        (message, context) -> {
                            ((EmotesMixinConnection) context.connection()).emotecraft$setVersions(message);
                            // TODO send emotes
                            context.finishCurrentTask(ConfigTask.TYPE); // And, we're done here
                        }
                ))
                .configurationToClient(EmoteFilePayload.TYPE, EmoteFilePayload.STREAM_CODEC,
                        (message, context) -> ClientNetwork.INSTANCE.receiveMessage(message)
                )

                // Player
                .playBidirectional(EmotePlayPayload.TYPE, EmotePlayPayload.STREAM_CODEC, new DirectionalPayloadHandler<>(
                        (message, context) -> ClientNetwork.INSTANCE.receiveMessage(message),
                        (message, context) -> CommonServerNetworkHandler.instance.receiveMessage(message, context.player())
                ))
                .playBidirectional(EmoteStopPayload.TYPE, EmoteStopPayload.STREAM_CODEC, new DirectionalPayloadHandler<>(
                        (message, context) -> ClientNetwork.INSTANCE.receiveMessage(message),
                        (message, context) -> CommonServerNetworkHandler.instance.receiveMessage(message, context.player())
                ))

                // Bedrock
                .playToServer(GeyserEmotePacket.TYPE, GeyserEmotePacket.STREAM_CODEC,
                        (message, context) -> CommonServerNetworkHandler.instance.receiveBEEmote(context.player(), message)
                )

                // Stream
                .playBidirectional(StreamPayload.TYPE, StreamPayload.STREAM_CODEC, new DirectionalPayloadHandler<>(
                        (message, context) -> ClientNetwork.INSTANCE.receiveStreamMessage(message),
                        (message, context) -> CommonServerNetworkHandler.instance.receiveStreamMessage(message, context.player())
                ));
    }

    @SubscribeEvent
    public static void registerNetworkConfigTask(final RegisterConfigurationTasksEvent event) {
        if (event.getListener().hasChannel(DiscoveryPayload.TYPE)) {
            event.register(new ConfigTask());
        } else {
            EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
        }
    }
}
