/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.actors;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
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
        var game = (ArcadeMsPacMan_GameModel) gameLevel.game();
        float speed = game.ghostAttackSpeed(gameLevel, this);
        setSpeed(speed);
        if (huntingTimer.phaseIndex() == 0) {
            roam(gameLevel);
        } else {
            boolean chase = huntingTimer.phase() == HuntingPhase.CHASING || game.isCruiseElroyModeActive();
            Vector2i targetTile = chase
                ? chasingTargetTile(gameLevel)
                : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Blinky (red ghost) attacks Pac-Man directly
        return gameLevel.pac().tile();
    }
}
