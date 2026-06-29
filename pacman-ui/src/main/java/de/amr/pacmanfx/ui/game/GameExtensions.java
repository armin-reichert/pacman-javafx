/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameExtensions {

    private final Game game;
    private final Map<Identifier, GameExtension> extensionMap = new HashMap<>();
    private final Map<Identifier, Object> values = new HashMap<>();

    public GameExtensions(Game game) {
        this.game = requireNonNull(game);
    }

    public void add(GameExtension extension) {
        requireNonNull(extension);
        extensionMap.put(extension.id(), extension);
    }

    public void remove(Identifier id) {
        requireNonNull(id);
        extensionMap.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T value(Identifier id, Class<T> expectedResultType) {
        final GameExtension extension = extensionMap.get(id);
        if (extension == null) {
            throw new IllegalArgumentException("No extension function registered with id='%s'".formatted(id));
        }
        final Object value = values.computeIfAbsent(id, _ -> extension.creator().apply(game));
        if (value == null) {
            throw new IllegalStateException("Extension function (id='%s') produced no result".formatted(id));
        }
        if (!expectedResultType.isInstance(value)) {
            throw new IllegalStateException("Extension function (id='%s') produced result of type '%s', expected type: '%s"
                .formatted(id, value.getClass(), expectedResultType));
        }
        return (T) value;
    }
}
