/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;

import static java.util.Objects.requireNonNull;

public class GameLevelMessage extends Actor {

    private final GameLevelMessageType type;

    public GameLevelMessage(GameLevelMessageType type) {
        this.type = requireNonNull(type);
    }

    public GameLevelMessage(GameLevelMessageType type, Vector2f position) {
        this.type = requireNonNull(type);
        setPosition(requireNonNull(position));
    }

    public GameLevelMessageType type() {
        return type;
    }
}
