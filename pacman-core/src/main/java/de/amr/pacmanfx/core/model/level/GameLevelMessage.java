/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.level;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.model.actors.Actor;

import static java.util.Objects.requireNonNull;

public class GameLevelMessage extends Actor {

    private final GameLevelMessageType type;

    public GameLevelMessage(GameLevelMessageType type) {
        this.name = "GameLevelMessage";
        this.type = requireNonNull(type);
    }

    public GameLevelMessage(GameLevelMessageType type, Vector2f pos) {
        this.name = "GameLevelMessage";
        this.type = requireNonNull(type);
        position().set(pos);
    }

    public GameLevelMessageType type() {
        return type;
    }
}
