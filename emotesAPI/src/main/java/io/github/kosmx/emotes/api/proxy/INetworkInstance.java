package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import javax.annotation.Nullable;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * To hold information about network
 * <p>
 * implement {@link AbstractNetworkInstance} if you want to implement only the necessary functions
 * <p>
 * use this interface if you want to do something completely different
 */
public interface INetworkInstance {
    /**
     * Get the version from the other side. null if default
     * the map doesn't have to contain information about every module. these will be added automatically.
     * <p>
     * do {@code HashMap#put(3, 0)} to disable sound streaming. NBS can't be optimized and often very large
     *
     * @return maybe null
     */
    DiscoveryPayload getRemoteVersions();

    /**
     * Receive (and save) versions from the other side
     * @param payload versions
     */
    void setVersions(DiscoveryPayload payload);

    /**
     * Do send the sender's id to the server
     * @return true means send
     */
    default boolean sendPlayerID(){
        return false;
    }

    /**
     * The Proxy controller ask you to send the message,
     * only if {@link #isActive()} is true
     * @param payload packet payload
     * @param target target to send message, if null, everyone in the view distance
     *               on server-side target will be ignored
     */
    void sendMessage(CustomPacketPayload payload, @Nullable UUID target);

    /**
     * Network instance has received a message, it will send it to EmoteX core to execute
     * you can set your receive event to invoke this
     * there are it's other forms in {@link AbstractNetworkInstance}
     * @param payload received payload
     * @param player player who plays the emote, Can be NULL but only if {@link #trustReceivedPlayer()} is true or message is not play or stop
     */
    default void receiveMessage(CustomPacketPayload payload, UUID player) {
        EmotesProxyManager.receiveMessage(payload, player, this);
    }

    /**
     * Client is sending config message to server. Vanilla clients will answer to the server configuration phase message.
     * This might get invoked multiple times on the same network instance.
     */
    default void sendC2SConfig(Consumer<CustomPacketPayload> consumer) {
    }

    /**
     * when receiving a message, it contains a player. If you don't trust in this information, override this and return false
     *
     * @return false if received info is untrusted
     */
    default boolean trustReceivedPlayer(){
        return true;
    }

    /**
     * If emote validation happens (or can happen at server side)
     * if you return false, Emotecraft will ALWAYS validate the received emote according to the use configuration.
     * @return is the received is validated at server-side
     */
    default boolean safeProxy(){
        return true;
    }

    /**
     * Is the other side is available
     * your send won't be invoke if you return false
     *
     * @return is this channel working
     */
    boolean isActive();

    /**
     * Does the track the emote play state of every player -> true
     * The client has to resend the emote if a new player get close -> false
     */
    boolean isServerTrackingPlayState();

    /**
     * Maximum size of the data what the instance can send
     * <p>
     * {@link AbstractNetworkInstance#maxDataSize()} defaults to {@link Short#MAX_VALUE}
     * @return max size of bytes[]
     */
    int maxDataSize();

    /**
     * If {@link ByteBuffer} is wrapped, it is safe to get the array
     * but if is direct manual read is required.
     * @param byteBuffer get the bytes from
     * @return the byte array
     */
    static byte[] safeGetBytesFromBuffer(ByteBuffer byteBuffer){
        try {
            if (byteBuffer.isDirect() || byteBuffer.isReadOnly()) {
                //not so fast, but there is no other way if the buffer is direct
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                return bytes;
            }
            //Most efficient way.
            else return byteBuffer.array();
        }
        //This shouldn't be able to happen
        catch (BufferOverflowException | BufferUnderflowException e){
            e.printStackTrace();
            return null;
        }
    }
}
