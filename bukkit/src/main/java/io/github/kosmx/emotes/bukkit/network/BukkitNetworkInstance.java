package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.bukkit.utils.BukkitUnwrapper;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class BukkitNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    final Player player;

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    public BukkitNetworkInstance(Player player){
        this.player = player;
    }

    @Override
    public void sendMessage(CustomPacketPayload payload, @Nullable UUID target) {
        if (sendStreamMessage(payload)) {
            return;
        }

        BukkitUnwrapper.getNormalPlayer(this.player).connection
                .send(new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
