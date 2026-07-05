/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;


import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.event.PacGetsPowerEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.simulation.GamePlay;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GamePlay implements GamePlay {

    public ArcadePacMan_GamePlay() {
    }

    @Override
    public void eatPellet(GameContext context, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = context.model();

        model.scorePoints(context, model.rules().pointsForPellet(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.entities().pac().setRestingTicks(model.rules().restingTicksForPellet());
        checkRedGhostCruiseElroyActivation(level);
    }

    @Override
    public void eatEnergizer(GameContext context, GameLevel level, Vector2i tile) {
        requireNonNull(context);
        requireNonNull(level);
        requireNonNull(tile);

        final GameModel model = context.model();

        model.scorePoints(context, model.rules().pointsForEnergizer(), level.number());
        model.gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());

        final Pac pac = level.entities().pac();
        pac.setRestingTicks(model.rules().restingTicksForEnergizer());

        checkRedGhostCruiseElroyActivation(level);

        level.clearGhostKillChain();

        startPacPowerMode(context, level, pac);
    }

    @Override
    public void eatBonus(GameContext context, GameLevel level, Bonus bonus) {
        requireNonNull(context);
        requireNonNull(level);
        requireNonNull(bonus);

        final GameModel model = context.model();

        model.scorePoints(context, bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(model.rules().eatenBonusDisplaySeconds());

        context.flow().publishGameEvent(new BonusEatenEvent(context, bonus));
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

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final Ghost redGhost = level.ghost(GameModel.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroy().setBoost(Elroy.Boost.MEDIUM);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroy().setBoost(Elroy.Boost.LARGE);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }
}
