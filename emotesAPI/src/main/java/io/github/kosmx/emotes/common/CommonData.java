package io.github.kosmx.emotes.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * static channel to access constant from everywhere in the mod.
 * Including Fabric and Bukkit code.
 */
public class CommonData {
    public static boolean isLoaded = false; //to detect if the mod loads twice...

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";

    public static ResourceLocation newIdentifier(String id){
        return ResourceLocation.fromNamespaceAndPath(CommonData.MOD_ID, id);
    }

    public static Component fromJson(Object obj) {
        switch (obj) {
            case String json -> {
                try {
                    return Component.Serializer.fromJson(json, RegistryAccess.EMPTY);
                } catch (JsonParseException e) {
                    return Component.literal(json);
                }
            }

            case JsonElement element -> {
                return Component.Serializer.fromJson(element, RegistryAccess.EMPTY);
            }

            case null, default -> {
                return Component.empty();
            }
        }
    }
}
