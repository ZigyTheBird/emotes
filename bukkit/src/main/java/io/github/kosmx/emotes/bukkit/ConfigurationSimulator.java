package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.bukkit.utils.BukkitUnwrapper;
import io.github.kosmx.emotes.common.network.configuration.ConfigTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLinksSendEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("UnstableApiUsage")
public class ConfigurationSimulator implements Listener {
    private static final VarHandle CONFIGURATION_TASKS;
    private static final MethodHandle FINISH_CURRENT_TASK;

    static {
        try {
            MethodHandles.Lookup lookupIn = MethodHandles.privateLookupIn(ServerConfigurationPacketListenerImpl.class, MethodHandles.lookup());

            CONFIGURATION_TASKS = lookupIn.findVarHandle(ServerConfigurationPacketListenerImpl.class, "configurationTasks", Queue.class);
            FINISH_CURRENT_TASK = lookupIn.findVirtual(ServerConfigurationPacketListenerImpl.class, "finishCurrentTask", MethodType.methodType(void.class, ConfigurationTask.Type.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Map<Player, ServerConfigurationPacketListenerImpl> CONFIGURATION_MAP = new HashMap<>();

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        Set<String> channels = event.getPlayer().getListeningPluginChannels();
        if (channels.isEmpty()) {
            throw new NullPointerException();
        }

        if (!channels.contains(DiscoveryPayload.TYPE.id().toString())) {
            EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
            return;
        }

        ServerPlayer serverPlayer = BukkitUnwrapper.getNormalPlayer(event.getPlayer());
        ServerConfigurationPacketListenerImpl configurationListener = getConfigurationListener(serverPlayer);

        Queue<ConfigurationTask> configurationTasks = (Queue<ConfigurationTask>)
                CONFIGURATION_TASKS.get(configurationListener);

        configurationTasks.add(new ConfigTask());
        CONFIGURATION_MAP.put(event.getPlayer(), configurationListener);
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

    public static void finishCurrentTask(Player player, ConfigurationTask.Type key) {
        try {
            ConfigurationSimulator.FINISH_CURRENT_TASK.invoke(ConfigurationSimulator.CONFIGURATION_MAP.remove(player), key);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
