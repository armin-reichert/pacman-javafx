/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Ghost;

public interface GameEvents {
    void onGameEnding();
    void onLevelCompleted();
    void onPacKilled();
    void onGhostKilled(Ghost ghost);
}
