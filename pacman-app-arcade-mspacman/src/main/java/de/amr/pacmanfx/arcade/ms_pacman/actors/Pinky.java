/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.actors;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.PINK_GHOST_SPEEDY;

/**
 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
 * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
 * only the scatter target of Blinky and Pinky would have been affected. Who knows?
 *
 * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
 */
public class Pinky extends Ghost {

    public Pinky() {
        reset();
    }

    @Override
    public String name() {
        return "Pinky";
    }

    @Override
    public byte personality() {
        return PINK_GHOST_SPEEDY;
    }

    @Override
    public void hunt(GameLevel gameLevel, HuntingTimer huntingTimer) {
        float speed = gameLevel.game().ghostAttackSpeed(gameLevel, this);
        setSpeed(speed);
        if (huntingTimer.phaseIndex() == 0) {
            roam(gameLevel);
        } else {
            Vector2i targetTile = huntingTimer.phase() == HuntingPhase.CHASING
                ? chasingTargetTile(gameLevel)
                : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Pinky (pink ghost) ambushes Pac-Man
        return gameLevel.pac().tilesAheadWithOverflowBug(4);
    }
}
