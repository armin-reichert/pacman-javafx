/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.simulation.GamePlay;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomFloat;
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
    public void onEatGhost(GameContext context, GameLevel level, Ghost eatenGhost) {
        requireNonNull(context);
        requireNonNull(level);
        requireNonNull(eatenGhost);

        final GameModel model = context.model();

        final int killedBefore = level.ghostKillChainSize();
        final int points = model.rules().pointsForGhost(killedBefore);

        model.scorePoints(context, points, level.number());
        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        eatenGhost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        eatenGhost.animations().selectAndSetFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        level.addToGhostKillChain(eatenGhost);
        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());

        context.flow().publishGameEvent(new GhostEatenEvent(context, eatenGhost));
    }


    @Override
    public void activateNextBonus(GameContext context, GameLevel level) {
        requireNonNull(context);
        requireNonNull(level);

        final GameModel model = context.model();

        level.selectNextBonus();
        final int bonusSymbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(bonusSymbolCode, model.rules().pointsForBonus(bonusSymbolCode));
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, ArcadePacMan_GameModel.DEFAULT_BONUS_TILE);
        bonus.setPosition(WorldMap.halfTileRightOf(bonusTile));
        bonus.showEdibleForSeconds(randomFloat(9, 10));
        level.setBonus(bonus);
        context.flow().publishGameEvent(new BonusActivatedEvent(context, bonus));
    }

    @Override
    public void startPacPowerMode(GameContext context, GameLevel level, Pac pac) {
        requireNonNull(context);
        requireNonNull(level);
        requireNonNull(pac);

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

    @Override
    public void updatePacPowerMode(GameContext context, GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                context.flow().publishGameEvent(new PacPowerFadesEvent(context, pac));
            } else if (pac.powerTimer().hasExpired()) {
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.clearGhostKillChain();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                context.flow().publishGameEvent(new PacLostPowerEvent(context, pac));
            }
        }
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        return false;
    }

    @Override
    public void onLevelCompleted(GameLevel level) {
        requireNonNull(level);

        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_FULL);
        pac.setSpeed(0);
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().stopSelected();
            //TODO check in emulator if ghost animation is reset to normal
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.setSpeed(0);
        });
        level.optBonus().ifPresent(Bonus::setInactive);
    }

    // -----------------------------------------------

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
