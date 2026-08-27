/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.GameRules;

public class Common_LevelCompleteState extends GameState {

    protected GameFlowController gameFlow;
    protected GameSystems systems;
    protected GameRules rules;
    protected GameSession session;
    protected GameLevel level;
    protected Pac pac;

    public Common_LevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {

        systems = game.variant().systems();
        gameFlow = game.variant().gameFlow();
        rules = game.variant().rules();
        session = game.session();
        level = session.level();
        pac = level.entities().pac();

        level.huntingTimerStrategy().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        // Pac-Man stops and stands still
        systems.pacState().setState(pac, PacState.SLEEPING);
        systems.pacPower().reset(pac);

        // Ghosts stop
        level.entities().ghosts().forEach(ghost -> systems.worldNavigator().setDisabled(ghost, true));

        level.entities().optBonus().ifPresent(bonus -> {
            systems.bonusState().setBonusInactive(bonus);
            bonus.optComp(BonusMoveAndJumpComp.class).ifPresent(_-> systems.bonusMoveAndJump().setBonusInactive(bonus));
            level.entities().remove(bonus);
        });

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onExit(GameContext context) {
        level.entities().ghosts().forEach(ghost -> systems.worldNavigator().setDisabled(ghost, false));
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            gameFlow.enterGameState(game, computeNextStateID());
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
