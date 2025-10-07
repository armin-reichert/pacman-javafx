/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;

public class ArcadePacMan_HuntingTimer extends HuntingTimer {

    // Ticks of scatter and chasing phases, -1 = INFINITE
    static final int[] TICKS_LEVEL_1 = {420, 1200, 420, 1200, 300, 1200, 300, -1};
    static final int[] TICKS_LEVEL_2_3_4 = {420, 1200, 420, 1200, 300, 61980, 1, -1};
    static final int[] TICKS_LEVEL_5_ON = {300, 1200, 300, 1200, 300, 62262, 1, -1};

    public ArcadePacMan_HuntingTimer() {
        super("ArcadePacMan-HuntingTimer", 8);
        phaseIndex.addListener((py, ov, newPhaseIndex) -> {
            if (gameLevel != null && newPhaseIndex.intValue() > 0) {
                gameLevel.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::requestTurnBack);
            }
            logPhaseChange();
        });
    }

    @Override
    public long huntingTicks(int levelNumber, int phaseIndex) {
        long ticks = switch (levelNumber) {
            case 1 -> TICKS_LEVEL_1[phaseIndex];
            case 2, 3, 4 -> TICKS_LEVEL_2_3_4[phaseIndex];
            default -> TICKS_LEVEL_5_ON[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}
