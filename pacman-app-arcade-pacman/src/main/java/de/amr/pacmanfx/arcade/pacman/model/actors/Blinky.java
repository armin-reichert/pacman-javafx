/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model.actors;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_LevelData;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    private boolean cruiseElroyActive;
    private byte cruiseElroyValue;

    protected Blinky() {
        super(RED_GHOST_SHADOW, "Blinky");
        reset();
        cruiseElroyActive = false;
        cruiseElroyValue = 0;
    }

    public int cruiseElroyValue() { return cruiseElroyValue; }

    public boolean isCruiseElroyActive() { return cruiseElroyActive; }

    public void setCruiseElroyActive(boolean active) {
        cruiseElroyActive = active;
        Logger.info("Cruise Elroy is: {}, active: {}", cruiseElroyValue, cruiseElroyActive);
    }

    private void setCruiseElroyValue(int value) {
        cruiseElroyValue = (byte) value;
        Logger.info("Cruise Elroy is: {}, active: {}", cruiseElroyValue, cruiseElroyActive);
    }

    @Override
    public void onFoodCountChange(GameLevel gameLevel) {
        super.onFoodCountChange(gameLevel);

        // "Cruise Elroy"
        final Arcade_GameModel game = (Arcade_GameModel) gameLevel.game();
        final Arcade_LevelData data = game.levelData(gameLevel.number());
        int uneatenFoodCount = gameLevel.worldMap().foodLayer().uneatenFoodCount();
        if (uneatenFoodCount == data.numDotsLeftElroy1()) {
            setCruiseElroyValue(1);
        } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
            setCruiseElroyValue(2);
        }
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {
        super.onPacKilled(gameLevel);
        setCruiseElroyActive(false);
    }

    @Override
    public void hunt(GameLevel gameLevel) {
        // Blinky overrides hunt method to take "Cruise Elroy" mode into account
        boolean chase = gameLevel.huntingTimer().phase() == HuntingPhase.CHASING || isCruiseElroyActive();
        Vector2i targetTile = chase
            ? chasingTargetTile(gameLevel)
            : gameLevel.worldMap().terrainLayer().ghostScatterTile(personality());
        setSpeed(gameLevel.game().ghostSpeedWhenAttacking(gameLevel, this));
        tryMovingTowardsTargetTile(gameLevel, targetTile);
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel gameLevel) {
        // Blinky (red ghost) attacks Pac-Man directly
        return gameLevel.pac().tile();
    }
}