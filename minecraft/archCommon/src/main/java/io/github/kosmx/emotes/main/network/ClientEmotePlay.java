package io.github.kosmx.emotes.main.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClientEmotePlay extends ClientEmoteAPI {

    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    //private static final int maxQueueLength = 256;
    private static final HashMap<UUID, QueueEntry> queue = new HashMap<>();

    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(KeyframeAnimation emote) {
        IEmotePlayerEntity player = TmpGetters.getClientMethods().getMainPlayer();
        if (player.emotecraft$isForcedEmote()) {
            return false;
        }

        ClientPacketManager.send(new EmotePlayPayload(emote, player.emotes_getUUID()), null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emote, player.emotes_getUUID());
        TmpGetters.getClientMethods().getMainPlayer().emotecraft$playEmote(emote, 0, false);
        return true;
    }

    public static void clientRepeatLocalEmote(KeyframeAnimation emote, int tick, UUID target){
        ClientPacketManager.send(new EmotePlayPayload(emote, tick, TmpGetters.getClientMethods().getMainPlayer().emotes_getUUID()), target);
    }

    public static boolean clientStopLocalEmote() {
        if (TmpGetters.getClientMethods().getMainPlayer().isPlayingEmote()) {
            return clientStopLocalEmote(TmpGetters.getClientMethods().getMainPlayer().emotecraft$getEmote().getData());
        }
        return false;
    }

    public static boolean isForcedEmote() {
        IEmotePlayerEntity player = TmpGetters.getClientMethods().getMainPlayer();
        return player.emotecraft$isForcedEmote();
    }

    public static boolean clientStopLocalEmote(KeyframeAnimation emoteData) {
        if (emoteData != null && !TmpGetters.getClientMethods().getMainPlayer().emotecraft$isForcedEmote()) {
            ClientPacketManager.send(new EmoteStopPayload(emoteData.getUuid(), TmpGetters.getClientMethods().getMainPlayer().emotes_getUUID()), null);
            TmpGetters.getClientMethods().getMainPlayer().stopEmote();

            ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
            return true;
        }
        return false;
    }

    static void executeMessage(CustomPacketPayload payload, INetworkInstance networkInstance) throws NullPointerException {
        EmoteInstance.instance.getLogger().log(Level.FINEST, "[emotes client] Received message: " + payload);

        /*if (data.purpose == null) {
            if (EmoteInstance.configuration.showDebug.get()) {
                EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible without a purpose");
            }
        }*/
        switch (Objects.requireNonNull(payload)) {
            case EmotePlayPayload play:
                assert play.emoteData() != null;
                if(play.valid() || !(((ClientConfig)EmoteInstance.config).alwaysValidate.get() || !networkInstance.safeProxy())) {
                    receivePlayPacket(play.emoteData(), play.player(), play.tick(), play.isForced());
                }
                break;
            case EmoteStopPayload stop:
                IEmotePlayerEntity player = stop.playerId().map(PlatformTools::getPlayerFromUUID).orElse(null);
                assert stop.stopEmoteID() != null;
                if(player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(stop.stopEmoteID(), player.emotes_getUUID());
                    player.stopEmote(stop.stopEmoteID());
                    if(player.isMainPlayer() && !stop.isForced()){
                        TmpGetters.getClientMethods().sendChatMessage(Component.translatable("emotecraft.blockedEmote"));
                    }
                }
                else {
                    queue.remove(stop.player());
                }
                break;
            case DiscoveryPayload discovery:
                networkInstance.setVersions(discovery.cloneVersions());
                break;
            case EmoteFilePayload file:
                EmoteHolder.addEmoteToList(file.emoteData()).fromInstance = networkInstance;
            default:
                if (EmoteInstance.config.showDebug.get()) {
                    EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible unknown purpose");
                }
                break;
        }
    }

    static void receivePlayPacket(KeyframeAnimation emoteData, UUID player, int tick, boolean isForced) {
        IEmotePlayerEntity playerEntity = PlatformTools.getPlayerFromUUID(player);
        if(isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, player);
                playerEntity.emotecraft$playEmote(emoteData, tick, isForced);
            }
            else {
                addToQueue(new QueueEntry(emoteData, tick, TmpGetters.getClientMethods().getCurrentTick()), player);
            }
        }
    }

    public static boolean isEmoteAllowed(KeyframeAnimation emoteData, UUID player) {
        return (((ClientConfig)EmoteInstance.config).enablePlayerSafety.get() || !TmpGetters.getClientMethods().isPlayerBlocked(player))
                && (!emoteData.nsfw || ((ClientConfig)EmoteInstance.config).enableNSFW.get());
    }

    static void addToQueue(QueueEntry entry, UUID player) {
        queue.put(player, entry);
    }


    /**
     * @param uuid get emote for this player
     * @return KeyframeAnimation, current tick of the emote
     */
    public static @Nullable
    Pair<KeyframeAnimation, Integer> getEmoteForUUID(UUID uuid) {
        if (queue.containsKey(uuid)) {
            QueueEntry entry = queue.get(uuid);
            KeyframeAnimation emoteData = entry.emoteData;
            int tick = entry.beginTick - entry.receivedTick + TmpGetters.getClientMethods().getCurrentTick();
            queue.remove(uuid);
            if (!emoteData.isPlayingAt(tick)) return null;
            return new Pair<>(emoteData, tick);
        }
        return null;
    }

    /**
     * Call this periodically to keep the queue clean
     */
    public static void checkQueue(){
        int currentTick = TmpGetters.getClientMethods().getCurrentTick();
        queue.forEach((uuid, entry) -> {
            if(!entry.emoteData.isPlayingAt(entry.beginTick + currentTick)
                    && entry.beginTick + currentTick > 0
                    || TmpGetters.getClientMethods().getCurrentTick() - entry.receivedTick > 24000){
                queue.remove(uuid);
            }
        });
    }

    public static void init() {
        ClientEmoteAPI.INSTANCE = new ClientEmotePlay();
    }

    @Override
    protected boolean playEmoteImpl(KeyframeAnimation animation) {
        if (animation != null) {
            return clientStartLocalEmote(animation);
        } else {
            return clientStopLocalEmote();
        }
    }

    @Override
    protected Collection<KeyframeAnimation> clientEmoteListImpl() {
        return EmoteHolder.list.values().stream().map(EmoteHolder::getEmote).collect(Collectors.toList());
    }

    static class QueueEntry{
        final KeyframeAnimation emoteData;
        final int beginTick;
        final int receivedTick;

        QueueEntry(KeyframeAnimation emoteData, int begin, int received) {
            this.emoteData = emoteData;
            this.beginTick = begin;
            this.receivedTick = received;
        }
    }
}
