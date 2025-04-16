/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.HuntingTimer;
import de.amr.games.pacman.lib.timer.TickTimer;

/**
 * TODO: I have no info about the timing so far, use (inofficial) Arcade game values for now.
 */
public class TengenMsPacMan_HuntingTimer extends HuntingTimer {

    private static final int NUM_PHASES = 8;

    private static final long[] TICKS_LEVEL_1_TO_4 = { 420, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };
    private static final long[] TICKS_LEVEL_5_PLUS = { 300, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };

    public TengenMsPacMan_HuntingTimer() {
        super(NUM_PHASES);
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        return levelNumber <= 4 ? TICKS_LEVEL_1_TO_4[phaseIndex] : TICKS_LEVEL_5_PLUS[phaseIndex];
    }
}