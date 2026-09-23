/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
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
        pac = level.entitySet().pac();

        level.gateKeeper().resetCounterAndSetEnabled(true);
        level.huntingTimer().stop();

        systems.pacPower().stopAndReset(pac);
        systems.pacAnimation().lockAnimation(pac, true);

        // Note: This does not immediately change the sprite but stops world movement
        pac.state().setEnumValue(PacState.DEAD);

        level.entitySet().ghosts().forEach(ghost -> {
            // Copilot claims that eaten ghosts returning to the house continue even when Pac-Man dies
            if (ghost.state().enumValue() != GhostState.RETURNING_HOME) {
                ghost.worldNavigation().setPaused(true);
            }
            // Note: this works also if the bonus has no Elroy component!
            systems.ghostState().setElroyEnabled(ghost, false);
        });

        // Stop bonus movement. Note: this works also if the bonus has no movement component!
        final Bonus bonus = level.entitySet().entities().anyOfTypeOrNull(Bonus.class);
        if (bonus != null) {
            systems.bonusMoveAndJump().setBonusInactive(bonus);
        }

        // End of dying animation triggers state timeout
        timer().resetToIndefiniteDuration();

        //TODO: needed? Scene controllers stop sounds already?
        game.eventManager().publishEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (stateTick == rules.pacDyingTiming().hideGhostsTick()) {
            level.entitySet().ghosts().forEach(GameEntity::hide);
            systems.pacAnimation().lockAnimation(pac, false);
            systems.pacAnimation().selectDyingAnimation(pac);
        }
        else if (stateTick == rules.pacDyingTiming().animationStartTick()) {
            systems.pacAnimation().startDyingAnimation(pac);
            game.eventManager().publishEvent(new PacDyingEvent(pac));
        }
        else if (stateTick == rules.pacDyingTiming().hidePacTick()) {
            pac.hide();
        }
        else if (stateTick == rules.pacDyingTiming().pacDeadTick()) {
            final Bonus bonus = level.entitySet().entities().anyOfTypeOrNull(Bonus.class);
            if (bonus != null) {
                level.entitySet().remove(bonus);
            }
            game.eventManager().publishEvent(new PacDeadEvent(pac));
        }

        if (timer().hasExpired()) {
            level.entitySet().ghosts().forEach(ghost -> ghost.worldNavigation().setPaused(false));
            session.setNumLives(session.numLives() - 1);
            flow.enterGameState(game, session.numLives() == 0
                ? CommonGameStateID.GAME_OVER
                : CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
