/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.HPortal;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.simulation.CommonGamePlay;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    public TengenMsPacMan_GamePlay() {}

    // Game start

    @Override
    public void init(GameModel model) {
        requireNonNull(model);
        model.init();
        resetForNewGame(model);
    }

    @Override
    public void resetForNewGame(GameModel model) {
        requireNonNull(model);

        if (!(model instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalArgumentException("Illegal model type");
        }

        super.resetForNewGame(model);
        tengenModel.setBoosterActive(false);
    }

    // Level building and level start

    @Override
    public GameLevel buildDemoLevel(GameEventManager eventManager, GameModel model) {
        requireNonNull(eventManager);
        requireNonNull(model);

        final GameLevel demoLevel = model.createLevel(1, true);
        demoLevel.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RuleBasedPacSteering();
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(1);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(1);

        return demoLevel;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    @Override
    public void startLevel(GameEventManager eventManager, GameLevel level) {
        requireNonNull(eventManager);
        requireNonNull(level);

        if (!(level.gameModel() instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalArgumentException("Illegal model type");
        }

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(level);

        // In Tengen, actors are shown immediately
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        if (tengenModel.pacBoosterMode() == PacBooster.ALWAYS_ON) {
            tengenModel.activatePacBooster(level.entities().pac(), true);
        }
        tengenModel.showMessage(level, GameLevelMessageType.READY);

        tengenModel.levelCounter().update(level.number(), level.bonusSymbolCode(0));
        tengenModel.score().setEnabled(true);

        //TODO fixme
        //context.cheats().update(level);
    }

    // Playing level

    @Override
    public void activateNextBonus(GameEventManager eventManager, GameLevel level) {
        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final GameModel model = level.gameModel();
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
        final Bonus bonus = new Bonus(symbolCode, model.rules().pointsForBonus(symbolCode));
        bonus.setMazeRoute(route, leftToRight);
        bonus.showEdibleAndStartWandering(model.rules().actorSpeedControl().bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }
}
