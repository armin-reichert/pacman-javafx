/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.model.AbstractHuntingTimer;

/**
 * Values are taken from the Pac-Man dossier.
 */
public class ArcadePacMan_HuntingTimer extends AbstractHuntingTimer {

    private static final int NUM_PHASES = 8;

    // Ticks of scatter (index 0, 2, 4, 6) and chasing (1, 3, 5, 7) phases, -1 = forever
    private static final int[] TICKS_LEVEL_1     = { 420, 1200, 420, 1200, 300,  1200, 300, -1 };
    private static final int[] TICKS_LEVEL_2_3_4 = { 420, 1200, 420, 1200, 300, 61980,   1, -1 };
    private static final int[] TICKS_LEVEL_5_ON  = { 300, 1200, 300, 1200, 300, 62262,   1, -1 };

    public ArcadePacMan_HuntingTimer() {
        super("ArcadePacMan-HuntingTimer", NUM_PHASES);
    }

    @Override
    public long phaseDuration(int levelNumber, int phaseIndex) {
        Validations.requireValidLevelNumber(levelNumber);
        if (Validations.inClosedRange(phaseIndex, 0, NUM_PHASES - 1)) {
            long ticks = switch (levelNumber) {
                case 1 -> TICKS_LEVEL_1[phaseIndex];
                case 2, 3, 4 -> TICKS_LEVEL_2_3_4[phaseIndex];
                default -> TICKS_LEVEL_5_ON[phaseIndex];
            };
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }
        else {
            throw new IllegalArgumentException("Phase index " + phaseIndex + " is invalid");
        }
    }
}
