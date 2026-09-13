/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusRouteInfo;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntities;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.*;
import de.amr.pacmanfx.core.rules.DefaultHuntingTimer;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.gamestate.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.sprites.NES_WorldMapColorScheme;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.basics.math.RandomNumbers.randomBoolean;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    public static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    public static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;
    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Tengen Ms. Pac-Man Game Flow");
        for (Tengen_GameState gameState : Tengen_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    // Tengen Ms. Pac-Man specific methods

    public static GamePlayOptions gameOptions(GameSession session) {
        return session.value(GamePlayOptions.Key.GAME_PLAY_OPTIONS, GamePlayOptions.class);
    }

    public static boolean checkGameContinuesOnGameOver(GameSession session) {
        requireNonNull(session);
        final GamePlayOptions options = gameOptions(session);

        if (options.startLevelNumber() < 10) {
            return false; // No continues for games started before 10th start level
        }

        if (options.numContinues() > 0) {
            options.setNumContinues(options.numContinues() - 1);
            return true;
        }

        //TODO This should be done elsewhere
        // Maximum number of continues reached: reset counter and return false (no further tries)
        options.setNumContinues(4);

        return false;
    }

    public TengenMsPacMan_GamePlay() {}

    @Override
    public boolean canStart(GameContext game) {
        requireNonNull(game);
        return gameOptions(game.session()).canStartNewGame();
    }

    @Override
    public void startSession(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();

        final GamePlayOptions options = new GamePlayOptions();
        options.setBoosterEnabled(false);
        options.setCanStartNewGame(false);

        session.setValue(GamePlayOptions.Key.GAME_PLAY_OPTIONS, options);
        session.setNumLives(game.variantPlayConfig().initialLifeCount());
        session.setCutScenesEnabled(true);
        session.setLevel(null);
        session.setGameRunning(false);

        final HUD hud = session.hud();
        hud.addEntity(new GameOptionsDisplay());
        // Level number boxes left and right side
        hud.addEntity(new LevelNumberDisplay());
        hud.addEntity(new LevelNumberDisplay());

        configureHUD(game, null, hud);

        initScores(game);

        game.variantPlayConfig().gameFlow().restartGameState(game, CommonGameStateID.BOOT);
    }

    // Level building and level start

    @Override
    public void configureHUD(GameContext game, GameLevel level, HUD hud) {
        requireNonNull(game);
        // level may be null!
        requireNonNull(hud);

        final LivesCounter livesCounter = hud.livesCounter();
        final LevelCounter levelCounter = hud.levelCounter();

        hud.gameScore().pos().set(4 * TS, TS);
        hud.highScore().pos().set(11 * TS, TS);
        hud.entities().theOne(GameOptionsDisplay.class).pos().set(16 * TS, 2.5f * TS); // horizontally centered

        if (level != null) {
            // Called when level is created, adjust positions to map size

            final int bottomPos = (level.worldMap().numRows() - 1) * TS;

            final var levelNumberDisplays = hud.entities().ofType(LevelNumberDisplay.class).toList();
            levelNumberDisplays.forEach(levelNumberDisplay -> levelNumberDisplay.levelNumber().setNumber(level.number()));
            if (levelNumberDisplays.size() != 2) {
                Logger.error("There should exist exactly 2 level number displays in this HUD!");
            }
            else {
                levelNumberDisplays.getFirst().pos().set(2 * TS, bottomPos);
                levelNumberDisplays.getLast().pos().set(28 * TS, bottomPos);
            }
            livesCounter.pos().set(4 * TS, bottomPos);
            levelCounter.pos().set(26 * TS - 2, bottomPos);
        }
        else {
            // Called when session is started, initialize

            livesCounter.data().setNumLivesShown(game.variantPlayConfig().initialLifeCount());
            livesCounter.data().setMaxLivesShown(5);

            levelCounter.data().setBehavior(LevelCounterBehavior.DISABLE_WHEN_FULL);
            levelCounter.data().setCapacity(7);
            levelCounter.data().setEnabled(true);
            game.variantPlayConfig().systems().levelCounterSystem().clear(levelCounter);
        }
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameSession session = game.session();
        final MapCategory mapCategory = gameOptions(session).mapCategory();

        final var rules = (TengenMsPacMan_GameRules) game.variantPlayConfig().rules();
        final GameSystems systems = game.variantPlayConfig().systems();
        final var entities = new GameLevelEntities();

        final WorldMap worldMap = game.variantPlayConfig().worldMapManager().supplyWorldMap(levelNumber, mapCategory);

        rules.setMapCategory(mapCategory);
        Logger.info("Using game rules for map category {}", mapCategory);

        final var huntingTimer = new DefaultHuntingTimer("Tengen Ms. Pac-Man Hunting Timer", rules.numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                entities.ghostsInAnyOfStates(TURNBACK_STATES)
                    .forEach(systems.navigator()::requestTurnBack);
            }
        });
        huntingTimer.reset();

        //TODO Tengen uses another logic for the house
        final var gateKeeper = new ArcadeHouseGateKeeper(levelNumber);
        gateKeeper.setGhostReleasedCallback((_, ghost) ->
            Logger.info("Ghost {} released from house", ghost.name()));

        createAndAddEntities(entities, session, worldMap);

        final GameLevel level = new GameLevel(levelNumber);
        level.setWorldMap(worldMap);
        level.setEntities(entities);
        level.setFoodState(new FoodState(worldMap.foodLayer()));
        level.setGateKeeper(gateKeeper);
        level.setHuntingTimer(huntingTimer);
        level.setHeartbeat(new Pulse(10, Pulse.State.OFF));
        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        configurePacAndGhosts(entities, game.variantPlayConfig().systems(), worldMap.terrainLayer());
        configureHUD(game, level, session.hud());

        session.setLevel(level);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        session.setGameOverStateTicks(gameOptions(session).mapCategory() == MapCategory.ARCADE
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

    private void configurePacAndGhosts(GameLevelEntities entities, GameSystems systems, TerrainLayer terrain) {
        entities.pac().autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.navigator(), systems.pacWorldMovementPolicy()
        ));

        final House house = entities.house();
        entities.ghost(GhostPersonality.RED_GHOST_SHADOW)  .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        entities.ghost(GhostPersonality.PINK_GHOST_SPEEDY) .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        entities.ghost(GhostPersonality.CYAN_GHOST_BASHFUL).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        entities.ghost(GhostPersonality.ORANGE_GHOST_POKEY).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variantPlayConfig().systems();

        final GameLevel demoLevel = createLevel(game, 1);

        session.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        // There are maps that cannot be handled with Arcade-steering logic
        pac.autoSteering().setSteering(new RuleGuidedPacSteering(systems.navigator(), systems.pacWorldMovementPolicy()));
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        session.hud().gameScore().data().setLevelNumber(demoLevel.number());
        session.hud().gameScore().data().setEnabled(true);

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

        final LevelCounterSystem levelCounterSystem = game.variantPlayConfig().systems().levelCounterSystem();
        final LevelCounter levelCounter = session.hud().levelCounter();
        levelCounterSystem.updateCounter(levelCounter, level.number(), level.bonusSymbolCode(0));

        level.showMessage(MessageType.READY);

        //TODO Check in emulator the sequence when actors etc. get visible
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        // Note: This event is very important because it triggers the creation of the actor animations!
        game.eventManager().publishGameEvent(new LevelStartedEvent(level.number()));
    }

    // Playing level

    @Override
    public void activateNextBonus(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameSystems systems = game.variantPlayConfig().systems();
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
        final float speed = game.variantPlayConfig().rules().actorSpeedRules().bonusSpeed(game, level);

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

        game.eventManager().publishGameEvent(new BonusActivatedEvent(bonus));
    }

    private MessageView createMessageView(House house, GameSession session, NES_WorldMapColorScheme colorScheme) {
        final var messageView = new MessageView();

        // Messages appear centered under house
        final Vector2i houseSize = house.sizeInTiles();
        float x = TS * (house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float y = TS * (house.floorplan().minTile().y() + houseSize.y() + 1);
        messageView.pos().set(x, y);

        messageView.setComp(MessageViewStyleComp.class, createMessageViewStyleComp(session, colorScheme));

        return messageView;
    }

    private MessageViewStyleComp createMessageViewStyleComp(GameSession session, NES_WorldMapColorScheme colorScheme) {
        final var style = new MessageViewStyleComp();
        style.setMessageFont(GlobalAssets.Fonts.ARCADE.font());
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
