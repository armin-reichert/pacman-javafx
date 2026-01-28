/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;

/**
 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
 * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
 * only the scatter target of Blinky and Pinky would have been affected. Who knows?
 *
 * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
 */
public class Pinky extends de.amr.pacmanfx.arcade.pacman.model.actors.Pinky {

    public Pinky() {}

    @Override
    public void hunt(GameLevel gameLevel, float speed) {
        setSpeed(speed);
        if (gameLevel.huntingTimer().phaseIndex() == 0) {
            // first scatter phase
            roam(gameLevel);
        } else {
            Vector2i targetTile = gameLevel.huntingTimer().phase() == HuntingPhase.CHASING
                ? chasingTargetTile(gameLevel)
                : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }
    }
}