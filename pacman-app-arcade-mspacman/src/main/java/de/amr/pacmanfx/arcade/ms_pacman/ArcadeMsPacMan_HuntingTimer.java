/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;

/**
 * Details are from a conversation with user @damselindis on Reddit. I am not sure if they are correct.
 *
 * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
 * @see <a href="https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
 */
public class ArcadeMsPacMan_HuntingTimer extends HuntingTimer {

    static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
    static final int[] HUNTING_TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

    public ArcadeMsPacMan_HuntingTimer() {
        super("ArcadeMsPacMan-HuntingTimer", 8);
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
        long ticks = levelNumber < 5 ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex] : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}
