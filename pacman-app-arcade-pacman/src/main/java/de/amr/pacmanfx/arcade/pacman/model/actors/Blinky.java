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
import static java.util.Objects.requireNonNull;

public class Blinky extends Ghost {

    public enum ElroyMode {NONE, _1, _2}

    private boolean cruiseElroyEnabled;
    private ElroyMode elroyMode;

    protected Blinky() {
        super(RED_GHOST_SHADOW, "Blinky");
        reset();
        cruiseElroyEnabled = false;
        elroyMode = ElroyMode.NONE;
    }

    public ElroyMode elroyMode() { return elroyMode; }

    public boolean isCruiseElroyEnabled() { return cruiseElroyEnabled; }

    public void setCruiseElroyEnabled(boolean on) {
        cruiseElroyEnabled = on;
        Logger.info("Cruise Elroy speed increase is: {}, active: {}", elroyMode, cruiseElroyEnabled);
    }

    public void setElroyMode(ElroyMode mode) {
        elroyMode = requireNonNull(mode);
        Logger.info("Cruise Elroy is: {}, active: {}", elroyMode, cruiseElroyEnabled);
    }

    /**
     * When Pac-Man is killed, Blinky disables his Cruise Elroy mode.
     *
     * @param level the game level where this happens
     */
    @Override
    public void onPacKilled(GameLevel level) {
        setCruiseElroyEnabled(false);
    }

    /**
     * Blinky overrides method to take "Cruise Elroy" mode into account.
     * @param level the current game level
     */
    @Override
    public void hunt(GameLevel level) {
        boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || isCruiseElroyEnabled();
        Vector2i targetTile = chase
            ? chasingTargetTile(level)
            : level.worldMap().terrainLayer().ghostScatterTile(personality());
        setSpeed(level.game().ghostSpeed(level, this));
        tryMovingTowardsTargetTile(level, targetTile);
    }

    /**
     * Blinky (red ghost) attacks Pac-Man directly.
     * @return Pac-Man's current tile position
     */
    @Override
    public Vector2i chasingTargetTile(GameLevel level) {
        return level.pac().tile();
    }
}