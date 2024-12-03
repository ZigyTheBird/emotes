package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DiscoveryPacket extends AbstractNetworkPacket{
    @Override
    public CustomPacketPayload read(ByteBuffer buf, CustomPacketPayload config, int version){
        //Read these into versions
        int size = buf.getInt();
        HashMap<Byte, Byte> map = new HashMap<>();

        for(int i = 0; i < size; i++){
            byte id = buf.get();
            byte ver = buf.get();
            map.put(id, ver);
        }

        if (config instanceof DiscoveryPayload discovery) {
            return new DiscoveryPayload(
                    map.get(PacketConfig.ANIMATION_FORMAT),
                    discovery.sendPlayerID(),
                    map.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) > 0,
                    map.get(PacketConfig.ALLOW_EMOTE_SYNC) > 0,
                    map.get(PacketConfig.ALLOW_EMOTE_STREAM) > 0 ? Short.MAX_VALUE : 0
            );
        }

        return config;
    }

    @Override
    public void write(ByteBuffer buf, CustomPacketPayload config){
        if (config instanceof DiscoveryPayload discovery) {
            HashMap<Byte, Byte> versions = convert(discovery);
            buf.putInt(versions.size());
            versions.forEach((aByte, integer) -> {
                buf.put(aByte);
                buf.put(integer);
            });
        }
    }

    private HashMap<Byte, Byte> convert(DiscoveryPayload payload) {
        HashMap<Byte, Byte> map = new HashMap<>();
        map.put(PacketConfig.ANIMATION_FORMAT, (byte) payload.animationFormat());
        map.put(PacketConfig.ALLOW_EMOTE_SYNC, (byte) (payload.allowSync() ? 1 : -1));
        map.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte) (payload.doesServerTrackEmotePlay() ? 1 : -1));
        map.put(PacketConfig.ALLOW_EMOTE_STREAM, (byte) (payload.allowStream() ? 1 : -1));

        EmotePacket.defaultVersions.forEach((aByte, bByte) -> {
            if(!map.containsKey(aByte)){
                map.put(aByte, bByte);
            }
        });

        return map;
    }

    @Override
    public byte getID() {
        return 8;
    }

    @Override
    public byte getVer() {
        return 8;
    }

    @Override
    public boolean doWrite(CustomPacketPayload config, PacketTask purpose) {
        return config instanceof DiscoveryPayload;
    }

    @Override
    public int calculateSize(CustomPacketPayload config) {
        if (config instanceof DiscoveryPayload discovery) {
            //every keypair contains 2 bytes + the length
            return convert(discovery).size()*2 + 4;
        }
        return 0;
    }
}
