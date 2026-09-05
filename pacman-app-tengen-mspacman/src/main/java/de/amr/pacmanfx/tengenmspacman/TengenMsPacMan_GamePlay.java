/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.*;
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
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntities;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.rules.DefaultHuntingTimer;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.gamestate.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_ActorSpeedRules;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.sprites.NES_WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumbers.randomBoolean;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    public static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    public static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;
    public static final int DEFAULT_START_LEVEL = 1;
    public static final int DEFAULT_NUM_CONTINUES = 4;
    public static final BoosterMode DEFAULT_PAC_BOOSTER = BoosterMode.BOOSTER_OFF;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;
    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Tengen Ms. Pac-Man Game Flow");
        for (Tengen_GameState gameState : Tengen_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    // Tengen Ms. Pac-Man specific methods

    public static boolean allOptionsHaveDefaultValue(GameSession session) {
        final BoosterMode boosterMode = session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
        final Difficulty difficulty = session.value(TengenMsPacMan_GamePlayOptions.DIFFICULTY, Difficulty.class);
        final MapCategory mapCategory = session.value(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, MapCategory.class);
        final int startLevel = session.value(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
        final int numContinues = session.value(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, Integer.class);

        return boosterMode == DEFAULT_PAC_BOOSTER
            && difficulty == DEFAULT_DIFFICULTY
            && mapCategory == DEFAULT_MAP_CATEGORY
            && startLevel == DEFAULT_START_LEVEL
            && numContinues == DEFAULT_NUM_CONTINUES;
    }

    public static void setBoosterOn(GameContext game, Pac pac, boolean boosterOn) {
        requireNonNull(game);
        requireNonNull(pac);

        final GameSession session = game.session();
        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, boosterOn);

        //TODO this is currently broken! Sprite is reset when Ms. Pac-Man moves!
        final ActorSpriteAnimController animSystem = game.variant().systems().actorSpriteAnimController();
        animSystem.select(pac, boosterOn ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
    }

    public static void setBoosterMode(GameSession session, BoosterMode boosterMode) {
        requireNonNull(session);
        requireNonNull(boosterMode);
        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, boosterMode);
    }

    public static BoosterMode boosterMode(GameSession session) {
        return session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
    }

    public static void setMapCategory(GameSession session, MapCategory mapCategory) {
        requireNonNull(session);
        requireNonNull(mapCategory);
        session.setValue(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, mapCategory);
    }

    public static MapCategory mapCategory(GameSession session) {
        return session.value(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, MapCategory.class);
    }

    public static void setDifficulty(GameContext game, Difficulty difficulty) {
        requireNonNull(game);
        requireNonNull(difficulty);
        game.session().setValue(TengenMsPacMan_GamePlayOptions.DIFFICULTY, difficulty);
        //TODO this should also move into session!
        final var speedRules = (TengenMsPacMan_ActorSpeedRules) game.variant().rules().actorSpeedRules();
        speedRules.setDifficulty(difficulty);
    }

    public static Difficulty difficulty(GameSession session) {
        requireNonNull(session);
        return session.value(TengenMsPacMan_GamePlayOptions.DIFFICULTY, Difficulty.class);
    }

    public static void setStartLevelNumber(GameSession session, int number) {
        requireNonNull(session);
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL ||
            number > TengenMsPacMan_GameRules.LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }
        session.setValue(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, number);
    }

    public static int startLevelNumber(GameSession session) {
        requireNonNull(session);
        return session.value(TengenMsPacMan_GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
    }

    public static void setNumContinues(GameSession session, int numContinues) {
        requireNonNull(session);
        session.setValue(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, numContinues);
    }

    public static int numContinues(GameSession session) {
        requireNonNull(session);
        return session.value(TengenMsPacMan_GamePlayOptions.NUM_CONTINUES, Integer.class);
    }

    public static boolean checkGameContinuesOnGameOver(GameSession session) {
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

    public static void setBoosterOn(GameSession session, boolean boosterOn) {
        requireNonNull(session);
        session.setValue(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, boosterOn);
    }

    public static boolean isBoosterOn(GameSession session) {
        requireNonNull(session);
        return session.value(TengenMsPacMan_GamePlayOptions.BOOSTER_ON, Boolean.class);
    }

    public static boolean canStartNewGame(GameSession session) {
        requireNonNull(session);
        return session.value(TengenMsPacMan_GamePlayOptions.CAN_START_GAME, Boolean.class);
    }

    public static void setCanStartNewGame(GameSession session, boolean canStartNewGame) {
        requireNonNull(session);
        session.setValue(TengenMsPacMan_GamePlayOptions.CAN_START_GAME, canStartNewGame);
    }

    // HUD extras

    public static void setHUD_Option(GameSession session, TengenMsPacMan_HUD_Options option, boolean value) {
        requireNonNull(session);
        session.setValue(option, value);
    }

    public static boolean hasHUD_Option(GameSession session, TengenMsPacMan_HUD_Options option) {
        return session.value(option, Boolean.class);
    }

    public TengenMsPacMan_GamePlay() {}

    // GamePlay interface

    @Override
    public boolean canStart(GameContext game) {
        return canStartNewGame(game.session());
    }

    @Override
    public void startSession(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();

        setBoosterMode(session,      DEFAULT_PAC_BOOSTER);
        setDifficulty(game,          DEFAULT_DIFFICULTY);
        setMapCategory(session,      DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(session, DEFAULT_START_LEVEL);
        setNumContinues(session,     DEFAULT_NUM_CONTINUES);

        setBoosterOn(session, false);

        final int numLives = game.variant().initialLifeCount();
        session.setNumLives(numLives);

        // HUD
        final LivesCounter livesCounter = session.hud().livesCounter();
        livesCounter.data().setNumLives(numLives);
        livesCounter.data().setMaxLivesShown(5);

        configureLevelCounter(game, game.variant().systems().levelCounterSystem(), session.hud().levelCounter());

        initScores(game);

        addHUDElements(session.hud());

        session.setCutScenesEnabled(true);
        session.setLevel(null);
        session.setGameRunning(false);

        game.variant().gameFlow().restartGameState(game, CommonGameStateID.BOOT);
    }

    // Level building and level start

    @Override
    public void configureLevelCounter(GameContext game, LevelCounterSystem levelCounterSystem, LevelCounter levelCounter) {
        levelCounter.pos().set(26 * TS, 35 * TS); //TODO depends on map height!
        levelCounter.data().setBehavior(LevelCounterBehavior.DISABLE_WHEN_FULL);
        levelCounter.data().setCapacity(7);
        levelCounter.data().setEnabled(true);
        levelCounterSystem.clear(levelCounter);
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        final GameLevelEntities entities = new GameLevelEntities();

        final GameSession session = game.session();
        final WorldNavigationSystem navigator = game.variant().systems().navigator();

        final MapCategory mapCategory = mapCategory(session);

        final WorldMap worldMap = game.variant().worldMapManager().supplyWorldMap(levelNumber, mapCategory);

        final TengenMsPacMan_GameRules rules = (TengenMsPacMan_GameRules) game.variant().rules();
        rules.setMapCategory(mapCategory);
        Logger.info("Using game rules for map category {}", mapCategory);

        final var huntingTimer = new DefaultHuntingTimer("Tengen Ms. Pac-Man Hunting Timer", rules.numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                entities.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(navigator::requestTurnBack);
            }
        });

        createAndAddEntities(entities, session, worldMap);
        configurePacAndGhosts(entities, game.variant().systems(), worldMap.terrainLayer(), entities.house());

        final GameLevel level = new GameLevel(levelNumber, worldMap, entities, huntingTimer);

        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        configureHUD(session.hud(), worldMap, levelNumber);

        session.setLevel(level);
        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        session.setGameOverStateTicks(mapCategory(session) == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        return level;
    }

    private void createAndAddEntities(GameLevelEntities entities, GameSession session, WorldMap worldMap) {
        final House house = HouseFactory.createArcadeHouse(HOUSE_MIN_TILE);
        final NES_WorldMapColorScheme colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final MessageView messageView = createMessageView(house, session, colorScheme);

        final var actorFactory  = TengenMsPacMan_ActorFactory.instance();
        final Pac msPacMan      = actorFactory.createMsPacMan();
        final Ghost redGhost    = actorFactory.createRedGhost();
        final Ghost pinkGhost   = actorFactory.createPinkGhost();
        final Ghost cyanGhost   = actorFactory.createCyanGhost();
        final Ghost orangeGhost = actorFactory.createOrangeGhost();

        entities.add(house);
        entities.add(messageView);
        entities.add(msPacMan);
        entities.add(redGhost);
        entities.add(pinkGhost);
        entities.add(cyanGhost);
        entities.add(orangeGhost);
    }

    private void configurePacAndGhosts(GameLevelEntities entities, GameSystems systems, TerrainLayer terrain, House house) {
        entities.pac().autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.navigator(), systems.pacWorldMovementPolicy()
        ));

        entities.ghost(GhostPersonality.RED_GHOST_SHADOW)  .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        entities.ghost(GhostPersonality.PINK_GHOST_SPEEDY) .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        entities.ghost(GhostPersonality.CYAN_GHOST_BASHFUL).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        entities.ghost(GhostPersonality.ORANGE_GHOST_POKEY).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    private void configureHUD(HUD hud, WorldMap worldMap, int levelNumber) {
        hud.levelCounter().pos().set(26 * TS, (worldMap.numRows() - 1) * TS);

        final var levelNumberDisplays = hud.entities().selectAllOfType(LevelNumberDisplay.class).toList();

        final LevelNumberDisplay either = levelNumberDisplays.getFirst();
        either.levelNumber().setNumber(levelNumber);
        either.pos().set(2 * TS, (worldMap.numRows() - 1) * TS);

        final LevelNumberDisplay other = levelNumberDisplays.getLast();
        other.levelNumber().setNumber(levelNumber);
        other.pos().set(28 * TS, (worldMap.numRows() - 1) * TS);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variant().systems();

        final GameLevel demoLevel = createLevel(game, 1);

        session.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        // There are maps that cannot be handled with Arcade-steering logic
        pac.autoSteering().setSteering(new RuleGuidedPacSteering(systems.navigator(), systems.pacWorldMovementPolicy()));
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        session.hud().gameScore().data().setLevelNumber(demoLevel.number());

        session.setLevel(demoLevel);
        session.setAttractMode(true);

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

        level.showMessage(MessageType.READY);

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
        final Bonus prevBonus = level.entities().optBonus().orElse(null);
        if (prevBonus != null) {
            if (prevBonus.state().enumValue() == BonusState.EDIBLE) {
                Logger.info("Previous bonus is still active, skip new bonus");
                return;
            }
            level.entities().remove(prevBonus);
        }

        final House house = level.entities().house();
        final Vector2i houseEntry = PositionSystem.computeTileAt(house.floorplan().entryPosition());
        final Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);

        final List<HPortal> portals = terrain.horizontalPortals();
        final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
        final HPortal exitPortal  = portals.get(randomInt(0, portals.size()));

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final float speed = game.variant().rules().actorSpeedRules().bonusSpeed(game, level);

        final Bonus bonus = Bonus.createMovingBonus(symbolCode);
        level.entities().optBonus().ifPresent(oldBonus -> level.entities().remove(oldBonus));
        level.entities().add(bonus);
        systems.bonusState().setEdible(bonus);
        bonus.show();

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

    private void addHUDElements(HUD hud) {
        final GameOptionsDisplay gameOptionsDisplay = new GameOptionsDisplay();
        hud.addEntity(gameOptionsDisplay);

        final LevelNumberDisplay leftLevelNumberDisplay = new LevelNumberDisplay();
        hud.addEntity(leftLevelNumberDisplay);

        final LevelNumberDisplay rightLevelNumberDisplay = new LevelNumberDisplay();
        hud.addEntity(rightLevelNumberDisplay);
    }

    private MessageView createMessageView(House house, GameSession session, NES_WorldMapColorScheme colorScheme) {
        final var messageView = new MessageView();

        // Messages appear centered under house
        final Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        messageView.pos().set(cx, cy);

        messageView.setComp(MessageViewStyleComp.class, createMessageViewStyleComp(session, colorScheme));

        return messageView;
    }

    private MessageViewStyleComp createMessageViewStyleComp(GameSession session, NES_WorldMapColorScheme colorScheme) {
        final var style = new MessageViewStyleComp();
        style.setMessageFont(GlobalAssets.Fonts.ARCADE8.font());
        style.setMessageColor(type -> computeMessageColor(type, session, colorScheme));
        return style;
    }

    private Color computeMessageColor(MessageType type, GameSession session, NES_WorldMapColorScheme colorScheme) {
        return switch (type) {
            case NO_MESSAGE -> null; //TODO delete this message type
            case READY -> NES_Palette.color(0x28);
            case GAME_OVER ->session.isAttractMode() ? Color.valueOf(colorScheme.wallStroke()) : NES_Palette.color(0x11);
        };
    }
}
