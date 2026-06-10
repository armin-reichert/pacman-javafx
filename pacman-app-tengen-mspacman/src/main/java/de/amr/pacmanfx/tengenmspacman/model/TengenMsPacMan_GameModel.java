/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorSpeedControl;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends AbstractGameModel {

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";
    public static final String READY_MESSAGE_TEXT = "READY!";
    public static final String LEVEL_TEST_MESSAGE_TEXT_PATTERN = "TEST    L%02d";

    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;
    public static final byte GAME_OVER_MESSAGE_DELAY_SEC = 2;

    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBoosterMode;
    private boolean boosterActive;
    private int startLevelNumber; // 1-7
    private boolean canStartNewGame;
    private int numContinues;

    public TengenMsPacMan_GameModel() {
        mapSelector = new TengenMsPacMan_MapSelector();
        levelCounter = new TengenMsPacMan_LevelCounter();
        hud = new TengenMsPacMan_HUDState();
        actorSpeedControl = new TengenMsPacMan_ActorSpeedControl();
        gateKeeper = new GateKeeper(); //TODO implement original logic from Tengen game
        automaticSteering = new RuleBasedPacSteering();
        demoLevelSteering = new RuleBasedPacSteering();

        setDifficulty(Difficulty.NORMAL);
    }

    public boolean allOptionsDefault() {
        return pacBoosterMode == TengenMsPacMan_GameRules.DEFAULT_PAC_BOOSTER
            && difficulty == TengenMsPacMan_GameRules.DEFAULT_DIFFICULTY
            && mapCategory == TengenMsPacMan_GameRules.DEFAULT_MAP_CATEGORY
            && startLevelNumber == TengenMsPacMan_GameRules.DEFAULT_START_LEVEL
            && numContinues == TengenMsPacMan_GameRules.DEFAULT_NUM_CONTINUES;
    }

    public void setPacBoosterMode(PacBooster mode) {
        pacBoosterMode = requireNonNull(mode);
    }

    public PacBooster pacBoosterMode() {
        return pacBoosterMode;
    }

    public void activatePacBooster(Pac pac, boolean active) {
        requireNonNull(pac);
        pac.animations().select(active ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : ArcadePacMan_AnimationID.PAC_MUNCHING);
        boosterActive = active;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = requireNonNull(mapCategory);
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = requireNonNull(difficulty);
        actorSpeedControl().setDifficulty(difficulty);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setStartLevelNumber(int number) {
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL || number > TengenMsPacMan_GameRules.LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }
        startLevelNumber = number;
    }

    public int startLevelNumber() {
        return startLevelNumber;
    }

    public int numContinues() {
        return numContinues;
    }

    public boolean isBoosterActive() {
        return boosterActive;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    public void showMessage(GameLevel level, GameLevelMessageType type) {
        final Vector2f center = level.worldMap().terrainLayer().messageCenterPosition();
        // Non-Arcade maps show a moving "Game Over" message
        final GameLevelMessage message = type == GameLevelMessageType.GAME_OVER && mapCategory != MapCategory.ARCADE
            ? new MovingGameLevelMessage(type, center, GAME_OVER_MESSAGE_DELAY_SEC * GameRules.NUM_TICKS_PER_SEC)
            : new GameLevelMessage(type, center);
        level.setMessage(message);
    }

    @Override
    public TengenMsPacMan_ActorSpeedControl actorSpeedControl() {
        return (TengenMsPacMan_ActorSpeedControl) actorSpeedControl;
    }

    @Override
    public TengenMsPacMan_HUDState hud() {
        return (TengenMsPacMan_HUDState) hud;
    }

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return (TengenMsPacMan_LevelCounter) levelCounter;
    }

    @Override
    public void init() {
        super.init();

        setPacBoosterMode(TengenMsPacMan_GameRules.DEFAULT_PAC_BOOSTER);
        setDifficulty(TengenMsPacMan_GameRules.DEFAULT_DIFFICULTY);
        setMapCategory(TengenMsPacMan_GameRules.DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(TengenMsPacMan_GameRules.DEFAULT_START_LEVEL);
        numContinues = TengenMsPacMan_GameRules.DEFAULT_NUM_CONTINUES;
    }

    @Override
    public void prepareNewGame() {
        super.prepareNewGame();
        boosterActive = false;
    }

    @Override
    public boolean canContinueOnGameOver() {
        //TODO don't change values inside this method
        if (startLevelNumber >= 10 && numContinues > 0) {
            numContinues -= 1;
            return true;
        } else {
            numContinues = 4;
            return false;
        }
    }

    @Override
    public boolean canStartNewGame(GameContext gameContext) {
        return canStartNewGame;
    }

    @Override
    public void startLevel(GameContext gameContext) {
        final GameLevel level = optGameLevel().orElseThrow();
        level.recordStartTime(System.currentTimeMillis());

        prepareLevelForPlaying(level);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        if (pacBoosterMode == PacBooster.ALWAYS_ON) {
            activatePacBooster(level.entities().pac(), true);
        }
        if (level.isDemoLevel()) {
            showMessage(level, GameLevelMessageType.GAME_OVER);
            score().setEnabled(true);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showMessage(level, GameLevelMessageType.READY);
            levelCounter.update(level.number(), level.bonusSymbolCode(0));
            score().setEnabled(true);
            cheats.update(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        gameContext.flow().publishGameEvent(new LevelStartedEvent(gameContext, level));
    }

    //TODO Remove tick parameter, introduce game state
    @Override
    public void startDemoLevel(GameContext gameContext, long tick) {
        if (tick == 1) {
            buildDemoLevel(gameContext);
        }
        else if (tick == 2) {
            startLevel(gameContext);
        }
        else if (tick == 3) {
            // Now, actor animations are available
            final GameLevel level = optGameLevel().orElseThrow();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == TengenMsPacMan_GameState.Timing.TICK_DEMO_LEVEL_START_HUNTING) {
            gameContext.flow().enterState(TengenMsPacMan_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (ghost.personality() == RED_GHOST_SHADOW) {
            ghost.setStartPosition(WorldMap.halfTileRightOf(tile));
        } else {
            // The ghosts starting inside the house sit at the *bottom*!
            ghost.setStartPosition(WorldMap.halfTileRightOf(tile).plus(0, WorldMap.HTS));
        }
    }

    @Override
    public GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber, mapCategory);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final ArcadeHouse house = new ArcadeHouse(HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, createHuntingTimer(gameContext.rules()), 3);
        level.setDemoLevel(demoLevel);

        int index = levelNumber <= 19 ? levelNumber - 1 : 18;
        float powerSeconds = TengenMsPacMan_GameRules.POWER_PELLET_TIMES[index] / 16.0f;
        level.setPacPowerSeconds(powerSeconds);
        level.setPacPowerFadingSeconds(0.5f * 3);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        level.setGameOverStateTicks(mapCategory == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        setMsPacMan(level);
        setGhosts(level, house);

        //TODO not sure about this:
        level.setBonusSymbolCode(0, gameContext.rules().selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, gameContext.rules().selectBonusSymbolCode(level.number(), 1));

        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber) {
        final GameLevel newLevel = createLevel(gameContext, levelNumber, false);
        newLevel.setCutSceneNumber(gameContext.rules().cutSceneNumberAfterLevel(levelNumber).orElse(0));
        score.setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        setLevel(newLevel);
        gameContext.flow().publishGameEvent(new LevelCreatedEvent(gameContext, newLevel));
    }

    @Override
    public void buildDemoLevel(GameContext gameContext) {
        final GameLevel newLevel = createLevel(gameContext, 1, true);
        newLevel.setCutSceneNumber(0);
        newLevel.setGameOverStateTicks(120);

        final Pac pac = newLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(1);
        demoLevelSteering.init();
        score.setLevelNumber(1);

        setLevel(newLevel);
        gameContext.flow().publishGameEvent(new LevelCreatedEvent(gameContext, newLevel));
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        if (runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} milliseconds", runningMillis);
            return true;
        }
        return false;
    }

    @Override
    public void activateNextBonus(GameContext gameContext, GameLevel level) {
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
        final Bonus bonus = new Bonus(symbolCode, gameContext.rules().pointsForBonus(symbolCode));
        bonus.setMazeRoute(route, leftToRight);
        bonus.showEdibleAndStartWandering(actorSpeedControl.bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        gameContext.flow().publishGameEvent(new BonusActivatedEvent(gameContext, bonus));
    }

    @Override
    public void eatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(gameContext, gameContext.rules().pointsForEnergizer(), level.number());
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());

        level.clearGhostKillChain();

        startPacPowerMode(gameContext, level, level.entities().pac());
    }

    // Helpers

    private void setMsPacMan(GameLevel level) {
        final Pac msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        activatePacBooster(msPacMan, pacBoosterMode == PacBooster.ALWAYS_ON);
        level.setPac(msPacMan);
    }

    private void setGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        level.setGhosts(
            createGhost(RED_GHOST_SHADOW,   house, terrain, POS_GHOST_1_RED),
            createGhost(PINK_GHOST_SPEEDY,  house, terrain, POS_GHOST_2_PINK),
            createGhost(CYAN_GHOST_BASHFUL, house, terrain, POS_GHOST_3_CYAN),
            createGhost(ORANGE_GHOST_POKEY, house, terrain, POS_GHOST_4_ORANGE)
        );
    }

    private Ghost createGhost(byte personality, House house, TerrainLayer terrain, String startTileProperty) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(personality);
        ghost.setHome(house);
        setGhostStartPosition(ghost, terrain.getTileProperty(startTileProperty));
        return ghost;
    }

    private HuntingTimer createHuntingTimer(GameRules gameRules) {
        final var huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", gameRules.numHuntingPhases());
        huntingTimer.phaseIndexProperty().addListener((_, _, newPhaseIndex) -> {
            optGameLevel().ifPresent(level -> {
                if (newPhaseIndex.intValue() > 0) {
                    level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                        .forEach(Ghost::requestTurnBack);
                }
            });
            huntingTimer.logPhase();
        });
        return huntingTimer;
    }
}