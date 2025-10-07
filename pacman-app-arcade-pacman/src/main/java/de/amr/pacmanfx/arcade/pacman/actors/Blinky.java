/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.actors;

import de.amr.pacmanfx.arcade.pacman.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.Arcade_LevelData;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    private int cruiseElroy;

    protected Blinky() {
        reset();
    }

    /**
     * @return "Cruise Elroy" state (changes behavior of red ghost).
     * <p>0=off, 1=Elroy1, 2=Elroy2, -1=Elroy1 (disabled), -2=Elroy2 (disabled).</p>
     */
    public int cruiseElroy() { return cruiseElroy; }

    public void setCruiseElroy(int cruiseElroy) {
        this.cruiseElroy = cruiseElroy;
    }

    public boolean isCruiseElroyModeActive() { return cruiseElroy > 0; }

    public void activateCruiseElroyMode(boolean active) {
        int absValue = Math.abs(cruiseElroy);
        cruiseElroy = active ? absValue : -absValue;
    }

    @Override
    public void onFoodEaten(GameLevel gameLevel) {
        final Arcade_GameModel game = (Arcade_GameModel) gameLevel.game();
        final Arcade_LevelData data = game.levelData(gameLevel);
        int uneatenFoodCount = gameLevel.worldMap().foodLayer().uneatenFoodCount();
        if (uneatenFoodCount == data.elroy1DotsLeft()) {
            setCruiseElroy(1);
        } else if (uneatenFoodCount == data.elroy2DotsLeft()) {
            setCruiseElroy(2);
        }
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
        // Blinky overrides hunt method to take "Cruise Elroy" mode into account
        boolean chase = huntingTimer.phase() == HuntingPhase.CHASING || isCruiseElroyModeActive();
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