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
        final GameSystems systems = game.variant().systems();
        final GameFlowController gameFlow = game.variant().gameFlow();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (tick == 0) {
            // At this point in time, the "dying" animation has been selected. However, it could
            // be in the state from the previous playing (it is cached), so we reset it here.
            systems.actorSpriteAnimController().resetSelected(pac);
        }
        else if (tick == timing.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
        }
        else if (tick == timing.animationStartTick()) {
            systems.pacAnimation().setDisabled(pac, false); // "dying" animation can run now
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == timing.hidePacTick()) {
            systems.actorSpriteAnimController().resetSelected(pac);
            pac.hide();
        }
        else if (tick == timing.pacDeadTick()) {
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
        game.session().level().entities().ghosts().forEach(ghost ->
            game.variant().systems().worldNavigator().setDisabled(ghost, false));
    }
}
