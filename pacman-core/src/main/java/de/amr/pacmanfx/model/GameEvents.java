/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;

public interface GameEvents {
    void onPelletEaten();
    void onEnergizerEaten(Vector2i tile);
    void onLevelCompleted(GameLevel gameLevel);
    void onPacKilled();
    void onGhostKilled(Ghost ghost);
    void onGameEnding();
}
