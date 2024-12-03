package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.util.NetworkHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import io.github.kosmx.emotes.common.network.payloads.type.HasPlayerPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlayerDataPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 1;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public CustomPacketPayload read(ByteBuffer byteBuffer, CustomPacketPayload chain, int version) throws IOException {
        if (chain instanceof HasPlayerPayload<?> config) {
            chain = config.setPlayerID(NetworkHelper.readUUID(byteBuffer));
            boolean isForced = byteBuffer.get() != 0x00;

            if (chain instanceof EmoteStopPayload stop) {
                return new EmoteStopPayload(stop.stopEmoteID(), stop.playerId(), isForced);
            }

            if (chain instanceof EmotePlayPayload play) {
                return new EmotePlayPayload(play.emoteData(), play.tick(), play.playerId(), isForced);
            }
        }
        return chain;
    }

    @Override
    public void write(ByteBuffer byteBuffer, CustomPacketPayload payload) throws IOException {
        if (payload instanceof HasPlayerPayload<?> config) {
            NetworkHelper.writeUUID(byteBuffer, config.getPlayerID());

            if (payload instanceof EmoteStopPayload stop) {
                byteBuffer.put(stop.isForced() ? (byte) 0x01 : (byte) 0x00);
            } else if (payload instanceof EmotePlayPayload play) {
                byteBuffer.put(play.isForced() ? (byte) 0x01 : (byte) 0x00);
            }
        }
    }

    @Override
    public boolean doWrite(CustomPacketPayload config, PacketTask purpose) {
        return config instanceof HasPlayerPayload<?>;
    }

    @Override
    public int calculateSize(CustomPacketPayload config) {
        return 17;//1 UUID = 2 Long = 2*8 bytes = 16 bytes + 1 byte for forced flag
    }
}
