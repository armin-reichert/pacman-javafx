/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Map of sprite IDs to sprite data. Values are either:
 * <ul>
 * <li>Single sprites: {@link RectShort}</li>
 * <li>Sprite sequences: {@link RectShort[]}</li>
 * </ul>
 */
public class SpriteMap {

    private final Map<Named, Object> map;

    public SpriteMap() {
        map = new HashMap<>();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private Object get(Named id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite map value is null for id '%s'".formatted(id));
        }
        return value;
    }

    public final RectShort sprite(Named id) {
        requireNonNull(id);
        Object value = get(id);
        if (!(value instanceof RectShort))    {
            throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite".formatted(id));
        }
        return (RectShort) value;
    }

    public final RectShort[] spriteSequence(Named id) {
        requireNonNull(id);
        Object value = get(id);
        if (!(value instanceof RectShort[])) {
            throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite sequence".formatted(id));
        }
        return (RectShort[]) value;
    }

    public final void add(Named id, RectShort... sprites) {
        requireNonNull(id);
        if (sprites.length == 0) {
            throw new IllegalArgumentException("Sprite list is null! WTF?");
        }
        for (int i = 0; i < sprites.length; ++i) {
            requireNonNull(sprites[i], "Sprite list for ID '%s' contains null value at index %d! WTF?"
                .formatted(id, i));
        }
        if (sprites.length == 1) {
            map.put(id, sprites[0]);
        } else {
            map.put(id, sprites.clone());
        }
    }
}
