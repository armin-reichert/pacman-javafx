/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public final class Common_PacManDyingState extends GameState {

    public record Timing(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick) {}

    private final Timing timing;

    public Common_PacManDyingState(Timing timing) {
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

        level.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimerStrategy().stop();

        // Note: this works also if the bonus has no Elroy component!
        level.entities().ghosts().forEach(ghost -> systems.ghostState().setElroyEnabled(ghost, false));

        // Note: this works also if the bonus has no movement component!
        level.entities().optBonus().ifPresent(bonus -> systems.bonusMoveAndJump().setBonusInactive(bonus));

        systems.worldNavigator().setMoveDirSpeed(pac, 0);
        systems.pacPower().reset(pac);
        systems.pacState().setState(pac, PacState.DEAD);
        systems.pacAnimation().setDisabled(pac, true);

        timer().resetToIndefiniteDuration();

        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameFlowController gameFlow = game.variant().gameFlow();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

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
        else if (tick == timing.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
        }
        else if (tick == timing.animationStartTick()) {
            systems.pacAnimation().setDisabled(pac, false);
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
