/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.assets;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Map of sprite IDs to sprite data.
 */
public final class SpriteMap<K extends Named> {

    public static SpriteMap<Named> createHashSpriteMap(int initialCapacity) {
        return new SpriteMap<>(new HashMap<>(initialCapacity));
    }

    public static <K extends Enum<K> & Named> SpriteMap<K> createEnumSpriteMap(Class<K> enumType) {
        return new SpriteMap<>(new EnumMap<K, Object>(enumType));
    }

    private final Map<K, Object> entries;

    private SpriteMap(Map<K, Object> entries) {
        this.entries = requireNonNull(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public RectShort[] spriteSequence(K id) {
        requireNonNull(id);
        Object value = get(id);
        if (!(value instanceof RectShort[])) {
            throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite sequence".formatted(id));
        }
        return (RectShort[]) value;
    }

    public void add(K key, RectShort... sprites) {
        requireNonNull(key);
        if (sprites.length == 0) {
            throw new IllegalArgumentException("Sprite list is null! WTF?");
        }
        for (int i = 0; i < sprites.length; ++i) {
            requireNonNull(sprites[i],
                "Sprite list for ID '%s' contains null value at index %d! WTF?".formatted(key, i));
        }
        entries.put(key, sprites.clone());
    }

    private Object get(K key) {
        requireNonNull(key);
        if (!entries.containsKey(key)) {
            throw new IllegalArgumentException("No sprite map value exists for id '%s'".formatted(key));
        }
        return entries.get(key);
    }
}
