/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;


import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacGetsPowerEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay implements GamePlay {

    public TengenMsPacMan_GamePlay() {
    }

    @Override
    public void eatPellet(GameContext context, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = context.model();

        model.scorePoints(context, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
    }

    @Override
    public void eatEnergizer(GameContext context, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) context.model();

        model.scorePoints(context, model.rules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());

        level.clearGhostKillChain();

        startPacPowerMode(context, level, level.entities().pac());
    }

    @Override
    public void startPacPowerMode(GameContext context, GameLevel level, Pac pac) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            context.flow().publishGameEvent(new PacGetsPowerEvent(context, pac));
        }
    }
}
