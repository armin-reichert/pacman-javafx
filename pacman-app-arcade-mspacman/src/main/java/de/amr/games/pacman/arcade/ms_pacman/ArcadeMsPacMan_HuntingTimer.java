/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.lib.timer.TickTimer;

/**
 * Details are from a conversation with user @damselindis on Reddit. I am not sure if they are correct.
 *
 * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
 * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
 */
public class ArcadeMsPacMan_HuntingTimer extends HuntingTimer {

    private static final int NUM_PHASES = 8;

    private static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    public ArcadeMsPacMan_HuntingTimer() {
        super(NUM_PHASES);
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}