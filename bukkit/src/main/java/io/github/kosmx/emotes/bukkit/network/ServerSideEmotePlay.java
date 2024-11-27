package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ServerSideEmotePlay extends AbstractServerEmotePlay<Player> implements Listener {
    public final HashMap<UUID, BukkitNetworkInstance> player_database = new HashMap<>();
    private final BukkitWrapper plugin;

    public ServerSideEmotePlay(BukkitWrapper plugin){
        this.plugin = plugin;
    }

    @Override
    public UUID getUUIDFromPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    public Player getPlayerFromUUID(UUID player) {
        return plugin.getServer().getPlayer(player);
    }

    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getEntityId();
    }

    @Override
    public BukkitNetworkInstance getPlayerNetworkInstance(Player player) {
        UUID playerUuid = getUUIDFromPlayer(player);
        if (!player_database.containsKey(playerUuid)) {
            EmoteInstance.instance.getLogger().log(Level.INFO, "Player " + player.getName() + " never joined. If it is a fake player, the fake-player plugin forgot to fire join event.");
            player_database.put(playerUuid, new BukkitNetworkInstance(player));
        }
        return player_database.get(playerUuid);
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) {
        if (!player_database.containsKey(player)) return getPlayerNetworkInstance(getPlayerFromUUID(player));
        return this.player_database.get(player);
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    sendForPlayer(packet, player1, getUUIDFromPlayer(player1));
                }catch (Exception e){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void sendForEveryoneElse(CustomPacketPayload payload, GeyserEmotePacket emotePacket, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    //Bukkit server will filter if I really can send, or not.
                    //If else to not spam dumb forge clients.
                    if(player1.getListeningPluginChannels().contains(payload.type().id().toString())) {
                        sendForPlayer(payload, player1, getUUIDFromPlayer(player1));
                    }
                    else if(emotePacket != null) sendForPlayer(emotePacket, player1, getUUIDFromPlayer(player1));
                }catch (Exception e){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(CustomPacketPayload payload, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        if (targetPlayer == null) return;
        if(targetPlayer.canSee(player)){
            sendForPlayer(payload, player, target);
        }
    }

    @Override
    protected void sendForPlayer(CustomPacketPayload payload, Player player, UUID target) {
        IServerNetworkInstance targetPlayer = getPlayerNetworkInstance(target);
        try {
            // TODO multiversion
            targetPlayer.sendMessage(payload, null);
        }catch (Exception e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();

        BukkitNetworkInstance instance = this.player_database.remove(player.getUniqueId());
        if(instance != null)instance.closeConnection();
    }

    @EventHandler
    public void playerDies(EntityPoseChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Pose pose = event.getPose();
            if (pose == Pose.SNEAKING || pose == Pose.DYING || pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SLEEPING) {
                playerEntersInvalidPose((Player) event.getEntity());
            }
        }
    }
}
