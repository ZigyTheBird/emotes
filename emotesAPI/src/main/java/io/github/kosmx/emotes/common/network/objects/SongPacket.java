package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.data.opennbs.NBS;
import dev.kosmx.playerAnim.core.data.opennbs.network.NBSPacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.type.EmoteDataPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SongPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return 3;
    }

    @Override
    public byte getVer() {
        return 1; //Ver0 means NO sound
    }

    @Override
    public CustomPacketPayload read(ByteBuffer byteBuffer, CustomPacketPayload config, int version) throws IOException {
        if (config instanceof EmoteDataPayload emote) {
            NBSPacket reader = new NBSPacket();
            reader.read(byteBuffer);
            emote.emoteData().extraData.put("song", reader.getSong());
        }
        return config;
    }

    @Override
    public void write(ByteBuffer byteBuffer, CustomPacketPayload config) throws IOException {
        if (config instanceof EmoteDataPayload emote) {
            NBSPacket writer = new NBSPacket((NBS) emote.emoteData().extraData.get("song"));
            writer.write(byteBuffer);
        }
    }

    @Override
    public boolean doWrite(CustomPacketPayload config, PacketTask purpose) {
        return config instanceof EmoteDataPayload emote && emote.emoteData().extraData.containsKey("song");
    }

    @Override
    public int calculateSize(CustomPacketPayload config) {
        if (config instanceof EmoteDataPayload emote) {
            Object nbs = emote.emoteData().extraData.get("song");
            if (nbs == null) {
                return 0;
            }
            return NBSPacket.calculateMessageSize((NBS) nbs);
        }
        return 0;
    }
}
