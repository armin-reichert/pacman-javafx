/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.model.AbstractHuntingTimer;

/**
 * Details are from a conversation with user @damselindis on Reddit. I am not sure if they are correct.
 *
 * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
 * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
 */
public class ArcadeMsPacMan_HuntingTimer extends AbstractHuntingTimer {

    private static final int NUM_PHASES = 8;

    // Ticks of scatter (index 0, 2, 4, 6) and chasing (1, 3, 5, 7) phases, -1 = forever
    private static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = { 420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_LEVEL_5_PLUS = { 300, 1200, 1, 62220, 1, 62220, 1, -1};

    public ArcadeMsPacMan_HuntingTimer() {
        super("ArcadeMsPacMan-HuntingTimer", NUM_PHASES);
    }

    @Override
    public long phaseDuration(int levelNumber, int phaseIndex) {
        Validations.requireValidLevelNumber(levelNumber);
        if (Validations.inClosedRange(phaseIndex, 0, NUM_PHASES - 1)) {
            long ticks = levelNumber < 5
                ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex]
                : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }
        else {
            throw new IllegalArgumentException("Phase index " + phaseIndex + " is invalid");
        }
    }
}
