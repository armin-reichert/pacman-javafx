/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;


import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.HPortal;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay implements GamePlay {

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

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
        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        final House house = terrain.optHouse().orElse(null);
        if (house == null) {
            Logger.error("\"Cannot activate next bonus: No house exists in game level!");
            return;
        }

        if (terrain.horizontalPortals().isEmpty()) {
            Logger.error("Cannot activate next bonus: No portal exists in game level");
            return;
        }

        final Vector2i houseEntry = WorldMap.computeTileAt(house.entryPosition());
        final Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);

        final List<HPortal> portals = terrain.horizontalPortals();
        final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
        final HPortal exitPortal  = portals.get(randomInt(0, portals.size()));

        final boolean leftToRight = randomBoolean();
        final List<Vector2i> route = List.of(
            leftToRight ? entryPortal.leftBorderEntryTile() : entryPortal.rightBorderEntryTile(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightBorderEntryTile().plus(1, 0) : exitPortal.leftBorderEntryTile().minus(1, 0)
        );

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final Bonus bonus = new Bonus(symbolCode, context.model().rules().pointsForBonus(symbolCode));
        bonus.setMazeRoute(route, leftToRight);
        bonus.showEdibleAndStartWandering(context.model().actorSpeedControl().bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        context.flow().publishGameEvent(new BonusActivatedEvent(context, bonus));
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
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
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

    @Override
    public boolean isDemoLevelRunning(GameContext context) {
        return context.model().optGameLevel().isPresent() && context.model().assertLevel().isDemoLevel();
    }
}
