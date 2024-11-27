package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;

import java.io.IOException;

public interface IServerNetworkInstance extends INetworkInstance {


    /**
     * Server closes connection with instance
     */
    default void closeConnection(){}

    default boolean trackPlayState() {
        return true;
    }

    EmotePlayTracker getEmoteTracker();

    default void sendGeyserPacket(GeyserEmotePacket packet) throws IOException {
        sendMessage(packet, null);
    }
}
