/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * The red ghost attacks Pac-Man directly and follows him like a shadow.
 */
public class RedGhostShadow extends Ghost {

    private final ElroyState elroyState = new ElroyState();

    public RedGhostShadow(String name) {
        super(RED_GHOST_SHADOW, name);
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
}