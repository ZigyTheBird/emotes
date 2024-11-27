package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implement this if you want to act as a proxy for EmoteX
 * This has most of the functions implemented as you might want, but you can override any.
 */
public abstract class AbstractNetworkInstance implements INetworkInstance{

    //Notable version parameters
    protected int remoteVersion = 0;
    protected boolean disableNBS = false;
    protected boolean doesServerTrackEmotePlay = false;

    protected int animationFormat = 1;

    /*
     * You have to implement at least one of these three functions
     * EmoteX packet (PacketBuilder) -> ByteBuffer -> byte[]
     */

    /**
     * If you want to send byte array
     * <p>
     * You can wrap bytes to Netty
     * {@code Unpooled.wrappedBuffer(bytes)}
     * or to Minecraft's PacketByteBuf (yarn mappings) / FriendlyByteBuf (official mappings)
     * {@code new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))}
     *
     * @param payload payload to send
     * @param target target to send message, if null, everyone in the view distance
     */
    public void sendMessage(CustomPacketPayload payload, @Nullable UUID target){
        //If code here were invoked, you have made a big mistake.
        throw new UnsupportedOperationException("You should have implemented send emote feature");
    }

    /**
     * Receive message, but you don't know who sent this
     * The bytes data has to contain the identity of the sender
     * {@link #trustReceivedPlayer()} should return true as you don't have your own identifier system as alternative
     * @param payload message payload
     */
    public void receiveMessage(CustomPacketPayload payload){
        this.receiveMessage(payload, null);
    }

    /**
     * When the network instance disconnects...
     */
    protected void disconnect(){
        EmotesProxyManager.disconnectInstance(this);
    }

    /**
     * If {@link ByteBuffer} is wrapped, it is safe to get the array
     * but if is direct manual read is required.
     * @param byteBuffer get the bytes from
     * @return the byte array
     */
    public static byte[] safeGetBytesFromBuffer(ByteBuffer byteBuffer){
        return INetworkInstance.safeGetBytesFromBuffer(byteBuffer);
    }

    /**
     * Default client-side version configuration,
     * Please call super if you override it.
     * @param map version/configuration map
     */
    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        if (map.containsKey((byte) 3)) {
            disableNBS = map.get((byte) 3) == 0;
        }
        if (map.containsKey((byte) 8)) {
            remoteVersion = map.get((byte) 8); //8x8 :D
        }
        if (map.containsKey(PacketConfig.SERVER_TRACK_EMOTE_PLAY)) {
            this.doesServerTrackEmotePlay = map.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
        }
        if (map.containsKey((byte) 0)) {
            animationFormat = map.get((byte) 0);
        }
    }

    /**
     * see {@link INetworkInstance#getRemoteVersions()}
     * it is just a default implementation
     */
    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        HashMap<Byte, Byte> map = new HashMap<>();
        if(disableNBS){
            map.put(PacketConfig.NBS_CONFIG, (byte) 0);
        }
        if (doesServerTrackEmotePlay) {
            map.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)1);
        }
        map.put(PacketConfig.ANIMATION_FORMAT, (byte)this.animationFormat);
        return map;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return this.doesServerTrackEmotePlay;
    }

    @Override
    public void sendC2SConfig(Consumer<CustomPacketPayload> consumer) { // TODO
        consumer.accept(new DiscoveryPayload(getRemoteVersions()));
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE;
    }
}
