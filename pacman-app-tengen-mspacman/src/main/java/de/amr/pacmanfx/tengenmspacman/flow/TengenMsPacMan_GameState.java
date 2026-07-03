/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.gamestate.*;

import static java.util.Objects.requireNonNull;

/**
 * The game states used by the Ms. Pac-Man Tengen game variant.
 */
public enum TengenMsPacMan_GameState {
    BOOT                        (new CommonGameBootState()),
    GAME_INTRO                  (new CommonGameIntroState()),
    GAME_PREPARATION            (new CommonGamePreparationState()),
    SHOWING_HALL_OF_FAME        (new ShowingHallOfFameState() ),
    GAME_OR_LEVEL_STARTING      (new GameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING          (new CommonDemoLevelPlayingState(120)),
    GAME_STARTING               (new GameStartingState()),
    GAME_LEVEL_CONTINUE         (new GameLevelContinueState()),
    GAME_LEVEL_PLAYING          (new CommonGameLevelPlayingState()),
    GAME_LEVEL_COMPLETE         (new GameLevelCompleteState()),
    GAME_LEVEL_TRANSITION       (new CommonGameLevelTransitionState()),
    GAME_LEVEL_EATING_GHOST     (new CommonEatingGhostState()),
    GAME_LEVEL_PACMAN_DYING     (new CommonPacManDyingState(60, 90, 190, 240)),
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
