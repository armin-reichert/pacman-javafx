/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.lib.timer.TickTimer;

public class ArcadePacMan_HuntingTimer extends HuntingTimer {

    private static final int NUM_PHASES = 8;

    // Ticks of scatter and chasing phases, -1=INDEFINITE
    private static final int[] HUNTING_TICKS_LEVEL_1 = {420, 1200, 420, 1200, 300,  1200, 300, -1};
    private static final int[] HUNTING_TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980,   1, -1};
    private static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 300, 1200, 300, 62262,   1, -1};

    public ArcadePacMan_HuntingTimer() {
        super(NUM_PHASES);
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = switch (levelNumber) {
            case 1 -> HUNTING_TICKS_LEVEL_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_LEVEL_2_3_4[phaseIndex];
            default -> HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}