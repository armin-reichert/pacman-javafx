/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import static java.util.Objects.requireNonNull;

public class GameLevelMessage {

    private final GameLevelMessageType type;

    public GameLevelMessage(GameLevelMessageType type) {
        this.type = requireNonNull(type);
    }

    public GameLevelMessageType type() {
        return type;
    }
}
