/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.model.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.event.pac.PacGetsPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacLostPowerEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerFadesEvent;
import de.amr.pacmanfx.core.model.entities.ghost.GhostState;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class PacPowerSystem {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(
        GhostState.FRIGHTENED, GhostState.HUNTING_PAC
    );

    public void update(GameContext gameContext, GameEntity pac) {
        requireNonNull(gameContext);
        requireNonNull(pac);

        final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState();

        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
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

    public void start(GameContext gameContext, GameEntity pac) {
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

    public void reset(GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        power.reset();
    }

    public boolean isPowerActive(GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        return power.timer().isRunning();
    }

    public boolean isPowerOver(GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        return power.timer().hasExpired();
    }

    public boolean isPowerFading(GameLevel level, GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        final TickTimer timer = power.timer();
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerStartingFading(GameLevel level, GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        final TickTimer timer = power.timer();
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() == fadingTicks
            || timer.durationTicks() < fadingTicks && timer.tickCount() == 1;
    }

    public long powerTicksRemaining(GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        final TickTimer timer = power.timer();
        return timer.isRunning() ? timer.remainingTicks() : 0;
    }

    public long powerTicksTotal(GameEntity pac) {
        final PacPowerComp power = pac.requireComponent(PacPowerComp.class);
        return power.timer().durationTicks();
    }
}
