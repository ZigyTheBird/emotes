package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.EmotesProxyManager;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.type.HasPlayerPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Client emote proxy manager
 * Responsible for calling proxy instances and other stuff
 */
public final class ClientPacketManager extends EmotesProxyManager {
    private static final INetworkInstance defaultNetwork = PlatformTools.getClientNetworkController();
    //that casting should always work

    public static void init(){
        setManager(new ClientPacketManager()); //Some dependency injection
    }

    private ClientPacketManager(){} //that is a utility class :D

    /**
     *
     * @return Use all network instances even if the server has the mod installed
     */
    private static boolean useAlwaysAlt(){
        return false;
    }

    static void send(CustomPacketPayload payload, UUID target){
        if(!defaultNetwork.isActive() || useAlwaysAlt()){
            for(INetworkInstance network:networkInstances){
                if(network.isActive()){
                    if (target == null || !network.isServerTrackingPlayState()) {
                        if (!defaultNetwork.sendPlayerID() && payload instanceof HasPlayerPayload<?> playerPayload) {
                            defaultNetwork.sendMessage(playerPayload.removePlayerID(), target);

                        } else {
                            defaultNetwork.sendMessage(payload, target);
                        }
                    }
                }
            }
        }
        if(defaultNetwork.isActive() && (target == null || !defaultNetwork.isServerTrackingPlayState())){
            if (!defaultNetwork.sendPlayerID() && payload instanceof HasPlayerPayload<?> playerPayload) {
                defaultNetwork.sendMessage(playerPayload.removePlayerID(), target);

            } else {
                defaultNetwork.sendMessage(payload, target);
            }
        }
    }

    static void receiveMessage(CustomPacketPayload payload, UUID player, INetworkInstance networkInstance){
        try{
            if(payload == null){
                throw new IOException("no valid data");
            }
            if(payload instanceof HasPlayerPayload<?> hasPlayerPayload && !networkInstance.trustReceivedPlayer()){
                payload = hasPlayerPayload.removePlayerID();
            }
            if(payload instanceof HasPlayerPayload<?> hasPlayerPayload && player != null) {
                payload = hasPlayerPayload.setPlayerID(player);
            }
            if(payload instanceof HasPlayerPayload<?> hasPlayerPayload && hasPlayerPayload.getPlayerID() == null){
                //this is not exactly IO but something went wrong in IO so it is IO fail
                throw new IOException("Didn't received any player information");
            }

            try {
                ClientEmotePlay.executeMessage(payload, networkInstance);
            }
            catch (Exception e){//I don't want to break the whole game with a bad message but I'll warn with the highest level
                EmoteInstance.instance.getLogger().log(Level.SEVERE, "Critical error has occurred while receiving emote: " + e.getMessage(), true);
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);

            }

        }
        catch (IOException e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while receiving packet: " + e.getMessage(), true);
            if(EmoteInstance.config.showDebug.get()) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void logMSG(Level level, String msg) {
        EmoteInstance.instance.getLogger().log(level, "[emotes proxy module] " +  msg, level.intValue() >= Level.WARNING.intValue());
    }

    @Override
    protected void dispatchReceive(CustomPacketPayload payload, UUID player, INetworkInstance networkInstance) {
        receiveMessage(payload, player, networkInstance);
    }

    public static boolean isRemoteAvailable(){
        return defaultNetwork.isActive();
    }

    public static boolean isRemoteTracking() {
        return isRemoteAvailable() && defaultNetwork.isServerTrackingPlayState();
    }

    public static boolean isAvailableProxy(){
        for(INetworkInstance instance : networkInstances){
            if(instance.isActive()){
                return true;
            }
        }
        return false;
    }


    /**
     * This shall be invoked when disconnecting from the server
     * @param networkInstance ...
     */
    @Override
    public void onDisconnectFromServer(INetworkInstance networkInstance){
        if(networkInstance == null)throw new NullPointerException("network instance must be non-null");
        EmoteHolder.list.removeIf(emoteHolder -> emoteHolder.fromInstance == networkInstance);
        networkInstance.setVersions(DiscoveryPayload.DEFAULT);
    }
}
