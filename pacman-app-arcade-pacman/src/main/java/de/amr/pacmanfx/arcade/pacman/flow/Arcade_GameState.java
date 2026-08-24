/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.gamestate.*;

/**
 * The game states used by the Arcade game variants (Pc-Man, Ms. Pac-Man and their XXL versions).
 */
public enum Arcade_GameState {
    BOOT                       (new Common_BootState()),
    GAME_INTRO                 (new Common_IntroState()),
    GAME_PREPARATION           (new Common_PreparationState()),
    GAME_OR_LEVEL_STARTING     (new Common_GameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING         (new Common_DemoLevelPlayingState()),
    GAME_STARTING              (new Arcade_GameStartingState()),
    GAME_LEVEL_CONTINUE        (new Common_LevelContinueState()),
    GAME_LEVEL_PLAYING         (new Common_PlayingLevelState()),
    GAME_LEVEL_COMPLETE        (new Common_LevelCompleteState()),
    GAME_LEVEL_TRANSITION      (new Common_LevelTransitionState()),
    GAME_LEVEL_EATING_GHOST    (new Common_EatingGhostState()),
    GAME_LEVEL_PACMAN_DYING    (new Common_PacManDyingState(new Common_PacManDyingState.Timing(60, 90, 190, 210))),
    GAME_OVER                  (new Arcade_GameOverState()),
    GAME_LEVEL_INTERMISSION    (new Arcade_LevelIntermissionState());

    Arcade_GameState(GameState state) {
        this.state = state;
    }

    public GameState state() {
        return state;
    }

    final GameState state;
}
