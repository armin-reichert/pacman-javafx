/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;

public interface GameEvents {
    void onLevelCompleted(GameLevel gameLevel);
    void onPacKilled(GameLevel gameLevel);
    void onGhostKilled(GameLevel gameLevel, Ghost ghost);
    void onGameEnding(GameLevel gameLevel);
}
