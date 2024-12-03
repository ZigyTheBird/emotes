package io.github.kosmx.emotes.common.network;

import io.github.kosmx.emotes.common.network.objects.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Send everything emotes mod data...
 */
public class EmotePacket {
    public static final HashMap<Byte, Byte> defaultVersions = new HashMap<>();

    static {
        AbstractNetworkPacket tmp = new EmoteDataPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new PlayerDataPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new DiscoveryPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new StopPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new SongPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new EmoteHeaderPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new EmoteIconPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
    }

    public final NetHashMap subPackets = new NetHashMap();

    public EmotePacket() {
        this.subPackets.put(new EmoteDataPacket());
        this.subPackets.put(new PlayerDataPacket());
        this.subPackets.put(new StopPacket());
        this.subPackets.put(new DiscoveryPacket());
        this.subPackets.put(new SongPacket());
        this.subPackets.put(new EmoteHeaderPacket());
        this.subPackets.put(new EmoteIconPacket());
    }

    //Write packet to a new ByteBuf
    public ByteBuffer write(CustomPacketPayload data) throws IOException {
        PacketTask purpose = PacketTask.getTaskFromPayload(data);
        if(purpose == PacketTask.UNKNOWN)throw new IllegalArgumentException("Can't send packet without any purpose...");
        AtomicReference<Byte> partCount = new AtomicReference<>((byte) 0);
        AtomicInteger sizeSum = new AtomicInteger(6); //5 bytes is the header
        subPackets.forEach((aByte, packet) -> {
            if(packet.doWrite(data, purpose)){
                partCount.getAndSet((byte) (partCount.get() + 1));
                sizeSum.addAndGet(packet.calculateSize(data) + 6); //it's size + the header
            }
        });

        ByteBuffer buf = ByteBuffer.allocate(sizeSum.get());
        buf.putInt(subPackets.get((byte)8).getVer());
        buf.put(purpose.id);
        buf.put(partCount.get());

        AtomicBoolean ex = new AtomicBoolean(false);
        subPackets.forEach((aByte, packet) -> {
            try {
                writeSubPacket(buf, data, purpose, packet);
            } catch (IOException exception) {
                exception.printStackTrace();
                ex.set(true);
            }
        });
        if(ex.get())throw new IOException("Exception while writing sub-packages");
        ((Buffer)buf).flip(); // make it ready to read
        return buf;
    }

    void writeSubPacket(ByteBuffer byteBuffer, CustomPacketPayload data, PacketTask purpose, AbstractNetworkPacket packetSender) throws IOException {
        if(packetSender.doWrite(data, purpose)){
            //This is not time critical task, HeapByteBuf is more secure and I can wrap it again.
            int len = packetSender.calculateSize(data);
            byteBuffer.put(packetSender.getID());
            byteBuffer.put(packetSender.getVer());
            byteBuffer.putInt(len);
            int currentIndex = byteBuffer.position();
            packetSender.write(byteBuffer, data);
            if(byteBuffer.position() != currentIndex + len){
                throw new IOException("Incorrect size calculator: " + packetSender.getClass());
            }
        }
    }

    @Nullable
    public CustomPacketPayload read(ByteBuffer byteBuffer) throws IOException {
        try {
            byteBuffer.getInt(); // Ignore version

            PacketTask purpose = PacketTask.getTaskFromID(byteBuffer.get());
            CustomPacketPayload data = purpose.payload.get();

            byte count = byteBuffer.get();
            for (int i = 0; i < count; i++) {
                byte id = byteBuffer.get();
                byte sub_version = byteBuffer.get();
                int size = byteBuffer.getInt();
                int currentPos = byteBuffer.position();
                if (subPackets.containsKey(id)) {
                    data = subPackets.get(id).read(byteBuffer, data, sub_version);
                    if(data == null){
                        throw new IOException("Invalid " + subPackets.get(id).getClass().getName() + " sub-packet received");
                    }
                    if (byteBuffer.position() != size + currentPos) {
                        ((Buffer)byteBuffer).position(currentPos + size);
                    }
                }
                else {
                    ((Buffer)byteBuffer).position(currentPos + size);
                    //byteBuffer.position(currentPos + size);//Skip unknown sub-packets...
                }
            }
            return data;
        }
        catch (RuntimeException e){
            e.printStackTrace();
            throw new IOException(e.getClass().getTypeName() + " has occurred: " + e.getMessage());
        }
    }
}
