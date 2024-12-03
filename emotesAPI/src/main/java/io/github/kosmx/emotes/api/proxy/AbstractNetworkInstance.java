package io.github.kosmx.emotes.api.proxy;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implement this if you want to act as a proxy for EmoteX
 * This has most of the functions implemented as you might want, but you can override any.
 */
public abstract class AbstractNetworkInstance implements INetworkInstance{
    protected DiscoveryPayload discoveryPayload = DiscoveryPayload.DEFAULT;
    private EmoteStreamHelper emoteStreamHelper;

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

    public void receiveStreamMessage(StreamPayload payload){
        this.receiveStreamMessage(payload, null);
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
    @Override
    public void disconnect(){
        INetworkInstance.super.disconnect();
        if (this.emoteStreamHelper != null) {
            this.emoteStreamHelper.close();
            this.emoteStreamHelper = null;
        }
        setVersions(DiscoveryPayload.DEFAULT);
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
     * @param payload version/configuration map
     */
    @Override
    public void setVersions(DiscoveryPayload payload) {
        this.discoveryPayload = payload;
    }

    /**
     * see {@link INetworkInstance#getRemoteVersions()}
     * it is just a default implementation
     */
    @Override
    public DiscoveryPayload getRemoteVersions() {
        return this.discoveryPayload;
    }

    @Override
    public void sendC2SConfig(Consumer<CustomPacketPayload> consumer) {
        consumer.accept(new DiscoveryPayload(
                AnimationBinary.getCurrentVersion(),
                sendPlayerID(),
                isServerTrackingPlayState(),
                this.discoveryPayload.allowSync(),
                maxDataSize()
        ));
    }

    @Override
    public EmoteStreamHelper getStreamHelper() {
        if (allowStream() && this.emoteStreamHelper == null) {
            this.emoteStreamHelper = new EmoteStreamHelper(maxDataSize());
        }

        return this.emoteStreamHelper;
    }
}
