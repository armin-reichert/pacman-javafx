/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

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
        final GameLevel level = session.assertLevel();

        final Pac pac = level.entities().pac();

        session.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimerStrategy().stop();

        level.entities().ghosts().forEach(ghost ->
            ghost.optComp(ElroyComp.class).ifPresent(elroy -> elroy.setEnabled(false)));

        level.entities().optBonus().ifPresent(bonus ->
            systems.bonusState().setInactive(bonus, systems.bonusMoveAndJump(), systems.worldNavigator()));

        systems.worldNavigator().setSpeed(pac, 0);
        systems.pacPower().reset(pac);
        systems.pacState().setState(pac, PacState.DEAD);
        systems.pacAnimation().stop(pac);

        waitForTimeout();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final LivesCounter livesCounter = session.livesCounter();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (session.isAttractMode()) {
                game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_OVER);
            } else {
                LivesCounterSystem.subtractLife(livesCounter);
                final boolean gameOver = livesCounter.data().numLives() == 0;
                game.variant().gameFlow().enterState(game,
                    gameOver ? CommonGameStateID.GAME_OVER : CommonGameStateID.GAME_OR_LEVEL_STARTING);
            }
            return;
        }

        if (tick == timing.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
            pac.animation().setReadyForDying(true);
        }
        else if (tick == timing.animationStartTick()) {
            pac.animation().setStartDying(true);
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == timing.hidePacTick()) {
            pac.hide();
            level.entities().optBonus().ifPresent(bonus -> {
                systems.bonusState().setInactive(bonus, systems.bonusMoveAndJump(), systems.worldNavigator());
                level.entities().remove(bonus);
            });
        }
        else if (tick == timing.pacDeadTick()) {
            game.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.heartbeat().triggerPulse();
            systems.pacState().update(pac);
        }

        systems.pacAnimation().update(pac);
    }
}
