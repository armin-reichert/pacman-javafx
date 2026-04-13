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

    private final Class<SID> idEnumClass;
    private final EnumMap<SID, Object> map;

    public SpriteMap(Class<SID> idEnumClass) {
        this.idEnumClass = requireNonNull(idEnumClass);
        map = new EnumMap<>(idEnumClass);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private Object get(SID id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite map value is null for id '%s'".formatted(id));
        }
        return value;
    }

    public final RectShort sprite(SID id) {
        requireNonNull(id);
        Object value = get(id);
        if (!(value instanceof RectShort))    {
            throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite".formatted(id));
        }
        return (RectShort) value;
    }

    public final RectShort[] spriteSequence(SID id) {
        requireNonNull(id);
        Object value = get(id);
        if (!(value instanceof RectShort[])) {
            throw new IllegalArgumentException("Sprite ID '%s' does not reference a sprite sequence".formatted(id));
        }
        return (RectShort[]) value;
    }

    public final void add(SID id, RectShort... sprites) {
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

    public final void checkCompleteness() {
        for (SID id : idEnumClass.getEnumConstants()) {
            if (!map.containsKey(id)) {
                Logger.warn("Found sprite ID without value: {}", id);
            }
        }
    }
}
