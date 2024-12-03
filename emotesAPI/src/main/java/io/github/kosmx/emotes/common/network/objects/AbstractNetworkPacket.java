package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractNetworkPacket {
    public abstract byte getID();
    public abstract byte getVer();

    /**
     * Read byte buf to T type
     * @param byteBuffer ByteBuffer
     * @param config Reader config
     * @return success
     */
    public abstract CustomPacketPayload read(ByteBuffer byteBuffer, CustomPacketPayload config, int version) throws IOException;

    public abstract void write(ByteBuffer byteBuffer, CustomPacketPayload config) throws IOException;

    public abstract boolean doWrite(CustomPacketPayload config, PacketTask purpose);

    /**
     * Estimated size to create buffers
     * @param config some input data
     * @return the packet's size (estimated)
     */
    public abstract int calculateSize(CustomPacketPayload config);
}
