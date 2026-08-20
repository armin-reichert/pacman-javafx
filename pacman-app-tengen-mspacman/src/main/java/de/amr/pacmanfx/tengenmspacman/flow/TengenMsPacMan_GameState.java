/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.gamestate.*;

import static java.util.Objects.requireNonNull;

/**
 * The game states used by the Ms. Pac-Man Tengen game variant.
 */
public enum TengenMsPacMan_GameState {
    BOOT                        (new GameState_Booting()),
    GAME_INTRO                  (new GameState_Intro()),
    GAME_PREPARATION            (new GameState_PreparingGamePlay()),
    SHOWING_HALL_OF_FAME        (new ShowingHallOfFameState() ),
    GAME_OR_LEVEL_STARTING      (new GameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING          (new GameState_DemoLevelPlaying()),
    GAME_STARTING               (new GameStartingState()),
    GAME_LEVEL_CONTINUE         (new GameLevelContinueState()),
    GAME_LEVEL_PLAYING          (new GameState_PlayingLevel()),
    GAME_LEVEL_COMPLETE         (new GameLevelCompleteState()),
    GAME_LEVEL_TRANSITION       (new GameState_LevelTransition()),
    GAME_LEVEL_EATING_GHOST     (new GameState_EatingGhost()),
    GAME_LEVEL_PACMAN_DYING     (new GameState_PacManDying(new GameState_PacManDying.Timing(60, 90, 190, 240))),
    GAME_OVER                   (new GameOverState()),
    GAME_LEVEL_INTERMISSION     (new GameLevelIntermissionState());

    TengenMsPacMan_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    final GameState state;
}
