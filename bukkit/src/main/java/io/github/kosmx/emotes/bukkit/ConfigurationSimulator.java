package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.bukkit.utils.BukkitUnwrapper;
import io.github.kosmx.emotes.common.network.configuration.ConfigTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLinksSendEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Level;

@SuppressWarnings("UnstableApiUsage")
public class ConfigurationSimulator implements Listener {
    private static final VarHandle CONFIGURATION_TASKS;

    static {
        try {
            CONFIGURATION_TASKS = MethodHandles.privateLookupIn(ServerConfigurationPacketListenerImpl.class, MethodHandles.lookup()).findVarHandle(
                    ServerConfigurationPacketListenerImpl.class, "configurationTasks", Queue.class
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        if (!event.getPlayer().getListeningPluginChannels().contains(DiscoveryPayload.TYPE.id().toString())) {
            EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
            return;
        }

        ServerPlayer serverPlayer = BukkitUnwrapper.getNormalPlayer(event.getPlayer());
        ServerConfigurationPacketListenerImpl configurationListener = getConfigurationListener(serverPlayer);

        Queue<ConfigurationTask> configurationTasks = (Queue<ConfigurationTask>)
                CONFIGURATION_TASKS.get(configurationListener);

        configurationTasks.add(new ConfigTask() {
            @Override
            public void start(@NotNull Consumer<Packet<?>> consumer) {
                super.start(packet -> consumer.accept(BukkitUnwrapper.wrapCustomPayload(packet)));
            }
        });
    }

    private static ServerConfigurationPacketListenerImpl getConfigurationListener(ServerPlayer serverPlayer) {
        List<Connection> connections = Objects.requireNonNull(serverPlayer.getServer()).getConnection().getConnections();

        for (Connection connection : connections) {
            if (!(connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl configurationListener)) {
                continue;
            }

            if (connection.getPlayer().equals(serverPlayer)) {
                return configurationListener;
            }
        }

        throw new NullPointerException("Failed to find a configuration listener!");
    }
}
