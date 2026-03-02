/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class Arcade_GameController extends StateMachine<Game> implements GameControl {

    public static final short TICK_NEW_GAME_SHOW_GUYS = 60;
    public static final short TICK_NEW_GAME_START_HUNTING = 240;
    public static final short TICK_RESUME_HUNTING = 120;
    public static final short TICK_EATING_GHOST_COMPLETE = 60;
    public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    public static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
    public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    public static final short TICK_PACMAN_DYING_PAC_DEAD = 210;

    public Arcade_GameController() {
        setName("Arcade Pac-Man Games State Machine");
        addStates(ArcadeGameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return this;
    }

}