package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;

public interface EmotesMixinConnection {
    DiscoveryPayload emotecraft$getRemoteVersions();
    void emotecraft$setVersions(DiscoveryPayload map);
}
