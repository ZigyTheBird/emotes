package io.github.kosmx.emotes.bukkit.utils;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BukkitUnwrapper {
    public static ServerPlayer getNormalPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
