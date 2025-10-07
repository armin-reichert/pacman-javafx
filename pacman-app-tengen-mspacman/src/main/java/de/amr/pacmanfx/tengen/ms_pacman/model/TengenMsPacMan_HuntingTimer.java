/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;

/**
 * TODO: I have no info about the timing so far, use (inofficial) Arcade game values for now.
 */
public class TengenMsPacMan_HuntingTimer extends HuntingTimer {

    private static final int NUM_PHASES = 8;

    private static final long[] TICKS_LEVEL_1_TO_4 = { 420, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };
    private static final long[] TICKS_LEVEL_5_PLUS = { 300, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };

    public TengenMsPacMan_HuntingTimer() {
        super("TengenMsPacMan-HuntingTimer", NUM_PHASES);
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
        return levelNumber <= 4 ? TICKS_LEVEL_1_TO_4[phaseIndex] : TICKS_LEVEL_5_PLUS[phaseIndex];
    }
}