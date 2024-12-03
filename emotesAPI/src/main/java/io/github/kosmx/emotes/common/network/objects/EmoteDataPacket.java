package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.payloads.EmoteFilePayload;
import io.github.kosmx.emotes.common.network.payloads.EmotePlayPayload;
import io.github.kosmx.emotes.common.network.payloads.type.EmoteDataPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * It should be placed into emotecraftCommon, but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {
    @Override
    public void write(ByteBuffer buf, CustomPacketPayload payload){
        if (payload instanceof EmoteDataPayload config) {
            int version = calculateVersion();
            if (payload instanceof EmotePlayPayload play) {
                buf.putInt(play.tick());
            } else {
                buf.putInt(0);
            }
            AnimationBinary.write(config.emoteData(), buf, version);
        }
    }

    @Override
    public CustomPacketPayload read(ByteBuffer buf, CustomPacketPayload chain, int version) throws IOException {
        try {
            int tick = buf.getInt();
            KeyframeAnimation animation = AnimationBinary.read(buf, version);

            if (chain instanceof EmoteFilePayload) {
                return new EmoteFilePayload(animation);
            }

            if (chain instanceof EmotePlayPayload play) {
                return new EmotePlayPayload(animation, tick, play.playerId(), play.isForced());
            }
        } catch(IOException|RuntimeException e) {
            e.printStackTrace();
        }
        return chain;
    }


    @Override
    public byte getID() {
        return 0;
    }

    /**
     * version 1: 2.1 features, extended parts, UUID emote ID
     * version 2: Animation library, dynamic parts
     * version 3: Animation scale
     */
    @Override
    public byte getVer() {
        return (byte) AnimationBinary.getCurrentVersion();
    }

    protected int calculateVersion() {
        //return Math.min(config.versions.get(getID()), getVer());
        return getVer();
    }

    @Override
    public boolean doWrite(CustomPacketPayload config, PacketTask purpose) {
        return config instanceof EmoteDataPayload;
    }

    /*
    Data types in comment:
    I int, 4 bytes
    L Long 8 bytes (1 uuid = 2 L)
    B byte, ...1 byte
    F float, 4 bytes
     */
    @Override
    public int calculateSize(CustomPacketPayload config) {
        if(!(config instanceof EmoteDataPayload emote))return 0;
        return AnimationBinary.calculateSize(emote.emoteData(), calculateVersion()) + 4;
    }

}
