package io.github.kosmx.emotes.fabric.executor.types;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.dataTypes.Text;

public class Key implements InputKey {

    final InputConstants.Key storedKey;

    public Key(InputConstants.Key key) {
        this.storedKey = key;
    }

    @Override
    public boolean equals(InputKey key) {
        return this.storedKey.equals(((Key)key).storedKey);
    }

    @Override
    public String getTranslationKey() {
        return storedKey.getName();
    }

    @Override
    public Text getLocalizedText() {
        return new TextImpl(storedKey.getDisplayName().plainCopy());
    }
}
