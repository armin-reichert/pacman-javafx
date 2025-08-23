/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Actor;

import static java.util.Objects.requireNonNull;

public class GameLevelMessage extends Actor {

    private final MessageType type;

    public GameLevelMessage(MessageType type) {
        this.type = requireNonNull(type);
    }

    public MessageType type() {
        return type;
    }
}
