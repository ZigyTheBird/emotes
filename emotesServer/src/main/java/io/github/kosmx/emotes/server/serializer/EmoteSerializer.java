package io.github.kosmx.emotes.server.serializer;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.NBSFileUtils;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Serializing emotes.
 */
public class EmoteSerializer {
    public static void serializeEmotes(UUIDMap<KeyframeAnimation> emotes, Path externalEmotes) {
        try {
            if (!Files.isDirectory(externalEmotes)) {
                return; // Just skip
            }

            Files.walkFileTree(externalEmotes, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (AnimationFormat.byFileName(file.getFileName().toString()).getExtension() != null) {
                        emotes.addAll(serializeExternalEmote(file));
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return dir.equals(externalEmotes) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (Throwable e) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to walk emotes!", e);
        }
    }

    public static List<KeyframeAnimation> serializeExternalEmote(Path file) {
        String fileName = file.getFileName().toString();
        String baseFileName = getBaseName(fileName);

        try (InputStream reader = Files.newInputStream(file)) {
            List<KeyframeAnimation> emotes = UniversalEmoteSerializer.readData(reader, fileName);

            Path icon = file.getParent().resolve(baseFileName + ".png");
            if (Files.isRegularFile(icon)) {
                try (InputStream iconStream = Files.newInputStream(icon)) {
                    final ByteBuffer byteBuffer = MathHelper.readFromIStream(iconStream);

                    for (KeyframeAnimation emote : emotes) { // Avoid lambda
                        emote.extraData.put("iconData", byteBuffer);
                    }
                } catch (Throwable th) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while reading icon: " + icon.getFileName(), th);
                }
            }

            Path song = file.getParent().resolve(baseFileName + ".nbs");
            if (Files.isRegularFile(song) && emotes.size() == 1) {
                try (DataInputStream bis = new DataInputStream(Files.newInputStream(song))) {
                    emotes.getFirst().extraData.put("song", NBSFileUtils.read(bis));
                } catch (Throwable th) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while reading song: " + song.getFileName(), th);
                }
            }

            return emotes;
        } catch (Throwable th) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while importing external emote: " + file.getFileName(), th);
            return Collections.emptyList();
        }
    }

    private static String getBaseName(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileName = fileName.substring(0, i);
        }

        return fileName;
    }
}
