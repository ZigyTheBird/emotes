package io.github.kosmx.emotes.bukkit;

import com.mojang.brigadier.CommandDispatcher;
import io.github.kosmx.emotes.bukkit.executor.BukkitInstance;
import io.github.kosmx.emotes.bukkit.network.BukkitNetworkInstance;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.configuration.ConfigTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.ServerCommands;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class BukkitWrapper extends JavaPlugin implements PluginMessageListener {
    public final Map<ResourceLocation, StreamCodec<ByteBuf, ? extends CustomPacketPayload>> payloadsToClient = new HashMap<>();
    public final Map<ResourceLocation, StreamCodec<ByteBuf, ? extends CustomPacketPayload>> payloadsToServer = new HashMap<>();

    private ServerSideEmotePlay networkPlay = null;

    @Override
    public void onLoad() {
        if(CommonData.isLoaded){
            getLogger().warning("Emotecraft is loaded multiple times, please load it only once!");
            Bukkit.getPluginManager().disablePlugin(this); //disable itself.
        }
        else {
            CommonData.isLoaded = true;
        }
        EmoteInstance.instance = new BukkitInstance(this);
        Serializer.INSTANCE = new Serializer(); //it does register itself
        EmoteInstance.config = Serializer.getConfig();
        UniversalEmoteSerializer.loadEmotes();

        // Config
        this.payloadsToClient.put(EmoteFilePayload.TYPE.id(), EmoteFilePayload.STREAM_CODEC);
        this.payloadsToClient.put(DiscoveryPayload.TYPE.id(), DiscoveryPayload.STREAM_CODEC);
        this.payloadsToServer.put(DiscoveryPayload.TYPE.id(), DiscoveryPayload.STREAM_CODEC);
        // Player
        this.payloadsToClient.put(EmotePlayPayload.TYPE.id(), EmotePlayPayload.STREAM_CODEC);
        this.payloadsToServer.put(EmotePlayPayload.TYPE.id(), EmotePlayPayload.STREAM_CODEC);
        this.payloadsToClient.put(EmoteStopPayload.TYPE.id(), EmoteStopPayload.STREAM_CODEC);
        this.payloadsToServer.put(EmoteStopPayload.TYPE.id(), EmoteStopPayload.STREAM_CODEC);
        // Bedrock
        this.payloadsToServer.put(GeyserEmotePacket.TYPE.id(), GeyserEmotePacket.STREAM_CODEC);
        // Stream
        this.payloadsToClient.put(StreamPayload.TYPE.id(), StreamPayload.STREAM_CODEC);
        this.payloadsToServer.put(StreamPayload.TYPE.id(), StreamPayload.STREAM_CODEC);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                ServerCommands.register((CommandDispatcher) event.registrar().getDispatcher(), true)
        );
    }

    @Override
    public void onEnable() {
        this.networkPlay = new ServerSideEmotePlay(this);

        for (ResourceLocation id : this.payloadsToClient.keySet()) {
            getServer().getMessenger().registerOutgoingPluginChannel(this, id.toString());
        }
        for (ResourceLocation id : this.payloadsToServer.keySet()) {
            getServer().getMessenger().registerIncomingPluginChannel(this, id.toString(), this);
        }

        getServer().getPluginManager().registerEvents(networkPlay, this);
        getServer().getPluginManager().registerEvents(new ConfigurationSimulator(this.payloadsToServer.keySet()), this);

        getLogger().info("Loading Emotecraft as a bukkit plugin...");
    }

    @Override
    public void onDisable() {
        for (ResourceLocation id : this.payloadsToClient.keySet()) {
            getServer().getMessenger().unregisterOutgoingPluginChannel(this, id.toString());
        }
        for (ResourceLocation id : this.payloadsToServer.keySet()) {
            getServer().getMessenger().unregisterIncomingPluginChannel(this, id.toString());
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String type, @NotNull Player player, byte[] bytes) {
        try { // Let the common server logic process the message
            ResourceLocation id = ResourceLocation.tryParse(type);
            if (id == null) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Invalid payload type: " + type);
                return;
            }

            StreamCodec<ByteBuf, ? extends CustomPacketPayload> codec = this.payloadsToServer.get(id);
            if (codec == null) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to get codec for " + id);
                return;
            }

            if (ConfigurationSimulator.CONFIGURATION_MAP.containsKey(player)) { // In configuration phase
                this.networkPlay.player_database.put(player.getUniqueId(), new BukkitNetworkInstance(player));
                // TODO send emotes
                ConfigurationSimulator.finishCurrentTask(player, ConfigTask.TYPE);
            }

            BukkitNetworkInstance networkInstance = this.networkPlay.getPlayerNetworkInstance(player);

            CustomPacketPayload payload = codec.decode(Unpooled.wrappedBuffer(bytes));
            if (payload instanceof StreamPayload streamPayload) {
                this.networkPlay.receiveStreamMessage(streamPayload, player, networkInstance);
                return;
            }

            this.networkPlay.receiveMessage(Objects.requireNonNull(payload), player, networkInstance);
        } catch (Exception e) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to handle message!", e);
            player.kick(Component.text("Failed to handle packet '" + type + "'"));
        }
    }
}
