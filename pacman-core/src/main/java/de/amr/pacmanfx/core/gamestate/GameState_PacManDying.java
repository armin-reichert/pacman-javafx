/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public final class GameState_PacManDying extends GameState {

    public record Timing(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick) {}

    private final Timing timing;

    public GameState_PacManDying(Timing timing) {
        super(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        this.timing = requireNonNull(timing);
    }

    @Override
    public void onEnter(GameContext game) {
        requireNonNull(game);

        final GameSystems systems = game.variant().systems();

        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        session.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimerStrategy().stop();

        // Note: this works also if the bonus has no Elroy component!
        level.entities().ghosts().forEach(ghost -> systems.ghostState().setElroyEnabled(ghost, false));

        // Note: this works also if the bonus has no movement component!
        level.entities().optBonus().ifPresent(bonus -> systems.bonusMoveAndJump().setBonusInactive(bonus));

        systems.worldNavigator().setMoveDirSpeed(pac, 0);
        systems.pacPower().reset(pac);
        systems.pacState().setState(pac, PacState.DEAD);
        systems.pacAnimation().stop(pac);

        timer().resetToIndefiniteDuration();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameFlowController gameFlow = game.variant().gameFlow();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final LivesCounter livesCounter = session.livesCounter();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        systems.entityUpdater().updateLevelHeartbeat(level);
        systems.entityUpdater().updatePac(game, level, pac);

        if (timer().hasExpired()) {
            if (session.isAttractMode()) {
                gameFlow.enterState(game, CommonGameStateID.GAME_OVER);
            }
            else {
                LivesCounterSystem.subtractLife(livesCounter);
                final boolean gameOver = livesCounter.data().numLives() == 0;
                gameFlow.enterState(game, gameOver
                    ? CommonGameStateID.GAME_OVER
                    : CommonGameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == timing.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
            pac.animation().setReadyForDying(true);
        }
        else if (tick == timing.animationStartTick()) {
            pac.animation().setStartDying(true);
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == timing.hidePacTick()) {
            pac.hide();
        }
        else if (tick == timing.pacDeadTick()) {
            level.entities().optBonus().ifPresent(bonus -> level.entities().remove(bonus));
            game.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
    }
}
