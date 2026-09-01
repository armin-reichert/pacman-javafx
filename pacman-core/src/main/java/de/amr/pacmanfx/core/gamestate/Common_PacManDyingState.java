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

public final class Common_PacManDyingState extends AbstractGameState {

    private GameLevel level;
    private Pac pac;

    public Common_PacManDyingState() {
        super(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = session.level();
        pac = level.entities().pac();

        level.gateKeeper().resetCounterAndSetEnabled(true);
        level.huntingTimer().stop();

        pac.worldNavigation().setDisabled(true);
        systems.pacPower().stopAndReset(pac);
        systems.pacAnimation().lockAnimation(pac, true);

        pac.state().setEnumValue(PacState.DEAD);

        level.entities().ghosts().forEach(ghost -> {
            // Copilot claims that eaten ghosts returning to the house continue even when Pac-Man dies
            if (ghost.state().enumValue() != GhostState.RETURNING_HOME) {
                ghost.worldNavigation().setDisabled(true);
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

        if (tick == rules.pacDyingTiming().hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
            systems.pacAnimation().lockAnimation(pac, false);
            systems.pacAnimation().selectDyingAnimation(pac);
        }
        else if (tick == rules.pacDyingTiming().animationStartTick()) {
            systems.pacAnimation().startDyingAnimation(pac);
            game.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == rules.pacDyingTiming().hidePacTick()) {
            pac.hide();
        }
        else if (tick == rules.pacDyingTiming().pacDeadTick()) {
            level.entities().optBonus().ifPresent(bonus -> level.entities().remove(bonus));
            game.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }

        if (timer().hasExpired()) {
            level.entities().ghosts().forEach(ghost -> ghost.worldNavigation().setDisabled(false));
            session.setNumLives(session.numLives() - 1);
            flow.enterGameState(game, session.numLives() == 0
                ? CommonGameStateID.GAME_OVER
                : CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
