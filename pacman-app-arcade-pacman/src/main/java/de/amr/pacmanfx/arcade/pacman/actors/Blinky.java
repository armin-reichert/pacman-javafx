/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.actors;

import de.amr.pacmanfx.arcade.pacman.Arcade_GameModel;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    public Blinky() {
        reset();
    }

    @Override
    public String name() {
        return "Blinky";
    }

    @Override
    public byte personality() {
        return RED_GHOST_SHADOW;
    }

    @Override
    public void hunt(GameLevel gameLevel, HuntingTimer huntingTimer) {
        // Blinky overrides hunt method to take Cruise Elroy mode into account
        var arcadeGame = (Arcade_GameModel) gameLevel.game();
        boolean chase = huntingTimer.phase() == HuntingPhase.CHASING || arcadeGame.cruiseElroy() > 0;
        Vector2i targetTile = chase
            ? chasingTargetTile(gameLevel)
            : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
        setSpeed(gameLevel.game().ghostAttackSpeed(gameLevel, this));
        tryMovingTowardsTargetTile(gameLevel, targetTile);
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Blinky (red ghost) attacks Pac-Man directly
        return gameLevel.pac().tile();
    }
}
