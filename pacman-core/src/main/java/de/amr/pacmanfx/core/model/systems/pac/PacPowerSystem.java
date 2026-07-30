/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.pac.PacGetsPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacLostPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerFadesEvent;
import de.amr.pacmanfx.core.model.component.ghost.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.pac.PacPowerComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateSystem;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class PacPowerSystem {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(
        GhostState.FRIGHTENED, GhostState.HUNTING_PAC
    );

    public void update(GameContext gameContext, Pac pac) {
        requireNonNull(gameContext);
        requireNonNull(pac);

        final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState();

        final PacPowerComponent power = pac.power();
        final GameLevel level = gameContext.assertLevel();

        if (isPowerActive(pac)) {
            power.timer().doTick();
            if (isPowerStartingFading(level, pac)) {
                gameContext.eventManager().publishGameEvent(new PacPowerFadesEvent(pac));
            }
            else if (isPowerOver(pac)) {
                power.reset();
                level.clearGhostKillChain();

                // Resume hunting
                level.huntingRules().start();
                level.ghostsInState(GhostState.FRIGHTENED)
                    .forEach(ghost -> ghostStateSystem.changeState(gameContext, ghost, GhostState.HUNTING_PAC));

                gameContext.eventManager().publishGameEvent(new PacLostPowerEvent(pac));
            }
        }
    }

    public void start(GameContext gameContext, Pac pac) {
        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        level.ghostsInAnyOfStates(TURNBACK_STATES).forEach(sys.navigator()::requestTurnBack);

        final float seconds = level.pacPowerSeconds();
        if (seconds > 0) {
            level.huntingRules().stop();
            final long ticks = TickTimer.secToTicks(seconds);
            pac.power().timer().restartTicks(ticks);
            Logger.debug("Power timer activated, {} ticks ({0.00} sec)", ticks, seconds);
            level.ghostsInState(GhostState.HUNTING_PAC)
                .forEach(ghost -> sys.ghostState().changeState(gameContext, ghost, GhostState.FRIGHTENED));
            gameContext.eventManager().publishGameEvent(new PacGetsPowerEvent(pac));
        }
    }

    public void reset(Pac pac) {
        pac.power().reset();
    }

    public boolean isPowerActive(Pac pac) {
        return pac.power().timer().isRunning();
    }

    public boolean isPowerOver(Pac pac) {
        return pac.power().timer().hasExpired();
    }

    public boolean isPowerFading(GameLevel level, Pac pac) {
        final TickTimer timer = pac.power().timer();
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerStartingFading(GameLevel level, Pac pac) {
        final TickTimer timer = pac.power().timer();
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() == fadingTicks
            || timer.durationTicks() < fadingTicks && timer.tickCount() == 1;
    }

    public long powerTicksRemaining(Pac pac) {
        final TickTimer timer = pac.power().timer();
        return timer.isRunning() ? timer.remainingTicks() : 0;
    }

    public long powerTicksTotal(Pac pac) {
        final TickTimer timer = pac.power().timer();
        return timer.durationTicks();
    }
}
