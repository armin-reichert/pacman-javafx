/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model.actors;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.Ghost;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

public class Blinky extends Ghost {

    private final ElroyState elroyState = new ElroyState();

    public Blinky() {
        super(RED_GHOST_SHADOW, "Blinky");
        setHuntingStrategy((GameLevel level, Float speed) -> {
            setSpeed(speed);
            final boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || elroyState.enabled();
            final Vector2i targetTile = chase
                ? chasingTargetTile(level)
                : level.worldMap().terrainLayer().ghostScatterTile(personality());
            tryMovingTowardsTargetTile(level, targetTile);
        });
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        elroyState.reset();
    }

    public ElroyState elroyState() { return elroyState; }

    /**
     * When Pac-Man is killed, Blinky disables his Cruise Elroy mode.
     *
     * @param level the game level where this happens
     */
    @Override
    public void onPacKilled(GameLevel level) {
        elroyState.setEnabled(false);
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