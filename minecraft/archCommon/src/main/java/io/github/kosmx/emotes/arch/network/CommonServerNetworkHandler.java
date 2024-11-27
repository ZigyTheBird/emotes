package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerChunkCacheAccessor;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<Player> {
    public static CommonServerNetworkHandler instance = new CommonServerNetworkHandler();

    private CommonServerNetworkHandler() {} // make ctor private for singleton class

    public void init() {
    }

    public void receiveMessage(CustomPacketPayload payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(payload, player, getHandler(serverPlayer.connection));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static IServerNetworkInstance getHandler(ServerGamePacketListenerImpl handler) {
        return ((EmotesMixinNetwork)handler).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected Player getPlayerFromUUID(UUID player) {
        return NetworkPlatformTools.getServer().getPlayerList().getPlayer(player);
    }

    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getId();
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player sourcePlayer) {
        if (!(sourcePlayer instanceof ServerPlayer player)) {
            return null;
        }

        return ((EmotesMixinNetwork)player.connection).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        sendForEveryoneElse(null, packet, player); // don't make things complicated
    }

    @Override
    protected void sendForEveryoneElse(CustomPacketPayload payload, @Nullable GeyserEmotePacket geyserPacket, Player player) {
        getTrackedPlayers(player).forEach(target -> {
            if (target != player) {
                try {
                    if (payload != null && NetworkPlatformTools.canSendPlay(target, payload.type().id())) {
                        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                        playerNetwork.sendMessage(payload, null);
                    } else if (geyserPacket != null && NetworkPlatformTools.canSendPlay(target, geyserPacket.type().id())) {
                        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                        playerNetwork.sendGeyserPacket(geyserPacket);
                    }
                } catch (IOException e) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
    }

    @Override
    protected void sendForPlayerInRange(CustomPacketPayload payload, Player player, UUID target) {
        if (!(player instanceof ServerPlayer sourcePlayer)) {
            return;
        }

        var targetPlayer = sourcePlayer.server.getPlayerList().getPlayer(target);
        if (targetPlayer != null && targetPlayer.getChunkTrackingView().contains(sourcePlayer.chunkPosition())) {
            getPlayerNetworkInstance(targetPlayer).sendMessage(payload, null);
        }
    }

    @Override
    protected void sendForPlayer(CustomPacketPayload payload, Player ignore, UUID target) {
        Player player = getPlayerFromUUID(target);
        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(player);

        playerNetwork.sendMessage(payload, null);
    }

    private Collection<ServerPlayer> getTrackedPlayers(Entity entity) {
        var level = entity.level().getChunkSource();
        if (level instanceof ServerChunkCache chunkCache) {
            ServerChunkCacheAccessor storage = (ServerChunkCacheAccessor) chunkCache.chunkMap;

            var tracker = storage.getTrackedEntity().get(entity.getId());
            if (tracker != null) {
                return tracker.getPlayersTracking()
                        .stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
            }
            return Collections.emptyList();
        }
        throw new IllegalArgumentException("server function called on logical client");
    }
}
