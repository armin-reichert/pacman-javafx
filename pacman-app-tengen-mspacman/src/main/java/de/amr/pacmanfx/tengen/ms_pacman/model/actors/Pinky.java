/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.PINK_GHOST_SPEEDY;

public class Pinky extends Ghost {

    public Pinky() {
        super(PINK_GHOST_SPEEDY, "Pinky");
        reset();
    }

    @Override
    public void hunt(GameLevel gameLevel) {
        //TODO Clarify hunting behavior of Pinky
        float speed = gameLevel.game().ghostAttackSpeed(gameLevel, this);
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
        return gameLevel.pac().tilesAhead(4);
    }
}
