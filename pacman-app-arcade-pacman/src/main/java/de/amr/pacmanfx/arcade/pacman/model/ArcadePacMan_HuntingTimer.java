/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractHuntingTimer;

import static de.amr.pacmanfx.Validations.inClosedRange;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;

/**
 * Values are taken from the Pac-Man dossier.
 */
public class ArcadePacMan_HuntingTimer extends AbstractHuntingTimer {

    private static final int NUM_PHASES = 8;

    // Ticks of scatter (index 0, 2, 4, 6) and chasing (1, 3, 5, 7) phases, -1 = forever
    private static final int[] TICKS_SEQ_1 = { 420, 1200, 420, 1200, 300,  1200, 300, -1 };
    private static final int[] TICKS_SEQ_2 = { 420, 1200, 420, 1200, 300, 61980,   1, -1 };
    private static final int[] TICKS_SEQ_3 = { 300, 1200, 300, 1200, 300, 62262,   1, -1 };

    public ArcadePacMan_HuntingTimer() {
        super("ArcadePacMan-HuntingTimer", NUM_PHASES);
    }

    @Override
    public long phaseDuration(int levelNumber, int phaseIndex) {
        requireValidLevelNumber(levelNumber);
        if (!inClosedRange(phaseIndex, 0, NUM_PHASES - 1)) {
            throw new IllegalArgumentException("Phase index %d is invalid".formatted(phaseIndex));
        }
        final long ticks = switch (levelNumber) {
            case 1       -> TICKS_SEQ_1[phaseIndex];
            case 2, 3, 4 -> TICKS_SEQ_2[phaseIndex];
            default      -> TICKS_SEQ_3[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}
