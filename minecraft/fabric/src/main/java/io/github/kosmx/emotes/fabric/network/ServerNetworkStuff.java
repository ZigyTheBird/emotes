package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.configuration.ConfigTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;

import java.util.logging.Level;

public final class ServerNetworkStuff {
    public static void init() {
        PayloadTypeRegistator.init();
        CommonServerNetworkHandler.instance.init();

        // Config networking
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, DiscoveryPayload.TYPE)) {
                handler.addTask(new ConfigTask());
            } else {
                EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
            }
        });

        ServerConfigurationNetworking.registerGlobalReceiver(DiscoveryPayload.TYPE, (message, context) -> {
            Connection connection = ((ServerCommonPacketListenerAccessor) context.networkHandler()).getConnection();
            ((EmotesMixinConnection) connection).emotecraft$setVersions(message);
            // TODO send emotes
            context.networkHandler().completeTask(ConfigTask.TYPE); // And, we're done here
        });

        // Play networking
        ServerPlayNetworking.registerGlobalReceiver(EmotePlayPayload.TYPE, (message, context) ->
                CommonServerNetworkHandler.instance.receiveMessage(message, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(EmoteStopPayload.TYPE, (message, context) ->
                CommonServerNetworkHandler.instance.receiveMessage(message, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(GeyserEmotePacket.TYPE, (message, context) ->
                CommonServerNetworkHandler.instance.receiveBEEmote(context.player(), message)
        );
        ServerPlayNetworking.registerGlobalReceiver(StreamPayload.TYPE, (message, context) ->
                CommonServerNetworkHandler.instance.receiveStreamMessage(message, context.player())
        );
    }
}
