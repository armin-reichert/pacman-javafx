/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.PacDyingTiming;

import static java.util.Objects.requireNonNull;

public final class Common_PacManDyingState extends GameState {

    private GameFlowController gameFlow;
    private GameSystems systems;
    private GameSession session;
    private GameLevel level;
    private Pac pac;
    private PacDyingTiming pacDyingTiming;

    public Common_PacManDyingState() {
        super(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
    }

    @Override
    public void onEnter(GameContext game) {
        requireNonNull(game);

        gameFlow = game.variant().gameFlow();
        systems = game.variant().systems();
        pacDyingTiming = game.variant().rules().pacDyingTiming();
        session = game.session();
        level = session.level();
        pac = level.entities().pac();

        level.gateKeeper().resetCounterAndSetEnabled(true);
        level.huntingTimerStrategy().stop();

        // Stop Pac-Man and set "dead" state
        systems.worldNavigator().setDisabled(pac, true);
        systems.pacState().setState(pac, PacState.DEAD);
        systems.pacPower().reset(pac);
        systems.pacAnimation().setDisabled(pac, true);

        level.entities().ghosts().forEach(ghost -> {
            // Copilot claims that eaten ghosts returning to the house continue even when Pac-Man dies
            if (ghost.state().enumValue() != GhostState.RETURNING_HOME) {
                systems.worldNavigator().setDisabled(ghost, true);
            }
            // Note: this works also if the bonus has no Elroy component!
            systems.ghostState().setElroyEnabled(ghost, false);
        });

        // Note: this works also if the bonus has no movement component!
        level.entities().optBonus().ifPresent(bonus -> systems.bonusMoveAndJump().setBonusInactive(bonus));

        timer().resetToIndefiniteDuration();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        if (tick == 0) {
            // At this point in time, the "dying" animation has been selected. However, it could
            // be in the state from the previous playing (it is cached), so we reset it here.
            systems.actorSpriteAnimController().resetSelected(pac);
        }
        else if (tick == pacDyingTiming.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
        }
        else if (tick == pacDyingTiming.animationStartTick()) {
            systems.pacAnimation().setDisabled(pac, false); // "dying" animation can run now
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == pacDyingTiming.hidePacTick()) {
            systems.actorSpriteAnimController().resetSelected(pac);
            pac.hide();
        }
        else if (tick == pacDyingTiming.pacDeadTick()) {
            level.entities().optBonus().ifPresent(bonus -> level.entities().remove(bonus));
            game.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }

        if (timer().hasExpired()) {
            if (session.isAttractMode()) {
                gameFlow.enterGameState(game, CommonGameStateID.GAME_OVER);
            }
            else {
                session.setNumLives(session.numLives() - 1);
                gameFlow.enterGameState(game, session.numLives() == 0
                    ? CommonGameStateID.GAME_OVER
                    : CommonGameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        level.entities().ghosts().forEach(ghost -> systems.worldNavigator().setDisabled(ghost, false));
    }
}
