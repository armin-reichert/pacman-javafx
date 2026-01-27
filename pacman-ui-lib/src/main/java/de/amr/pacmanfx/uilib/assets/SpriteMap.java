/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.math.RectShort;
import org.tinylog.Logger;

import java.util.EnumMap;

import static java.util.Objects.requireNonNull;

/**
 * Map of sprite IDs to sprite data. Values are either:
 * <ul>
 * <li>Single sprites: {@link RectShort}</li>
 * <li>Sprite sequences: {@link RectShort[]}</li>
 * </ul>
 */
public class SpriteMap<SID extends Enum<SID>> {

    sealed interface SpriteData permits SingleSprite, SpriteSequence {}

    record SingleSprite(RectShort rect) implements SpriteData {}

    record SpriteSequence(RectShort[] sequence) implements SpriteData {}

    private final Class<SID> idEnumClass;
    private final EnumMap<SID, SpriteData> spriteDataByID;

    public SpriteMap(Class<SID> idEnumClass) {
        this.idEnumClass = requireNonNull(idEnumClass);
        spriteDataByID = new EnumMap<>(idEnumClass);
    }

    public boolean isEmpty() {
        return spriteDataByID.isEmpty();
    }

    private SpriteData get(SID id) {
        requireNonNull(id);
        SpriteData value = spriteDataByID.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite value is null for id '%s'".formatted(id));
        }
        return value;
    }

    public final RectShort sprite(SID id) {
        SpriteData value = get(id);
        return switch (value) {
            case SingleSprite(RectShort sprite) -> sprite;
            case SpriteSequence ignored -> throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite".formatted(id));
        };
    }

    public final RectShort[] spriteSequence(SID id) {
        SpriteData value = get(id);
        return switch (value) {
            case SingleSprite ignored -> throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite sequence".formatted(id));
            case SpriteSequence spriteSequence -> spriteSequence.sequence;
        };
    }

    public final void add(SID id, RectShort... sprites) {
        requireNonNull(sprites, "Sprite list is null! WTF?");
        if (sprites.length == 1) {
            spriteDataByID.put(id, new SingleSprite(sprites[0]));
        } else {
            spriteDataByID.put(id, new SpriteSequence(sprites));
        }
    }

    public final void checkCompleteness() {
        for (SID id : idEnumClass.getEnumConstants()) {
            if (!spriteDataByID.containsKey(id)) {
                Logger.warn("Found sprite ID without value: {}", id);
            }
        }
    }
}
