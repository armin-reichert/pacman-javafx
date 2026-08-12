/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.gameplay.CommonGamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessage;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.rules.HuntingTimer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import de.amr.pacmanfx.tengenmspacman.model.*;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_ActorSpeedRules;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GamePlay extends CommonGamePlay {

    public enum GamePlayOptions implements GameSession.GameSessionValueKey {
        BOOSTER_MODE, BOOSTER_ON, CAN_START_GAME, DIFFICULTY, MAP_CATEGORY, START_LEVEL_NUMBER, NUM_CONTINUES
    }

    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;

    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;

    public TengenMsPacMan_GamePlay() {}

    public boolean allOptionsHaveDefaultValue(GameSession session) {
        final BoosterMode boosterMode = session.value(GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
        final Difficulty difficulty = session.value(GamePlayOptions.DIFFICULTY, Difficulty.class);
        final MapCategory mapCategory = session.value(GamePlayOptions.MAP_CATEGORY, MapCategory.class);
        final int startLevel = session.value(GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
        final int numContinues = session.value(GamePlayOptions.NUM_CONTINUES, Integer.class);

        return boosterMode == TengenMsPacMan_GameModel.DEFAULT_PAC_BOOSTER
            && difficulty == TengenMsPacMan_GameModel.DEFAULT_DIFFICULTY
            && mapCategory == TengenMsPacMan_GameModel.DEFAULT_MAP_CATEGORY
            && startLevel == TengenMsPacMan_GameModel.DEFAULT_START_LEVEL
            && numContinues == TengenMsPacMan_GameModel.DEFAULT_NUM_CONTINUES;
    }

    public void activateBooster(GameContext game, Pac pac, boolean boosterOn) {
        requireNonNull(game);
        requireNonNull(pac);

        final GameSession session = game.session();

        session.setValue(GamePlayOptions.BOOSTER_ON, boosterOn);

        final SpriteAnimSystem animSystem = game.systems().spriteAnim();
        animSystem.select(pac, boosterOn ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : CommonSpriteAnimationID.PAC_MUNCHING);
    }

    public void setBoosterMode(GameSession session, BoosterMode boosterMode) {
        requireNonNull(session);
        session.setValue(GamePlayOptions.BOOSTER_MODE, boosterMode);
    }

    public BoosterMode boosterMode(GameSession session) {
        return session.value(GamePlayOptions.BOOSTER_MODE, BoosterMode.class);
    }

    public void setMapCategory(GameSession session, MapCategory mapCategory) {
        requireNonNull(session);
        requireNonNull(mapCategory);
        session.setValue(GamePlayOptions.MAP_CATEGORY, mapCategory);
    }

    public MapCategory mapCategory(GameSession session) {
        return session.value(GamePlayOptions.MAP_CATEGORY, MapCategory.class);
    }

    public void setDifficulty(GameContext game, Difficulty difficulty) {
        requireNonNull(game);
        requireNonNull(difficulty);

        final GameSession session = game.session();
        session.setValue(GamePlayOptions.DIFFICULTY, difficulty);

        //TODO this should also move into session!
        final var speedRules = (TengenMsPacMan_ActorSpeedRules) game.model().rules().actorSpeedRules();
        speedRules.setDifficulty(difficulty);
    }

    public Difficulty difficulty(GameSession session) {
        requireNonNull(session);
        return session.value(GamePlayOptions.DIFFICULTY, Difficulty.class);
    }

    public void setStartLevelNumber(GameSession session, int number) {
        requireNonNull(session);
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL || number > TengenMsPacMan_GameRules.LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }
        session.setValue(GamePlayOptions.START_LEVEL_NUMBER, number);
    }

    public int startLevelNumber(GameSession session) {
        requireNonNull(session);
        return session.value(GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
    }

    public void setNumContinues(GameSession session, int numContinues) {
        requireNonNull(session);
        session.setValue(GamePlayOptions.NUM_CONTINUES, numContinues);
    }

    public int numContinues(GameSession session) {
        requireNonNull(session);
        return session.value(GamePlayOptions.NUM_CONTINUES, Integer.class);
    }

    //TODO don't change values inside this method
    public boolean canContinueOnGameOver(GameSession session) {
        requireNonNull(session);
        final int startLevelNumber = session.value(GamePlayOptions.START_LEVEL_NUMBER, Integer.class);
        final int numContinues = session.value(GamePlayOptions.NUM_CONTINUES, Integer.class);
        if (startLevelNumber >= 10 && numContinues > 0) {
            session.setValue(GamePlayOptions.NUM_CONTINUES, numContinues - 1);
            return true;
        } else {
            session.setValue(GamePlayOptions.NUM_CONTINUES, 4);
            return false;
        }
    }

    public void setBoosterOn(GameSession session, boolean boosterOn) {
        requireNonNull(session);
        session.setValue(GamePlayOptions.BOOSTER_ON, boosterOn);
    }

    public boolean isBoosterOn(GameSession session) {
        requireNonNull(session);
        return session.value(GamePlayOptions.BOOSTER_ON, Boolean.class);
    }

    public boolean canStartNewGame(GameSession session) {
        return session.value(GamePlayOptions.CAN_START_GAME, Boolean.class);

    }
    public void setCanStartNewGame(GameSession session, boolean canStartNewGame) {
        requireNonNull(session);
        session.setValue(GamePlayOptions.CAN_START_GAME, canStartNewGame);
    }

    // Game start

    @Override
    public void onSessionStart(GameContext game) {
        super.onSessionStart(game);

        final GameSession session = game.session();

        setBoosterMode(session, TengenMsPacMan_GameModel.DEFAULT_PAC_BOOSTER);
        setDifficulty(game, TengenMsPacMan_GameModel.DEFAULT_DIFFICULTY);
        setMapCategory(session, TengenMsPacMan_GameModel.DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(session, TengenMsPacMan_GameModel.DEFAULT_START_LEVEL);
        setNumContinues(session, TengenMsPacMan_GameModel.DEFAULT_NUM_CONTINUES);

        setStartLevelNumber(session, 1);
        setBoosterOn(session, false);

        session.hud().hide();
    }

    // Level building and level start

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        final GameSession session = game.session();
        final WorldNavigationSystem navigator = game.systems().worldNavigator();
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) game.model();
        final WorldMap worldMap = model.worldMapManager().supplyWorldMap(levelNumber, mapCategory(session));
        final var huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", model.rules().numHuntingPhases());

        final GameLevel level = new GameLevel(levelNumber, worldMap, huntingTimer, 3);

        session.setLevel(level);

        final House house = HouseFactory.createArcadeHouse(TengenMsPacMan_GameModel.HOUSE_MIN_TILE);
        level.entities().add(house);

        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(navigator::requestTurnBack);
            }
        });

        int index = levelNumber <= 19 ? levelNumber - 1 : 18;
        float powerSeconds = TengenMsPacMan_GameRules.POWER_PELLET_TIMES[index] / 16.0f;
        level.setPacPowerSeconds(powerSeconds);
        level.setPacPowerFadingSeconds(0.5f * 3);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        level.setGameOverStateTicks(mapCategory(session) == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        setMsPacMan(game, level);
        createAndSetGhosts(level, house);

        //TODO not sure about this:
        level.setBonusSymbolCode(0, model.rules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, model.rules().selectBonusSymbolCode(level.number(), 1));

        level.entities().add(new MessageView());

        return level;
    }

    @Override
    public void showLevelMessage(GameContext game, GameLevel level, GameLevelMessageType type) {
        final GameSession session = game.session();
        final Vector2f messagePosition = messageCenterPosition(level);
        // For map categories "mini", "big" or "strange", the "game over" message is animated
        final GameLevelMessage message = type == GameLevelMessageType.GAME_OVER && mapCategory(session) != MapCategory.ARCADE
            ? new MovingGameLevelMessage(type, messagePosition, GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS)
            : new GameLevelMessage(type, messagePosition);
        level.setMessage(message);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems sys = game.systems();

        final GameLevel demoLevel = createLevel(game, 1);
        demoLevel.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        final var steering = new RuleGuidedPacSteering(
            sys.worldNavigator(),
            sys.pacWorldMovementPolicy()
        );
        pac.autoSteering().setSteering(steering);

        session.setLevel(demoLevel);
        session.setAttractMode(true);

        session.gateKeeper().setLevelNumber(1);
        ScoreSystem.setLevelNumber(session.score(), 1);

        return demoLevel;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    @Override
    public void startLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        level.recordStartTime(System.currentTimeMillis());
        prepareLevelForPlaying(game);

        // In Tengen, actors are shown immediately
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        if (boosterMode(session) == BoosterMode.BOOSTER_ALWAYS_ON) {
            activateBooster(game, level.entities().pac(), true);
        }
        showLevelMessage(game, level, GameLevelMessageType.READY);

        final LevelCounter levelCounter = session.levelCounter();
        LevelCounterSystem.update(levelCounter, level.number(), level.bonusSymbolCode(0));
        if (LevelCounterSystem.isFull(levelCounter)) {
            LevelCounterSystem.enable(levelCounter, false);
            Logger.info("Level counter is full and gets disabled!");
        }

        session.score().data().setEnabled(true);

        //TODO fixme
        //context.cheats().update(level);
    }

    // Playing level

    @Override
    public void activateNextBonus(GameContext game, GameLevel level) {
        final GameSystems sys = game.systems();
        final GameModel model = game.model();
        final GameEventManager eventManager = game.eventManager();
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().bonusState() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        if (terrain.horizontalPortals().isEmpty()) {
            Logger.error("Cannot activate next bonus: No portal exists in game level");
            return;
        }

        final House house = level.entities().theOne(House.class);
        final Vector2i houseEntry = WorldMap.computeTileAt(house.floorplan().entryPosition());
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
        final int value = model.rules().scoringRules().pointsForBonus(symbolCode);
        final float speed = model.rules().actorSpeedRules().bonusSpeed(game, level);
        final Bonus bonus = Bonus.createMovingBonus(symbolCode, value);
        sys.bonusMoveAndJump().setRoute(bonus, route, leftToRight);
        sys.bonusState().showEdibleAndStartWandering(bonus, speed);

        level.setBonus(bonus);
        eventManager.publishGameEvent(new BonusActivatedEvent(bonus));
    }

    // private

    private void setMsPacMan(GameContext game, GameLevel level) {
        final GameSession session = game.session();
        final GameSystems systems = game.systems();
        final var factory = TengenMsPacMan_ActorFactory.instance();
        final Pac msPacMan = factory.createMsPacMan();

        msPacMan.autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.worldNavigator(), systems.pacWorldMovementPolicy()
        ));
        activateBooster(game, msPacMan, boosterMode(session) == BoosterMode.BOOSTER_ALWAYS_ON);
        level.setPac(msPacMan);
    }

    private void createAndSetGhosts(GameLevel level, House house) {
        final var factory = TengenMsPacMan_ActorFactory.instance();

        final Ghost redGhost    = factory.createRedGhost();
        final Ghost pinkGhost   = factory.createPinkGhost();
        final Ghost cyanGhost   = factory.createCyanGhost();
        final Ghost orangeGhost = factory.createOrangeGhost();

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        redGhost.worldInfo()   .init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        pinkGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        cyanGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        orangeGhost.worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);

        level.setGhosts(redGhost, pinkGhost, cyanGhost, orangeGhost);
    }
}
