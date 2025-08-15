/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.RectShort;

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

    private final EnumMap<SID, SpriteData> data;

    public SpriteMap(Class<SID> spriteIdClass) {
        data = new EnumMap<>(spriteIdClass);
    }

    public RectShort sprite(SID id) {
        requireNonNull(id);
        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("Unknown sprite ID '%s'".formatted(id));
        }
        Object value = data.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite value is null for id '%s'".formatted(id));
        }
        if (value instanceof SingleSprite(RectShort rect)) {
            return rect;
        }
        throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite".formatted(id));
    }

    public RectShort[] spriteSequence(SID id) {
        requireNonNull(id);
        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("Unknown sprite ID '%s'".formatted(id));
        }
        Object value = data.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite value is null for id '%s'".formatted(id));
        }
        if (value instanceof SpriteSequence(RectShort[] sequence)) {
            return sequence;
        }
        throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite sequence".formatted(id));
    }

    public void addSprite(SID id, RectShort sprite) {
        data.put(id, new SingleSprite(sprite));
    }

    public void addSpriteSequence(SID id, RectShort... sprites) {
        data.put(id, new SpriteSequence(sprites));
    }
}
