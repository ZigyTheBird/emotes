package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.bukkit.utils.BukkitUnwrapper;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class BukkitNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    private HashMap<Byte, Byte> version = null;
    final Player player;
    final BukkitWrapper bukkitPlugin = BukkitWrapper.getPlugin(BukkitWrapper.class);

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    public BukkitNetworkInstance(Player player){
        this.player = player;
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return version;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        this.version = map;
    }

    @Override
    public void sendMessage(CustomPacketPayload payload, @Nullable UUID target) {
        BukkitUnwrapper.sendPayload(this.player, payload);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    /* @Override
    public void presenceResponse() { TODO
        IServerNetworkInstance.super.presenceResponse();
        for (Player player :bukkitPlugin.getServer().getOnlinePlayers()) {
            if (this.player.canSee(player)) {
                ServerSideEmotePlay.getInstance().playerStartTracking(player, this.player);
            }
        }
    }*/
}
