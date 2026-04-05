/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene2D {

    public enum SceneState {
        SHOWING_NOTHING,
        SHOWING_HEX_CODES,
        SHOWING_SPRITE_FRAGMENTS,
        SHOWING_GRID
    }

    private SceneState state;

    public SceneState state() {
        return state;
    }

    @Override
    protected void doInit(Game game) {
        state = SceneState.SHOWING_NOTHING;
    }

    @Override
    public void update(Game game) {
        final State<Game> gameState = game.control().state();
        final long tick = gameState.timer().tickCount();
        switch ((int) tick) {
            case 60 -> state = SceneState.SHOWING_HEX_CODES;
            case 120 -> state = SceneState.SHOWING_SPRITE_FRAGMENTS;
            case 210 -> state = SceneState.SHOWING_GRID;
            case 240 -> gameState.expire();
        }
    }
}