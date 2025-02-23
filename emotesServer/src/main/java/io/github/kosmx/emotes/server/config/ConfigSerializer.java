package io.github.kosmx.emotes.server.config;

import com.google.gson.*;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.lang.reflect.Type;
import java.util.logging.Level;

public class ConfigSerializer implements JsonDeserializer<SerializableConfig>, JsonSerializer<SerializableConfig> {
    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        JsonObject node = json.getAsJsonObject();
        SerializableConfig config = this.newConfig();
        config.configVersion = SerializableConfig.staticConfigVersion;
        if (node.has("config_version"))
            config.configVersion = node.get("config_version").getAsInt();

        if (config.configVersion < SerializableConfig.staticConfigVersion) {
            EmoteInstance.instance.getLogger().log(Level.INFO, "Serializing config with older version.", true);

        } else if (config.configVersion > SerializableConfig.staticConfigVersion) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "You are trying to load version " + config.configVersion + " config. The mod can only load correctly up to v" + SerializableConfig.staticConfigVersion + ". If you won't modify any config, I won't overwrite your config file.", true);
        }

        config.iterate(entry -> deserializeEntry(entry, node));

        return config;
    }

    protected SerializableConfig newConfig() {
        return new SerializableConfig();
    }

    @SuppressWarnings("unchecked")
    private <T> void deserializeEntry(SerializableConfig.ConfigEntry<T> entry, JsonObject node) {
        String id = null;
        if (node.has(entry.getName())) {
            id = entry.getName();

        } else if (node.has(entry.getOldConfigName())) {
            id = entry.getOldConfigName();
        }

        if (id == null)
            return;

        entry.set((T) Serializer.serializer.fromJson(node.get(id), entry.get().getClass()));
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.addProperty("config_version", SerializableConfig.staticConfigVersion); //I always save config with the latest format.
        config.iterate(entry -> node.add(entry.getName(), Serializer.serializer.toJsonTree(entry.get())));
        return node;
    }
}
