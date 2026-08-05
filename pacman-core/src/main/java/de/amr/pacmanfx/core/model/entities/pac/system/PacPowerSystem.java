/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.pac.PacGetsPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacLostPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerFadesEvent;
import de.amr.pacmanfx.core.model.entities.ghost.GhostState;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class PacPowerSystem {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(
        GhostState.FRIGHTENED, GhostState.HUNTING_PAC
    );

    //TODO This is not a "system" method but game flow
    public void update(GameContext gameContext, Pac pac) {
        requireNonNull(gameContext);
        requireNonNull(pac);

        final PacPowerComp power = pac.power();
        final GameLevel level = gameContext.assertLevel();

        if (power.isPowerActive()) {
            power.timer().doTick();
            if (power.isPowerStartingFading(level)) {
                gameContext.eventManager().publishGameEvent(new PacPowerFadesEvent(pac));
            }
            else if (power.isPowerOver()) {
                power.reset();
                level.clearGhostKillChain();

                // Resume hunting
                level.huntingRules().start();
                level.ghostsInState(GhostState.FRIGHTENED)
                    .forEach(ghost -> gameContext.systems().ghostState().changeState(gameContext, ghost, GhostState.HUNTING_PAC));

                gameContext.eventManager().publishGameEvent(new PacLostPowerEvent(pac));
            }
        }
    }

    //TODO This is not a "system" method but game flow
    public void start(GameContext gameContext, Pac pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);

        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        level.ghostsInAnyOfStates(TURNBACK_STATES).forEach(sys.worldNavigator()::requestTurnBack);

        final float seconds = level.pacPowerSeconds();
        if (seconds > 0) {
            level.huntingRules().stop();
            final long ticks = TickTimer.secToTicks(seconds);
            power.timer().restartTicks(ticks);
            Logger.debug("Power timer activated, {} ticks ({0.00} sec)", ticks, seconds);
            level.ghostsInState(GhostState.HUNTING_PAC)
                .forEach(ghost -> sys.ghostState().changeState(gameContext, ghost, GhostState.FRIGHTENED));
            gameContext.eventManager().publishGameEvent(new PacGetsPowerEvent(pac));
        }
    }

    public void reset(Pac pac) {
        pac.power().reset();
    }
}
