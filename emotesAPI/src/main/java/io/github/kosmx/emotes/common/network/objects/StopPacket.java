package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.EmoteStopPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.nio.ByteBuffer;
import java.util.UUID;

public class StopPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return 10;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public CustomPacketPayload read(ByteBuffer buf, CustomPacketPayload config, int version) {
        if (config instanceof EmoteStopPayload stop) {
            long msb = buf.getLong();
            long lsb = buf.getLong();
            UUID uuid = new UUID(msb, lsb);

            return new EmoteStopPayload(uuid, stop.playerId(), stop.isForced());
        }

        return config;
    }

    @Override
    public void write(ByteBuffer buf, CustomPacketPayload config) {
        if (config instanceof EmoteStopPayload stopPayload) {
            buf.putLong(stopPayload.stopEmoteID().getMostSignificantBits());
            buf.putLong(stopPayload.stopEmoteID().getLeastSignificantBits());
        }
    }

    @Override
    public boolean doWrite(CustomPacketPayload config, PacketTask purpose) {
        return config instanceof EmoteStopPayload;
    }

    @Override
    public int calculateSize(CustomPacketPayload config) {
        return Long.BYTES*2; //16
    }
}
