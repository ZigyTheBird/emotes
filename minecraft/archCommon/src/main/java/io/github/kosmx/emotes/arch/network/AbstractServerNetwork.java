package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractServerNetwork implements INetworkInstance {
    @NotNull
    protected abstract EmotesMixinConnection getServerConnection();

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return getServerConnection().emotecraft$getRemoteVersions();
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        getServerConnection().emotecraft$setVersions(map);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return true; // MC server does track this
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE - 16; // channel ID is 12, one extra int makes it 16 (string)
        // this way we have 3 byte error
    }
}
