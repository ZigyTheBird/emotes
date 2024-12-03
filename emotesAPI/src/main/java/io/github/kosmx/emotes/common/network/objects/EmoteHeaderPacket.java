package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.type.EmoteDataPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class EmoteHeaderPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0x11;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public CustomPacketPayload read(ByteBuffer byteBuffer, CustomPacketPayload chain, int version) throws IOException {
        if (chain instanceof EmoteDataPayload config) {
            config.emoteData().extraData.put("name", readString(byteBuffer));
            config.emoteData().extraData.put("description", readString(byteBuffer));
            config.emoteData().extraData.put("author", readString(byteBuffer));
        }
        return chain;
    }

    @Override
    public void write(ByteBuffer byteBuffer, CustomPacketPayload payload) throws IOException {
        if (payload instanceof EmoteDataPayload config) {
            writeString(byteBuffer, (String) config.emoteData().extraData.get("name"));
            writeString(byteBuffer, (String) config.emoteData().extraData.get("description"));
            writeString(byteBuffer, (String) config.emoteData().extraData.get("author"));
        }
    }

    @Override
    public boolean doWrite(CustomPacketPayload payload, PacketTask purpose) {
        return purpose == PacketTask.FILE && payload instanceof EmoteDataPayload;
    }

    @Override
    public int calculateSize(CustomPacketPayload payload) {
        if (!(payload instanceof EmoteDataPayload config)) {
            return 0;
        }

        KeyframeAnimation emote = config.emoteData();
        if (emote == null) return 0;
        return sumStrings((String) emote.extraData.get("name"), (String) emote.extraData.get("description"), (String) emote.extraData.get("author"));
    }

    public static void writeString(ByteBuffer byteBuffer, String s){
        if(s == null){
            byteBuffer.putInt(0);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
    }
    public static String readString(ByteBuffer byteBuffer){
        int len = byteBuffer.getInt();
        if(len == 0)return null;
        byte[] bytes = new byte[len];
        byteBuffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static int sumStrings(String... strings){
        int size = 0;
        for(String s : strings){
            if(s == null) size += 4;
            else size += s.getBytes(StandardCharsets.UTF_8).length + 4;
        }
        return size;
    }
}
