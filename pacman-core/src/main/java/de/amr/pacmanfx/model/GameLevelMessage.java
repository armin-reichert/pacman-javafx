/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;

import static java.util.Objects.requireNonNull;

public class GameLevelMessage extends Actor {

    private final MessageType type;

    public GameLevelMessage(MessageType type) {
        this.type = requireNonNull(type);
    }

    public GameLevelMessage(MessageType type, Vector2f position) {
        this.type = requireNonNull(type);
        setPosition(requireNonNull(position));
    }

    public MessageType type() {
        return type;
    }
}
