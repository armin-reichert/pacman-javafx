/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.gamestate.*;

import static java.util.Objects.requireNonNull;

public enum TengenMsPacMan_GameState {

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
     */
    BOOT (new GameBootState()),

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS MS. PAC-MAN" title,
     * the "PRESS START" and copyright text.
     * <p>
     * If no key is pressed for some time, the UI shows to the Ms. Pac-Man intro scene with the
     * ghost presentation. If still no key is pressed, the demo level is shown. After the demo
     * level ends, the credits screens are shown and then again the "PRESS START" scene.
     * </p>
     */
    GAME_INTRO (new GameIntroState()),

    /**
     * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
     * and start level can be set.
     */
    GAME_PREPARATION (new GamePreparationState()),

    SHOWING_HALL_OF_FAME (new ShowingHallOfFameState() ),

    GAME_OR_LEVEL_STARTING( new GameOrLevelStartingState() ),

    DEMO_LEVEL_PLAYING( new DemoLevelPlayingState(Timing.TICK_DEMO_LEVEL_START_HUNTING) ),

    GAME_STARTING( new GameStartingState() ),

    GAME_LEVEL_CONTINUE( new GameLevelContinueState() ),

    GAME_LEVEL_PLAYING( new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE( new GameLevelCompleteState() ),

    GAME_LEVEL_TRANSITION( new GameLevelTransitionState() ),

    GAME_LEVEL_EATING_GHOST( new GameLevelEatingGhostState() ),

    GAME_LEVEL_PACMAN_DYING( new GameLevelPacManDyingState(new GameLevelPacManDyingState.Timing(60, 90, 190, 240)) ),

    GAME_OVER ( new GameOverState() ),

    GAME_LEVEL_INTERMISSION( new GameLevelIntermissionState() );

    // end of enum values

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
