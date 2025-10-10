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
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    private boolean cruiseElroyActive;
    private byte cruiseElroyValue;

    protected Blinky() {
        reset();
        cruiseElroyActive = false;
        cruiseElroyValue = 0;
    }

    public int cruiseElroyValue() { return cruiseElroyValue; }

    public boolean isCruiseElroyActive() { return cruiseElroyActive; }

    public void setCruiseElroyActive(boolean active) {
        cruiseElroyActive = active;
    }

    @Override
    public void onFoodCountChange(GameLevel gameLevel) {
        super.onFoodCountChange(gameLevel);

        // "Cruise Elroy"
        final Arcade_GameModel game = (Arcade_GameModel) gameLevel.game();
        final Arcade_LevelData data = game.levelData(gameLevel);
        int uneatenFoodCount = gameLevel.worldMap().foodLayer().uneatenFoodCount();
        if (uneatenFoodCount == data.elroy1DotsLeft()) {
            cruiseElroyValue = 1;
        } else if (uneatenFoodCount == data.elroy2DotsLeft()) {
            cruiseElroyValue = 2;
        }
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {
        super.onPacKilled(gameLevel);
        setCruiseElroyActive(false);
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
    public void hunt(GameLevel gameLevel) {
        // Blinky overrides hunt method to take "Cruise Elroy" mode into account
        boolean chase = gameLevel.huntingTimer().phase() == HuntingPhase.CHASING || isCruiseElroyActive();
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