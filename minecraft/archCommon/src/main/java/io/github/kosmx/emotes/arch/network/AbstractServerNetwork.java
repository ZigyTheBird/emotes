package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractServerNetwork implements INetworkInstance {
    private EmoteStreamHelper emoteStreamHelper;

    @NotNull
    protected abstract EmotesMixinConnection getServerConnection();

    @Override
    public DiscoveryPayload getRemoteVersions() {
        return getServerConnection().emotecraft$getRemoteVersions();
    }

    @Override
    public void setVersions(DiscoveryPayload map) {
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
    public EmoteStreamHelper getStreamHelper() {
        if (allowStream() && this.emoteStreamHelper == null) {
            this.emoteStreamHelper = new EmoteStreamHelper(maxDataSize());
        }

        return this.emoteStreamHelper;
    }

    @Override
    public void disconnect() {
        if (this.emoteStreamHelper != null) {
            this.emoteStreamHelper.close();
            this.emoteStreamHelper = null;
        }
        setVersions(DiscoveryPayload.DEFAULT);
    }
}
