/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model.actors;

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

    public void setCruiseElroyValue(int value) {
        cruiseElroyValue = (byte) value;
        Logger.info("Cruise Elroy is: {}, active: {}", cruiseElroyValue, cruiseElroyActive);
    }

    @Override
    public void onPacKilled(GameLevel level) {
        super.onPacKilled(level);
        setCruiseElroyActive(false);
    }

    @Override
    public void hunt(GameLevel level) {
        // Blinky overrides hunt method to take "Cruise Elroy" mode into account
        boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || isCruiseElroyActive();
        Vector2i targetTile = chase
            ? chasingTargetTile(level)
            : level.worldMap().terrainLayer().ghostScatterTile(personality());
        setSpeed(level.game().ghostSpeed(level, this));
        tryMovingTowardsTargetTile(level, targetTile);
    }

    @Override
    public Vector2i chasingTargetTile(GameLevel level) {
        // Blinky (red ghost) attacks Pac-Man directly
        return level.pac().tile();
    }
}