/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusRouteInfo;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.rules.HuntingTimer;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_ActorSpeedRules;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumbers.randomBoolean;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    public static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    public static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    public TengenMsPacMan_GamePlay() {}

    // Tengen Ms. Pac-Man specific methods

    public boolean allOptionsHaveDefaultValue(GameSession session) {
        final BoosterMode boosterMode = session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
        final Difficulty difficulty = session.value(TengenMsPacMan_GamePlayOptions.DIFFICULTY, Difficulty.class);
        final MapCategory mapCategory = session.value(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, MapCategory.class);
        final int startLevel = session.value(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
        final int numContinues = session.value(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, Integer.class);

        return boosterMode == TengenMsPacMan_GameVariantUIConfig.DEFAULT_PAC_BOOSTER
            && difficulty == TengenMsPacMan_GameVariantUIConfig.DEFAULT_DIFFICULTY
            && mapCategory == TengenMsPacMan_GameVariantUIConfig.DEFAULT_MAP_CATEGORY
            && startLevel == TengenMsPacMan_GameVariantUIConfig.DEFAULT_START_LEVEL
            && numContinues == TengenMsPacMan_GameVariantUIConfig.DEFAULT_NUM_CONTINUES;
    }

    public void setBoosterOn(GameContext game, Pac pac, boolean boosterOn) {
        requireNonNull(game);
        requireNonNull(pac);

        final GameSession session = game.session();
        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, boosterOn);

        //TODO this is currently broken! Sprite is reset when Ms. Pac-Man moves!
        final ActorSpriteAnimController animSystem = game.variant().systems().actorSpriteAnimController();
        animSystem.select(pac, boosterOn ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
    }

    public void setBoosterMode(GameSession session, BoosterMode boosterMode) {
        requireNonNull(session);
        requireNonNull(boosterMode);

        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, boosterMode);
    }

    public BoosterMode boosterMode(GameSession session) {
        return session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
    }

    public void setMapCategory(GameSession session, MapCategory mapCategory) {
        requireNonNull(session);
        requireNonNull(mapCategory);

        session.setValue(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, mapCategory);
    }

    public MapCategory mapCategory(GameSession session) {
        return session.value(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, MapCategory.class);
    }

    public void setDifficulty(GameContext game, Difficulty difficulty) {
        requireNonNull(game);
        requireNonNull(difficulty);

        game.session().setValue(TengenMsPacMan_GamePlayOptions.DIFFICULTY, difficulty);

        //TODO this should also move into session!
        final var speedRules = (TengenMsPacMan_ActorSpeedRules) game.variant().rules().actorSpeedRules();
        speedRules.setDifficulty(difficulty);
    }

    public Difficulty difficulty(GameSession session) {
        requireNonNull(session);

        return session.value(TengenMsPacMan_GamePlayOptions.DIFFICULTY, Difficulty.class);
    }

    public void setStartLevelNumber(GameSession session, int number) {
        requireNonNull(session);
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL ||
            number > TengenMsPacMan_GameRules.LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }

        session.setValue(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, number);
    }

    public int startLevelNumber(GameSession session) {
        requireNonNull(session);

        return session.value(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
    }

    public void setNumContinues(GameSession session, int numContinues) {
        requireNonNull(session);

        session.setValue(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, numContinues);
    }

    public int numContinues(GameSession session) {
        requireNonNull(session);

        return session.value(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, Integer.class);
    }

    public boolean checkGameContinuesOnGameOver(GameSession session) {
        requireNonNull(session);

        if (startLevelNumber(session) < 10) {
            return false; // No continues possible for first 9 levels
        }

        final int numContinuesLeft = numContinues(session);
        if (numContinuesLeft > 0) {
            setNumContinues(session, numContinuesLeft - 1);
            return true;
        }

        // Maximum number of continues reached: reset counter and return false (no further tries)
        setNumContinues(session, 4);
        return false;
    }

    public void setBoosterOn(GameSession session, boolean boosterOn) {
        requireNonNull(session);

        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, boosterOn);
    }

    public boolean isBoosterOn(GameSession session) {
        requireNonNull(session);

        return session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, Boolean.class);
    }

    public boolean canStartNewGame(GameSession session) {
        requireNonNull(session);

        return session.value(TengenMsPacMan_GamePlayOptions.CAN_START_GAME, Boolean.class);
    }

    public void setCanStartNewGame(GameSession session, boolean canStartNewGame) {
        requireNonNull(session);

        session.setValue(TengenMsPacMan_GamePlayOptions.CAN_START_GAME, canStartNewGame);
    }

    // GamePlay interface

    @Override
    public boolean canStart(GameContext game) {
        return canStartNewGame(game.session());
    }

    @Override
    public void startSession(GameContext game) {
        requireNonNull(game);
        final GameSession session = game.session();

        setBoosterOn(session, false);

        setBoosterMode(session,      TengenMsPacMan_GameVariantUIConfig.DEFAULT_PAC_BOOSTER);
        setDifficulty(game,          TengenMsPacMan_GameVariantUIConfig.DEFAULT_DIFFICULTY);
        setMapCategory(session,      TengenMsPacMan_GameVariantUIConfig.DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(session, TengenMsPacMan_GameVariantUIConfig.DEFAULT_START_LEVEL);
        setNumContinues(session,     TengenMsPacMan_GameVariantUIConfig.DEFAULT_NUM_CONTINUES);

        session.setNumLives(game.variant().initialLifeCount());
        session.setCutScenesEnabled(true);
        session.setLevel(null);
        session.setGameRunning(false);
        initScores(session);
        configureLevelCounter(game, session.hud().levelCounter());

        game.variant().gameFlow().restartGameState(game, CommonGameStateID.BOOT);
    }

    // Level building and level start

    @Override
    public void configureLevelCounter(GameContext game, LevelCounter levelCounter) {
        final LevelCounterSystem system = game.variant().systems().levelCounterSystem();
        system.setCounterBehavior(levelCounter, LevelCounterBehavior.DISABLE_WHEN_FULL);
        system.setCounterCapacity(levelCounter, 7);
        system.clear(levelCounter);
        system.enableCounter(levelCounter, true);
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        final GameLevelEntitySet entities = new GameLevelEntitySet();

        final GameSession session = game.session();
        final WorldNavigationSystem navigator = game.variant().systems().worldNavigator();

        final MapCategory mapCategory = mapCategory(session);

        final WorldMap worldMap = game.variant().worldMapManager().supplyWorldMap(levelNumber, mapCategory);

        final TengenMsPacMan_GameRules rules = (TengenMsPacMan_GameRules) game.variant().rules();
        rules.setMapCategory(mapCategory);
        Logger.info("Using game rules for map category {}", mapCategory);

        final HuntingTimer huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", rules.numHuntingPhases());

        addEntities(entities, game, worldMap);

        final GameLevel level = new GameLevel(levelNumber, worldMap, entities, huntingTimer);

        session.setLevel(level);
        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        session.setGameOverStateTicks(mapCategory(session) == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.entities().ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(navigator::requestTurnBack);
            }
        });

        return level;
    }

    private void addEntities(GameLevelEntitySet entities, GameContext game, WorldMap worldMap) {
        final House house = HouseFactory.createArcadeHouse(TengenMsPacMan_GameVariantUIConfig.HOUSE_MIN_TILE);

        final var actorFactory  = TengenMsPacMan_ActorFactory.instance();
        final Pac msPacMan      = actorFactory.createMsPacMan();
        final Ghost redGhost    = actorFactory.createRedGhost();
        final Ghost pinkGhost   = actorFactory.createPinkGhost();
        final Ghost cyanGhost   = actorFactory.createCyanGhost();
        final Ghost orangeGhost = actorFactory.createOrangeGhost();

        entities.add(house);
        entities.add(msPacMan);
        entities.add(redGhost);
        entities.add(pinkGhost);
        entities.add(cyanGhost);
        entities.add(orangeGhost);

        // Configure entities

        final GameSystems systems = game.variant().systems();
        msPacMan.autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.worldNavigator(), systems.pacWorldMovementPolicy()
        ));

        final TerrainLayer terrain = worldMap.terrainLayer();
        redGhost   .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        pinkGhost  .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        cyanGhost  .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        orangeGhost.worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variant().systems();

        final GameLevel demoLevel = createLevel(game, 1);

        session.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        final var steering = new RuleGuidedPacSteering(
            systems.worldNavigator(),
            systems.pacWorldMovementPolicy()
        );
        pac.autoSteering().setSteering(steering);

        session.setLevel(demoLevel);
        session.setAttractMode(true);

        ScoreSystem.setLevelNumber(session.hud().gameScore(), 1);

        return demoLevel;
    }

    @Override
    public void startLevel(GameContext game, GameLevel level) {
        requireNonNull(game);

        final GameSession session = game.session();

        prepareLevelForPlaying(game, level);

        session.setLevelStartTimeMillis(System.currentTimeMillis());
        session.hud().gameScore().data().setEnabled(true);
        session.cheats().update(game);

        final LevelCounterSystem levelCounterSystem = game.variant().systems().levelCounterSystem();
        final LevelCounter levelCounter = session.hud().levelCounter();
        levelCounterSystem.updateCounter(levelCounter, level.number(), level.bonusSymbolCode(0));

        showMessage(game, MessageType.READY);

        final Pac pac = level.entities().pac();
        final boolean boosterOn = boosterMode(session) == BoosterMode.BOOSTER_ALWAYS_ON;
        setBoosterOn(game, pac, boosterOn);

        // Actors are shown immediately when level starts!
        pac.show();
        level.entities().ghosts().forEach(GameEntity::show);

        // Note: This event is very important because it triggers the creation of the actor animations!
        game.eventManager().publishGameEvent(new LevelStartedEvent(level.number()));
    }

    // Playing level

    @Override
    public void activateNextBonus(GameContext game, GameLevel level) {
        final GameSystems systems = game.variant().systems();
        final GameEventManager eventManager = game.eventManager();
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        //TODO Find out how Tengen really implemented this
        if (level.entities().optBonus().isPresent() && level.entities().optBonus().get().bonusState() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        if (terrain.horizontalPortals().isEmpty()) {
            Logger.error("Cannot activate next bonus: No portal exists in game level");
            return;
        }

        final House house = level.entities().house();
        final Vector2i houseEntry = PositionSystem.computeTileAt(house.floorplan().entryPosition());
        final Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);

        final List<HPortal> portals = terrain.horizontalPortals();
        final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
        final HPortal exitPortal  = portals.get(randomInt(0, portals.size()));

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final int value = game.variant().rules().scoringRules().pointsForBonus(symbolCode);
        final float speed = game.variant().rules().actorSpeedRules().bonusSpeed(game, level);

        final Bonus bonus = Bonus.createMovingBonus(symbolCode, value);
        level.entities().optBonus().ifPresent(oldBonus -> level.entities().remove(oldBonus));
        level.entities().add(bonus);

        systems.bonusState().showEdible(bonus);

        final boolean leftToRight = randomBoolean();
        final List<Vector2i> waypoints = List.of(
            leftToRight ? entryPortal.leftBorderEntryTile() : entryPortal.rightBorderEntryTile(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightBorderEntryTile().plus(1, 0) : exitPortal.leftBorderEntryTile().minus(1, 0)
        );
        systems.bonusMoveAndJump().startWandering(bonus, new BonusRouteInfo(leftToRight, waypoints), speed);

        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }
}
