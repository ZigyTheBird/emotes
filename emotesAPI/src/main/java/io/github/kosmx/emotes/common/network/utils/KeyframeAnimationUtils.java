package io.github.kosmx.emotes.common.network.utils;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.NBS;
import dev.kosmx.playerAnim.core.data.opennbs.network.NBSPacket;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class KeyframeAnimationUtils {
    public static final StreamCodec<ByteBuf, KeyframeAnimation> STREAM_CODEC_WITH_HEADER = new StreamCodec<>() {
        @Override
        public @NotNull KeyframeAnimation decode(@NotNull ByteBuf buf) {
            KeyframeAnimation animation = KeyframeAnimationUtils.STREAM_CODEC.decode(buf);

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                    .decode(buf)
                    .ifPresent(name -> animation.extraData.put("name", name));

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                    .decode(buf)
                    .ifPresent(description -> animation.extraData.put("description", description));

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                    .decode(buf)
                    .ifPresent(author -> animation.extraData.put("author", author));

            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY)
                    .decode(buf)
                    .map(ByteBuffer::wrap)
                    .ifPresent(iconData -> animation.extraData.put("iconData", iconData));

            return animation;
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull KeyframeAnimation animation) {
            KeyframeAnimationUtils.STREAM_CODEC.encode(buf, animation);

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, Optional.ofNullable(
                    (String) animation.extraData.get("name")
            ));

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, Optional.ofNullable(
                    (String) animation.extraData.get("description")
            ));

            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, Optional.ofNullable(
                    (String) animation.extraData.get("author")
            ));

            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY).encode(buf, Optional.ofNullable(
                    (ByteBuffer) animation.extraData.get("iconData")
            ).map(INetworkInstance::safeGetBytesFromBuffer));
        }
    };

    public static final StreamCodec<ByteBuf, KeyframeAnimation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull KeyframeAnimation decode(@NotNull ByteBuf buf) {
            int version = ByteBufCodecs.INT.decode(buf);
            int size = ByteBufCodecs.INT.decode(buf);

            byte[] bytes = new byte[size];
            buf.readBytes(bytes);

            KeyframeAnimation animation;
            try {
                animation = AnimationBinary.read(ByteBuffer.wrap(bytes), version);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteBufCodecs.optional(KeyframeAnimationUtils.NBS_STREAM_CODEC)
                    .decode(buf)
                    .ifPresent(nbs -> animation.extraData.put("song", nbs));

            return animation;
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull KeyframeAnimation animation) {
            int version = AnimationBinary.getCurrentVersion();
            ByteBufCodecs.INT.encode(buf, version);

            int size = AnimationBinary.calculateSize(animation, version);
            ByteBufCodecs.INT.encode(buf, size);

            ByteBuffer buffer = ByteBuffer.allocate(size);
            AnimationBinary.write(animation, buffer, version);
            buf.writeBytes(INetworkInstance.safeGetBytesFromBuffer(buffer));

            ByteBufCodecs.optional(KeyframeAnimationUtils.NBS_STREAM_CODEC)
                    .encode(buf, Optional.ofNullable((NBS) animation.extraData.get("song")));
        }
    };

    public static final StreamCodec<ByteBuf, NBS> NBS_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull NBS decode(@NotNull ByteBuf buf) {
            int size = ByteBufCodecs.INT.decode(buf);

            byte[] bytes = new byte[size];
            buf.readBytes(bytes);

            NBSPacket reader = new NBSPacket();
            try {
                reader.read(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return reader.getSong();
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull NBS nbs) {
            int size = NBSPacket.calculateMessageSize(nbs);
            ByteBufCodecs.INT.encode(buf, size);

            NBSPacket writer = new NBSPacket(nbs);

            ByteBuffer buffer = ByteBuffer.allocate(size);
            writer.write(buffer);
            buf.writeBytes(INetworkInstance.safeGetBytesFromBuffer(buffer));
        }
    };
}
