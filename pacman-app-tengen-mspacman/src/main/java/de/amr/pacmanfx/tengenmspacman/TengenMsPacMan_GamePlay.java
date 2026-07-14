/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.event.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.model.AbstractGameModel;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.HuntingTimer;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.world.*;
import de.amr.pacmanfx.core.simulation.CommonGamePlay;
import de.amr.pacmanfx.core.simulation.GamePlayContext;
import de.amr.pacmanfx.core.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.tengenmspacman.model.*;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;

    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

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
    public GameLevel createLevel(AbstractGameModel gameModel, int levelNumber, boolean demoLevel) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameModel;

        final WorldMap worldMap = model.mapSelector().supplyWorldMap(levelNumber, model.mapCategory());
        final TerrainLayer terrain = worldMap.terrainLayer();

        final ArcadeHouse house = new ArcadeHouse(TengenMsPacMan_GameModel.HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final var huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", model.rules().numHuntingPhases());

        final GameLevel level = new GameLevel(model, levelNumber, worldMap, huntingTimer, 3);
        level.setDemoLevel(demoLevel);

        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        });

        int index = levelNumber <= 19 ? levelNumber - 1 : 18;
        float powerSeconds = TengenMsPacMan_GameRules.POWER_PELLET_TIMES[index] / 16.0f;
        level.setPacPowerSeconds(powerSeconds);
        level.setPacPowerFadingSeconds(0.5f * 3);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        level.setGameOverStateTicks(model.mapCategory() == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        setMsPacMan(model, level);
        setGhosts(level, house);

        //TODO not sure about this:
        level.setBonusSymbolCode(0, model.rules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, model.rules().selectBonusSymbolCode(level.number(), 1));

        model.levelCounter().setEnabled(levelNumber < 8);

        return level;
    }

    // Helpers

    private void setMsPacMan(TengenMsPacMan_GameModel model, GameLevel level) {
        final Pac msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(model.automaticSteering());
        model.activatePacBooster(msPacMan, model.pacBoosterMode() == PacBooster.ALWAYS_ON);
        level.setPac(msPacMan);
    }

    private void setGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        level.setGhosts(
            TengenMsPacMan_ActorFactory.createGhost(GameModel.RED_GHOST_SHADOW,   house, terrain, WorldMapPropertyName.POS_GHOST_1_RED),
            TengenMsPacMan_ActorFactory.createGhost(GameModel.PINK_GHOST_SPEEDY,  house, terrain, WorldMapPropertyName.POS_GHOST_2_PINK),
            TengenMsPacMan_ActorFactory.createGhost(GameModel.CYAN_GHOST_BASHFUL, house, terrain, WorldMapPropertyName.POS_GHOST_3_CYAN),
            TengenMsPacMan_ActorFactory.createGhost(GameModel.ORANGE_GHOST_POKEY, house, terrain, WorldMapPropertyName.POS_GHOST_4_ORANGE)
        );
    }

    @Override
    public GameLevel buildDemoLevel(GamePlayContext playContext) {
        requireNonNull(playContext);

        final AbstractGameModel model = (AbstractGameModel) playContext.model();

        final GameLevel demoLevel = createLevel(model, 1, true);
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
    public void startLevel(GamePlayContext playContext) {
        requireNonNull(playContext);

        if (!(playContext.model() instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalArgumentException("Illegal model type");
        }
        GameLevel level = playContext.level();

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
    public void activateNextBonus(GamePlayContext playContext) {
        final GameModel model = playContext.model();
        final GameLevel level = playContext.level();
        final GameEventManager eventManager = playContext.eventManager();
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

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
