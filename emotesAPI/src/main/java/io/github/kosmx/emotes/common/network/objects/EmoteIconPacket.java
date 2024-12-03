package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.type.EmoteDataPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class EmoteIconPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0x12;
    }

    @Override
    public byte getVer() {
        return 0x12;
    }

    @Override
    public CustomPacketPayload read(ByteBuffer byteBuffer, CustomPacketPayload chain, int version) throws IOException {
        if (chain instanceof EmoteDataPayload config) {
            int size = byteBuffer.getInt();
            if(size != 0) {
                byte[] bytes = new byte[size];
                byteBuffer.get(bytes);
                config.emoteData().extraData.put("iconData", ByteBuffer.wrap(bytes));
            }
        }
        return chain;
    }

    @Override
    public void write(ByteBuffer byteBuffer, CustomPacketPayload payload) throws IOException {
        if (payload instanceof EmoteDataPayload config) {
            ByteBuffer iconData = (ByteBuffer) config.emoteData().extraData.get("iconData");
            byteBuffer.putInt(iconData.remaining());
            byteBuffer.put(iconData);
            ((Buffer) iconData).position(0);
        }
    }

    @Override
    public boolean doWrite(CustomPacketPayload payload, PacketTask purpose) {
        return purpose == PacketTask.FILE && payload instanceof EmoteDataPayload config && config.emoteData().extraData.containsKey("iconData");
    }

    @Override
    public int calculateSize(CustomPacketPayload payload) {
        if (payload instanceof EmoteDataPayload config) {
            return ((ByteBuffer)config.emoteData().extraData.get("iconData")).remaining() + 4;
        }
        return 0;
    }
}
