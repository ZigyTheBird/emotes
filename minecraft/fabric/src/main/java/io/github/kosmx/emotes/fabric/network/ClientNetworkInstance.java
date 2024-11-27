package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworkInstance {

    public static void init(){
        // Configuration
        ClientConfigurationNetworking.registerGlobalReceiver(DiscoveryPayload.TYPE, (message, context) -> {
            ClientNetwork.INSTANCE.receiveMessage(message);
            ClientNetwork.INSTANCE.sendC2SConfig(context.responseSender()::sendPacket);
        });
        ClientConfigurationNetworking.registerGlobalReceiver(EmoteFilePayload.TYPE, (message, context) ->
                ClientNetwork.INSTANCE.receiveMessage(message)
        );

        // Play
        C2SPlayChannelEvents.REGISTER.register((handler, sender, minecraft, channels) -> {
            if (channels.contains(DiscoveryPayload.TYPE.id())) {
                ClientNetwork.INSTANCE.configureOnPlay(sender::sendPacket);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNetwork.INSTANCE.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(EmotePlayPayload.TYPE, (message, context) ->
                ClientNetwork.INSTANCE.receiveMessage(message)
        );
        ClientPlayNetworking.registerGlobalReceiver(EmoteStopPayload.TYPE, (message, context) ->
                ClientNetwork.INSTANCE.receiveMessage(message)
        );
        // TODO stream
    }
}
