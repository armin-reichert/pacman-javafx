/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.model.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.model.entities.livescounter.LivesCounter;
import de.amr.pacmanfx.core.model.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.level.GameLevel;

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

        final GameLevel level = game.assertLevel();
        final Pac pac = level.entities().pac();

        game.model().gateKeeper().resetCounterAndSetEnabled(true);
        level.huntingTimerStrategy().stop();

        level.entities().ghosts().forEach(ghost -> ghost.optComponent(ElroyComp.class).ifPresent(elroy -> elroy.setEnabled(false)));
        level.entities().optBonus().ifPresent(bonus -> game.systems().bonusState().setInactive(bonus));

        game.systems().worldNavigator().setSpeed(pac, 0);
        game.systems().pacPower().reset(pac);
        game.systems().pacState().setState(pac, PacState.DEAD);
        game.systems().pacAnimation().stop(pac);

        waitForTimeout();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final LivesCounter livesCounter = level.entities().entitySet().uniqueOfType(LivesCounter.class);
        final Pac pac = level.entities().pac();

        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                gameContext.flow().enterState(gameContext, CommonGameStateID.GAME_OVER);
            } else {
                LivesCounterSystem.subtractLife(livesCounter);
                final boolean gameOver = livesCounter.data().numLives() == 0;
                gameContext.flow().enterState(gameContext,
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
            gameContext.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == timing.hidePacTick()) {
            pac.hide();
            level.optBonus().ifPresent(bonus -> gameContext.systems().bonusState().setInactive(bonus));
        }
        else if (tick == timing.pacDeadTick()) {
            gameContext.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.heartbeat().triggerPulse();
            gameContext.systems().pacState().update(pac);
        }

        gameContext.systems().pacAnimation().update(pac);
    }
}
