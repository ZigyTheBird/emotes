package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Wrapper class for Emotes play network implementation
 */
public class ModdedServerPlayNetwork extends AbstractServerNetwork implements IServerNetworkInstance {
    @NotNull private final ServerGamePacketListenerImpl serverGamePacketListener;

    @NotNull
    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();



    public ModdedServerPlayNetwork(@NotNull ServerGamePacketListenerImpl serverGamePacketListener) {
        super();
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    protected @NotNull EmotesMixinConnection getServerConnection() {
        return (EmotesMixinConnection) ((ServerCommonPacketListenerAccessor) serverGamePacketListener).getConnection();
    }

    @Override
    public void sendMessage(CustomPacketPayload payload, @Nullable UUID target) {
        if (sendStreamMessage(payload)) {
            return;
        }
        serverGamePacketListener.send(new ClientboundCustomPayloadPacket(payload));
    }

    // TODO isActive

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return emotePlayTracker;
    }
}
