package io.github.kosmx.emotes.arch.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Contract;

public final class NetworkPlatformTools {
    @ExpectPlatform
    @Contract // contract to fix flow analysis.
    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Contract
    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl player, ResourceLocation channel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static MinecraftServer getServer() {
        throw new AssertionError();
    }
}
