package io.github.kosmx.emotes.common.network;

import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.StreamPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EmoteStreamHelper implements AutoCloseable {
    /**
     * Maximum payload size after stitching (in bytes)
     */
    public static final int FINAL_PACKET_MAX_SIZE = 8537098; // ~16384 emote frames with full part

    /**
     * Payload codecs that can be streamed
     */
    @SuppressWarnings("unchecked")
    private static final Map<ResourceLocation, StreamCodec<ByteBuf, CustomPacketPayload>> CODECS = Map.ofEntries(
            Map.entry(EmotePlayPayload.TYPE.id(), (StreamCodec<ByteBuf, CustomPacketPayload>) (Object) EmotePlayPayload.STREAM_CODEC),
            Map.entry(EmoteFilePayload.TYPE.id(), (StreamCodec<ByteBuf, CustomPacketPayload>) (Object) EmoteFilePayload.STREAM_CODEC)
    );

    protected final Map<ResourceLocation, ByteBuf> receiveStreams = new HashMap<>();
    protected final int maxPacketSize;

    public EmoteStreamHelper(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
        if (this.maxPacketSize <= 0) {
            throw new IllegalArgumentException();
        }
    }

    public boolean sendMessage(CustomPacketPayload payload, Consumer<StreamPayload> payloadConsumer) {
        if (!CODECS.containsKey(payload.type().id())) {
            return false;
        }

        ByteBuf byteBuf = Unpooled.buffer();
        CODECS.get(payload.type().id()).encode(byteBuf, payload);
        // checkMaxSize(byteBuf, null);

        if (byteBuf.readableBytes() <= this.maxPacketSize) {
            return false;
        }

        System.out.println("Used streaming to send '" + payload + "'!");

        while (byteBuf.readableBytes() > 0) {
            byte[] targetArray = new byte[Math.min(this.maxPacketSize, byteBuf.readableBytes())];
            byteBuf.readBytes(targetArray);

            payloadConsumer.accept(new StreamPayload(payload.type().id(), targetArray, byteBuf.readableBytes() == 0));
        }

        return byteBuf.readableBytes() == 0;
    }

    /**
     * Receive stream data
     * @param payload Received chunk
     * @return null or the complete packet
     */
    @Nullable
    public CustomPacketPayload receiveStream(StreamPayload payload) {
        if (!CODECS.containsKey(payload.id())) {
            return null;
        }

        ByteBuf buf = this.receiveStreams.computeIfAbsent(payload.id(), key -> Unpooled.buffer());
        buf.writeBytes(payload.data());
        checkMaxSize(buf, () -> this.receiveStreams.remove(payload.id()));

        if (payload.last()) {
            CustomPacketPayload readyPayload = CODECS.get(payload.id())
                    .decode(this.receiveStreams.remove(payload.id()));

            System.out.println("Used streaming to receive '" + readyPayload + "'!");
            return readyPayload;
        }

        return null;
    }

    private static void checkMaxSize(ByteBuf buf, Runnable runnable) {
        if (buf.readableBytes() > FINAL_PACKET_MAX_SIZE) {
            if (runnable != null) {
                runnable.run();
            }

            throw new RuntimeException("The maximum size (" + FINAL_PACKET_MAX_SIZE + ") is exceeded!");
        }
    }

    @Override
    public void close() {
        this.receiveStreams.clear();
    }
}
