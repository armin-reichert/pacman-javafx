/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.PacDyingTiming;

public final class Common_PacManDyingState extends AbstractGameState {

    private PacDyingTiming pacDyingTiming;
    private GameLevel level;
    private Pac pac;

    public Common_PacManDyingState() {
        super(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
    }

    @Override
    public void onEnterState(GameContext game) {
        pacDyingTiming = rules.pacDyingTiming();
        level = session.level();
        pac = level.entities().pac();

        level.gateKeeper().resetCounterAndSetEnabled(true);
        level.huntingTimerStrategy().stop();

        // Stop Pac-Man movement and animation and set "dead" state
        systems.worldNavigator().setDisabled(pac, true);
        systems.pacAnimation().setDisabled(pac, true);
        systems.pacPower().reset(pac);
        systems.pacState().setState(pac, PacState.DEAD);

        level.entities().ghosts().forEach(ghost -> {
            // Copilot claims that eaten ghosts returning to the house continue even when Pac-Man dies
            if (ghost.state().enumValue() != GhostState.RETURNING_HOME) {
                systems.worldNavigator().setDisabled(ghost, true);
            }
            // Note: this works also if the bonus has no Elroy component!
            systems.ghostState().setElroyEnabled(ghost, false);
        });

        // Stop bonus movement. Note: this works also if the bonus has no movement component!
        level.entities().optBonus().ifPresent(bonus -> systems.bonusMoveAndJump().setBonusInactive(bonus));

        // End of dying animation triggers state timeout
        timer().resetToIndefiniteDuration();

        //TODO: needed? Scene controllers stop sounds already?
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        if (tick == 0) {
            // At this point in time, the "dying" animation is selected. However, it could still
            // be at the last frame (from the previous execution), so we have to reset it here.
            systems.actorSpriteAnimController().resetSelected(pac);
        }
        else if (tick == pacDyingTiming.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
        }
        else if (tick == pacDyingTiming.animationStartTick()) {
            systems.pacAnimation().setDisabled(pac, false); // "dying" animation can start/continue
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == pacDyingTiming.hidePacTick()) {
            pac.hide();
        }
        else if (tick == pacDyingTiming.pacDeadTick()) {
            level.entities().optBonus().ifPresent(bonus -> level.entities().remove(bonus));
            game.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }

        if (timer().hasExpired()) {
            session.setNumLives(session.numLives() - 1);
            flow.enterGameState(game, session.numLives() == 0
                ? CommonGameStateID.GAME_OVER
                : CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }

    @Override
    public void onExit(GameContext game) {
        level.entities().ghosts().forEach(ghost -> systems.worldNavigator().setDisabled(ghost, false));
    }
}
