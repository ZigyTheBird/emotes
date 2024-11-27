package io.github.kosmx.emotes.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import io.github.kosmx.emotes.bukkit.executor.BukkitInstance;
import io.github.kosmx.emotes.bukkit.network.BukkitNetworkInstance;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.bukkit.utils.BukkitUnwrapper;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.ServerCommands;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
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
import java.util.UUID;
import java.util.logging.Level;

public class BukkitWrapper extends JavaPlugin implements PluginMessageListener {
    public final Map<ResourceLocation, StreamCodec<ByteBuf, ? extends CustomPacketPayload>> payloads = new HashMap<>();
    private ServerSideEmotePlay networkPlay = null;
    private ProtocolManager protocolManager;

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
        protocolManager = ProtocolLibrary.getProtocolManager();
        registerProtocolListener();

        this.payloads.put(DiscoveryPayload.TYPE.id(), DiscoveryPayload.STREAM_CODEC);
        this.payloads.put(EmotePlayPayload.TYPE.id(), EmotePlayPayload.STREAM_CODEC);
        this.payloads.put(EmoteStopPayload.TYPE.id(), EmoteStopPayload.STREAM_CODEC);
        this.payloads.put(EmoteFilePayload.TYPE.id(), EmoteFilePayload.STREAM_CODEC);
        this.payloads.put(StreamPayload.TYPE.id(), StreamPayload.STREAM_CODEC);
        this.payloads.put(GeyserEmotePacket.TYPE.id(), GeyserEmotePacket.STREAM_CODEC);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                ServerCommands.register((CommandDispatcher) event.registrar().getDispatcher(), true)
        );
    }

    @Override
    public void onEnable() {
        this.networkPlay = new ServerSideEmotePlay(this);

        for (ResourceLocation id : this.payloads.keySet()) {
            getServer().getMessenger().registerOutgoingPluginChannel(this, id.toString());
            getServer().getMessenger().registerIncomingPluginChannel(this, id.toString(), this);
        }

        getServer().getPluginManager().registerEvents(networkPlay, this);
        getServer().getPluginManager().registerEvents(new ConfigurationSimulator(), this);

        getLogger().info("Loading Emotecraft as a bukkit plugin...");
    }

    @Override
    public void onDisable() {
        for (ResourceLocation id : this.payloads.keySet()) {
            getServer().getMessenger().unregisterOutgoingPluginChannel(this, id.toString());
            getServer().getMessenger().unregisterIncomingPluginChannel(this, id.toString());
        }
    }

    public void registerProtocolListener() {
        PacketType packetType = MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove() ?
                PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.NAMED_ENTITY_SPAWN;

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, packetType) {
            @Override
            public void onPacketSending(PacketEvent packetEvent) {
                if (packetEvent.getPacketType().equals(packetType)) {
                    //Field trackedField = packetEvent.getPacket().getStructures().getField(2);
                    UUID tracked = packetEvent.getPacket().getUUIDs().readSafely(0);

                    AbstractServerEmotePlay.getInstance().playerStartTracking(BukkitWrapper.this.networkPlay.getPlayerFromUUID(tracked), packetEvent.getPlayer());

                }
            }

            @Override
            public void onPacketReceiving(PacketEvent packetEvent) {

            }
        });
    }

    @Override
    public void onPluginMessageReceived(@NotNull String type, @NotNull Player player, byte[] bytes) {
        try { // Let the common server logic process the message
            ResourceLocation id = ResourceLocation.tryParse(type);
            if (id == null) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Invalid payload type: " + type);
                return;
            }

            BukkitNetworkInstance playerNetwork = this.networkPlay.getPlayerNetworkInstance(player);
            if (playerNetwork == null) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player.getName() + " is not registered");
                return;
            }

            this.networkPlay.receiveMessage(Objects.requireNonNull(
                    BukkitUnwrapper.decodePayload(id, bytes)
            ), player, playerNetwork);
        } catch (Exception e) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to handle message!", e);
            player.kick(Component.text("Failed to decode packet '" + type + "'"));
        }
    }
}
