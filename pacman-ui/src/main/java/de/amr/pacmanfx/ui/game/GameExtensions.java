/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class GameExtensions {

    private final Game game;
    private final Map<Identifier, Function<Game, Object>> functionMap = new HashMap<>();
    private final Map<Identifier, Object> resultMap = new HashMap<>();

    public GameExtensions(Game game) {
        this.game = Objects.requireNonNull(game);
    }

    public void add(Identifier id, Function<Game, Object> value) {
        functionMap.put(id, value);
    }

    public void remove(Identifier id) {
        functionMap.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T apply(Identifier id, Class<T> expectedResultType) {
        final Function<Game, Object> function = functionMap.get(id);
        if (function == null) {
            throw new IllegalArgumentException("No extension function registered with id='%s'".formatted(id));
        }
        final Object result = resultMap.computeIfAbsent(id, _ -> function.apply(game));
        if (result == null) {
            throw new IllegalStateException("Extension function (id='%s') produced no result".formatted(id));
        }
        if (!expectedResultType.isInstance(result)) {
            throw new IllegalStateException("Extension function (id='%s') produced result of type '%s', expected type: '%s"
                .formatted(id, result.getClass(), expectedResultType));
        }
        return (T) result;
    }
}
