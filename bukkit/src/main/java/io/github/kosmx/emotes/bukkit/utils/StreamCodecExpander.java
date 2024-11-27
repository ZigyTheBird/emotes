package io.github.kosmx.emotes.bukkit.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class StreamCodecExpander {
    public static void expandMapped(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec, Map<ResourceLocation, StreamCodec<ByteBuf, ? extends CustomPacketPayload>> add) throws ReflectiveOperationException {
        MethodHandles.Lookup lookupIn = MethodHandles.privateLookupIn(codec.getClass(), MethodHandles.lookup());
        VarHandle varHandle = lookupIn.findVarHandle(codec.getClass(), "this$0", StreamCodec.class);

        StreamCodecExpander.expand((StreamCodec<? extends ByteBuf, ? extends Packet<?>>) varHandle.get(codec), add);
    }

    @SuppressWarnings("rawtypes")
    public static void expand(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec, Map<ResourceLocation, StreamCodec<ByteBuf, ? extends CustomPacketPayload>> add) throws ReflectiveOperationException {
        MethodHandles.Lookup lookupIn = MethodHandles.privateLookupIn(codec.getClass(), MethodHandles.lookup());
        VarHandle varHandle = lookupIn.findVarHandle(codec.getClass(), "val$idToType", Map.class);

        Map<ResourceLocation, StreamCodec<? super ByteBuf, ? extends CustomPacketPayload>> idToType =
                new HashMap<>((Map) varHandle.get(codec));

        idToType.putAll(add);

        try {
            varHandle.set(codec, idToType);
        } catch (Throwable e) {
            Field val$idToTypeField = codec.getClass().getDeclaredField("val$idToType");
            val$idToTypeField.setAccessible(true);
            val$idToTypeField.set(codec, idToType);
        }
    }
}
