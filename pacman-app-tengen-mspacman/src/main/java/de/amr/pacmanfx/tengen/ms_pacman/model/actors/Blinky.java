/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    public Blinky() {
        super(RED_GHOST_SHADOW, "Blinky");
        reset();
    }

    @Override
    public void onPacKilled(GameLevel level) {}

    //TODO Clarify hunting behavior of Blinky
    @Override
    public void hunt(GameLevel gameLevel, float speed) {
        setSpeed(speed);
        if (gameLevel.huntingTimer().phaseIndex() == 0) {
            roam(gameLevel);
        } else {
            Vector2i targetTile = gameLevel.huntingTimer().phase() == HuntingPhase.CHASING
                ? chasingTargetTile(gameLevel)
                : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        return gameLevel.pac().tile();
    }
}
