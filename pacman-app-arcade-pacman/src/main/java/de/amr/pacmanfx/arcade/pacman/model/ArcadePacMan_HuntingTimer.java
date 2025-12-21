/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.model.AbstractHuntingTimer;

public class ArcadePacMan_HuntingTimer extends AbstractHuntingTimer {

    // Ticks of scatter and chasing phases, -1 = INFINITE
    static final int[] TICKS_LEVEL_1 = {420, 1200, 420, 1200, 300, 1200, 300, -1};
    static final int[] TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980, 1, -1};
    static final int[] TICKS_LEVEL_5_ON = {300, 1200, 300, 1200, 300, 62262, 1, -1};

    public ArcadePacMan_HuntingTimer() {
        super("ArcadePacMan-HuntingTimer", 8);
    }

    @Override
    public long phaseDuration(int levelNumber, int phaseIndex) {
        long ticks = switch (levelNumber) {
            case 1 -> TICKS_LEVEL_1[phaseIndex];
            case 2, 3, 4 -> TICKS_LEVEL_2_3_4[phaseIndex];
            default -> TICKS_LEVEL_5_ON[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}
