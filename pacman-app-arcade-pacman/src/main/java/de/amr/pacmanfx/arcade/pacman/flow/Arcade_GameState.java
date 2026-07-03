/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.gamestate.*;

import static java.util.Objects.requireNonNull;

public enum Arcade_GameState {
    BOOT                       (new CommonGameBootState()),
    GAME_INTRO                 (new CommonGameIntroState()),
    GAME_PREPARATION           (new CommonGamePreparationState()),
    GAME_OR_LEVEL_STARTING     (new ArcadeGameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING         (new CommonDemoLevelPlayingState(Timing.TICK_DEMO_LEVEL_HUNTING_START)),
    GAME_STARTING              (new ArcadeGameStartingState()),
    GAME_LEVEL_CONTINUE        (new ArcadeGameLevelContinueState()),
    GAME_LEVEL_PLAYING         (new CommonGameLevelPlayingState()),
    GAME_LEVEL_COMPLETE        (new ArcadeGameLevelCompleteState()),
    GAME_LEVEL_TRANSITION      (new CommonGameLevelTransitionState()),
    GAME_LEVEL_EATING_GHOST    (new CommonGameLevelEatingGhostState()),
    GAME_LEVEL_PACMAN_DYING    (new CommonGameLevelPacManDyingState(new CommonGameLevelPacManDyingState.Timing(60, 90, 190, 210))),
    GAME_OVER                  (new ArcadeGameOverState()),
    GAME_LEVEL_INTERMISSION    (new ArcadeGameLevelIntermissionState());

    final GameState state;

    Arcade_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    public interface Timing {

        int TICK_DEMO_LEVEL_HUNTING_START = 120;
    }
}
