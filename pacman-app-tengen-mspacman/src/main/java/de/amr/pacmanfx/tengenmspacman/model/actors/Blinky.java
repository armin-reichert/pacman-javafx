/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

//TODO Clarify hunting behavior of Blinky
public class Blinky extends Ghost {

    public Blinky() {
        super(RED_GHOST_SHADOW, "Blinky");
        reset();

        setHuntingStrategy( (GameLevel gameLevel, Float speed) -> {
            setSpeed(speed);
            if (gameLevel.huntingTimer().phaseIndex() == 0) {
                roam(gameLevel);
            } else {
                final Vector2i targetTile = gameLevel.huntingTimer().phase() == HuntingPhase.CHASING
                    ? chasingTargetTile(gameLevel)
                    : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
                tryMovingTowardsTargetTile(gameLevel, targetTile);
            }
        });
    }
}
