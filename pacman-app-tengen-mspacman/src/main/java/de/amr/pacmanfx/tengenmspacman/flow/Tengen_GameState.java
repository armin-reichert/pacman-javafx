/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.gamestate.*;

import static java.util.Objects.requireNonNull;

/**
 * The game states used by the Ms. Pac-Man Tengen game variant.
 */
public enum Tengen_GameState {
    BOOT                        (new Common_BootState()),
    GAME_INTRO                  (new Common_IntroState()),
    GAME_PREPARATION            (new Common_PreparationState()),
    SHOWING_HALL_OF_FAME        (new Tengen_ShowingHallOfFameState() ),
    GAME_OR_LEVEL_STARTING      (new Common_GameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING          (new Common_DemoLevelPlayingState()),
    GAME_STARTING               (new Tengen_GameStartingState()),
    GAME_LEVEL_CONTINUE         (new Common_LevelContinueState()),
    GAME_LEVEL_PLAYING          (new Common_PlayingLevelState()),
    GAME_LEVEL_COMPLETE         (new Tengen_LevelCompleteState()),
    GAME_LEVEL_TRANSITION       (new Common_LevelTransitionState()),
    GAME_LEVEL_EATING_GHOST     (new Common_EatingGhostState()),
    GAME_LEVEL_PACMAN_DYING     (new Common_PacManDyingState()),
    GAME_OVER                   (new Tengen_GameOverState()),
    GAME_LEVEL_INTERMISSION     (new Tengen_LevelIntermissionState());

    Tengen_GameState(AbstractGameState state) {
        this.state = requireNonNull(state);
    }

    public AbstractGameState state() {
        return state;
    }

    final AbstractGameState state;
}
