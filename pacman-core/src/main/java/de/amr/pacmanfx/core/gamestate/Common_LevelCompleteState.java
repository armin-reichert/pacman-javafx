/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.bonus.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.level.GameLevel;

public class Common_LevelCompleteState extends AbstractGameState {

    protected GameLevel level;
    protected Pac pac;

    public Common_LevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = session.level();
        pac = level.entitySet().pac();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().stopAndReset();

        level.huntingTimer().stop();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        systems.pacPower().stopAndReset(pac);
        pac.state().setEnumValue(PacState.SLEEPING);

        final Bonus bonus = level.entitySet().entities().anyOfTypeOrNull(Bonus.class);
        if (bonus != null) {
            systems.bonusState().setInactive(bonus);
            bonus.optComp(BonusMoveAndJumpComp.class).ifPresent(_-> systems.bonusMoveAndJump().setBonusInactive(bonus));
            level.entitySet().remove(bonus);
        }

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onExit(GameContext context) {
        level.entitySet().ghosts().forEach(ghost -> ghost.worldNavigation().setPaused(false));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (stateTick == 1) {
            lockPacAndGhosts(level.entitySet(), true);
        }
        if (timer().hasExpired()) {
            flow.enterGameState(game, computeNextStateID());
        }
    }

    protected Named computeNextStateID() {
        // Just in case: if demo level was completed, go back to intro scene
        if (session.isAttractMode()) {
            return CommonGameStateID.GAME_INTRO;
        }

        final boolean cutSceneFollows = rules.cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }

        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
