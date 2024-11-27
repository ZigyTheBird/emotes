package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinConnection;
import io.github.kosmx.emotes.common.network.payloads.DiscoveryPayload;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public class ConnectionHandlerMixin implements EmotesMixinConnection {
    @Unique
    private DiscoveryPayload emotecraft$versions;

    @Override
    public DiscoveryPayload emotecraft$getRemoteVersions() {
        return this.emotecraft$versions;
    }

    @Override
    public void emotecraft$setVersions(DiscoveryPayload payload) {
        this.emotecraft$versions = payload;
    }
}
