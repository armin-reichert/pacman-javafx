/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.gamestate.*;

import static java.util.Objects.requireNonNull;

public enum TengenMsPacMan_GameState {

    BOOT                        (new CommonGameBootState()),
    GAME_INTRO                  (new CommonGameIntroState()),
    GAME_PREPARATION            (new GamePreparationState()),
    SHOWING_HALL_OF_FAME        (new ShowingHallOfFameState() ),
    GAME_OR_LEVEL_STARTING      (new GameOrLevelStartingState()),
    DEMO_LEVEL_PLAYING          (new CommonDemoLevelPlayingState(Timing.TICK_DEMO_LEVEL_START_HUNTING)),
    GAME_STARTING               (new GameStartingState()),
    GAME_LEVEL_CONTINUE         (new GameLevelContinueState()),
    GAME_LEVEL_PLAYING          (new CommonGameLevelPlayingState()),
    GAME_LEVEL_COMPLETE         (new GameLevelCompleteState()),
    GAME_LEVEL_TRANSITION       (new CommonGameLevelTransitionState()),
    GAME_LEVEL_EATING_GHOST     (new CommonGameLevelEatingGhostState()),
    GAME_LEVEL_PACMAN_DYING     (new CommonGameLevelPacManDyingState(new CommonGameLevelPacManDyingState.Timing(60, 90, 190, 240))),
    GAME_OVER                   (new GameOverState()),
    GAME_LEVEL_INTERMISSION     (new GameLevelIntermissionState());

    final GameState state;

    TengenMsPacMan_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    //TODO make package-private again
    public static class Timing {
        public static final short TICK_SHOW_READY = 10;
        public static final short TICK_NEW_GAME_SHOW_GUYS = 70;
        public static final short TICK_NEW_GAME_START_HUNTING = 250;
        public static final short TICK_RESUME_HUNTING = 240;
        public static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
    }
}
