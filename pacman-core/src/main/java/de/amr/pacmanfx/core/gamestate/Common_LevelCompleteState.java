/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
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
        pac = level.entities().pac();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().stopAndReset();

        level.huntingTimer().stop();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        systems.pacPower().stopAndReset(pac);
        pac.state().setEnumValue(PacState.SLEEPING);

        level.entities().optBonus().ifPresent(bonus -> {
            systems.bonusState().setInactive(bonus);
            bonus.optComp(BonusMoveAndJumpComp.class).ifPresent(_-> systems.bonusMoveAndJump().setBonusInactive(bonus));
            level.entities().remove(bonus);
        });

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onExit(GameContext context) {
        level.entities().ghosts().forEach(ghost -> ghost.worldNavigation().setPaused(false));
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().tickCount() == 1) {
            lockPacAndGhosts(level.entities(), true);
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
