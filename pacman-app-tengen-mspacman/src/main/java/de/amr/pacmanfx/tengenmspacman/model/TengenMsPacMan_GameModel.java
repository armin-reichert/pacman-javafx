/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.flow.GameControlFlow;
import de.amr.pacmanfx.flow.StateMachineGameControlFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.steering.Steering;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorSpeedControl;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomBoolean;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.basics.math.Vector2i.vec2_int;
import static de.amr.pacmanfx.core.Globals.*;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState.Timing;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends AbstractGameModel {


    public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    public static final short TICK_PACMAN_DYING_START_PAC_ANIMATION = 90;
    public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    public static final short TICK_PACMAN_DYING_PAC_DEAD = 240;

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";
    public static final String READY_MESSAGE_TEXT = "READY!";
    public static final String LEVEL_TEST_MESSAGE_TEXT_PATTERN = "TEST    L%02d";

    public static final Vector2i HOUSE_MIN_TILE = vec2_int(10, 15);

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;
    public static final byte GAME_OVER_MESSAGE_DELAY_SEC = 2;

    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    private final GameControlFlow gameFlow;
    private final TengenMsPacMan_ActorSpeedControl actorSpeedControl;
    private final TengenMsPacMan_HeadsUpDisplay hud;
    private final TengenMsPacMan_MapSelector mapSelector;
    private final TengenMsPacMan_LevelCounter levelCounter;
    private final GateKeeper gateKeeper;
    private final Steering automaticSteering;
    private final Steering demoLevelSteering;
    private final TengenMsPacMan_GameRules rules;

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
        hud = new TengenMsPacMan_HeadsUpDisplay();

        gameFlow = new StateMachineGameControlFlow("Tengen Ms. Pac-Man Game Flow", this);
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            gameFlow.addState(gameState.state());
        }

        actorSpeedControl = new TengenMsPacMan_ActorSpeedControl();
        gateKeeper = new GateKeeper(); //TODO implement original logic from Tengen game
        automaticSteering = new RuleBasedPacSteering();
        demoLevelSteering = new RuleBasedPacSteering();
        rules = new TengenMsPacMan_GameRules(this);

        setCollisionStrategy(CollisionStrategy.CENTER_DISTANCE);
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
        actorSpeedControl.setDifficulty(difficulty);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setStartLevelNumber(int number) {
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL || number > TengenMsPacMan_GameRules.LAST_LEVEL) {
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
            ? new MovingGameLevelMessage(type, center, GAME_OVER_MESSAGE_DELAY_SEC * NUM_TICKS_PER_SEC)
            : new GameLevelMessage(type, center);
        level.setMessage(message);
    }

    @Override
    public TengenMsPacMan_GameRules rules() {
        return rules;
    }

    @Override
    public ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public GameControlFlow flow() {
        return gameFlow;
    }

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay hud() {
        return hud;
    }

    @Override
    public void init() {
        mapSelector.loadMapPrototypes();
        lives().setInitialCount(3);
        hud.all(false);

        setPacBoosterMode(TengenMsPacMan_GameRules.DEFAULT_PAC_BOOSTER);
        setDifficulty(TengenMsPacMan_GameRules.DEFAULT_DIFFICULTY);
        setMapCategory(TengenMsPacMan_GameRules.DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(TengenMsPacMan_GameRules.DEFAULT_START_LEVEL);
        numContinues = TengenMsPacMan_GameRules.DEFAULT_NUM_CONTINUES;

        prepareNewGame();
    }

    @Override
    public void prepareNewGame() {
        lives().setCount(lives().initialCount());
        levelProperty().set(null);
        levelCounter.clear();
        setPlayingLevel(false);
        boosterActive = false;
        gateKeeper.reset();
        score.reset();
        try {
            highScore.load();
            highScore.setEnabled(true);
        } catch (IOException x) {
            Logger.error(x, "Error loading high-score file {}", highScore.file().getAbsolutePath());
        }
    }

    @Override
    public void onGameOver(GameLevel level) {
        setPlayingLevel(false);
        showMessage(level, GameLevelMessageType.GAME_OVER);
        try {
            updateHighScore();
        } catch (IOException x) {
            Logger.error(x, "Error updating high-score file {}", highScore.file().getAbsolutePath());
        }
    }

    @Override
    public WorldMapSelector mapSelector() { return mapSelector; }

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public boolean canContinueOnGameOver() {
        if (startLevelNumber >= 10 && numContinues > 0) {
            numContinues -= 1;
            return true;
        } else {
            numContinues = 4;
            return false;
        }
    }

    @Override
    public boolean canStartNewGame() {
        return canStartNewGame;
    }

    @Override
    public void startLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        level.recordStartTime(System.currentTimeMillis());
        makeReadyForPlaying(level);
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
            updateCheats(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        flow().publishGameEvent(new LevelStartedEvent(this, level));
    }

    @Override
    public void startDemoLevel(long tick) {
        if (tick == 1) {
            buildDemoLevel();
        }
        else if (tick == 2) {
            startLevel();
        }
        else if (tick == 3) {
            // Now, actor animations are available
            final GameLevel level = optGameLevel().orElseThrow();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == Timing.TICK_DEMO_LEVEL_START_HUNTING) {
            flow().enterState(TengenMsPacMan_GameState.GAME_LEVEL_PLAYING.state());
        }
    }

    @Override
    public void startNextLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        if (level.number() < TengenMsPacMan_GameRules.LAST_LEVEL) {
            buildNormalLevel(level.number() + 1);
            startLevel();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", TengenMsPacMan_GameRules.LAST_LEVEL);
        }
    }

    @Override
    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (ghost.personality() == RED_GHOST_SHADOW) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            // The ghosts starting inside the house sit at the *bottom*!
            ghost.setStartPosition(halfTileRightOf(tile).plus(0, HTS));
        }
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber, mapCategory);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final ArcadeHouse house = new ArcadeHouse(HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, createHuntingTimer(), 3);
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
        level.setBonusSymbolCode(0, rules.selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, rules.selectBonusSymbolCode(level.number(), 1));

        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel newLevel = createLevel(levelNumber, false);
        newLevel.setCutSceneNumber(rules.cutSceneNumberAfterLevel(levelNumber).orElse(0));
        score.setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(newLevel);
        flow().publishGameEvent(new LevelCreatedEvent(this, newLevel));
    }

    @Override
    public void buildDemoLevel() {
        final GameLevel newLevel = createLevel(1, true);
        newLevel.setCutSceneNumber(0);
        newLevel.setGameOverStateTicks(120);

        final Pac pac = newLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(1);
        demoLevelSteering.init();
        score().setLevelNumber(1);

        levelProperty().set(newLevel);
        flow().publishGameEvent(new LevelCreatedEvent(this, newLevel));
    }

    @Override
    public int lastLevelNumber() { return TengenMsPacMan_GameRules.LAST_LEVEL; }

    @Override
    protected boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        if (runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} milliseconds", runningMillis);
            return true;
        }
        return false;
    }

    @Override
    public void activateNextBonus(GameLevel level) {
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

        final Vector2i houseEntry = computeTileAt(house.entryPosition());
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
        final Bonus bonus = new Bonus(symbolCode, rules.pointsForBonus(symbolCode));
        bonus.setMazeRoute(route, leftToRight);
        bonus.showEdibleAndStartWandering(actorSpeedControl.bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        flow().publishGameEvent(new BonusActivatedEvent(this, bonus));
    }

    @Override
    public void eatEnergizer(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(rules.pointsForEnergizer(), level.number());
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());

        level.killedGhostsForCurrentEnergizer().clear();

        final Pac pac = level.entities().pac();
        if (!isLevelCompleted()) {
            empowerPac(pac, level);
        }
    }

    @Override
    public void doPacManDying(GameLevel level, Pac pac, long tick) {
        if (tick == 1) {
            level.huntingTimer().stop();
            gateKeeper.resetCounterAndSetEnabled(true);

            pac.powerTimer().stop();
            pac.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero.");

            pac.setSpeed(0);
            pac.setDead(true);
            pac.animations().stopSelected();

            level.entities().ghosts().forEach(ghost -> ghost.onPacKilled(level));
            flow().publishGameEvent(new StopAllSoundsEvent(this));
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.entities().ghosts().forEach(Ghost::hide);
            pac.animations().select(ArcadePacMan_AnimationID.PAC_DYING);
            pac.animations().resetSelected();
        }
        else if (tick == TICK_PACMAN_DYING_START_PAC_ANIMATION) {
            pac.animations().playSelected();
            flow().publishGameEvent(new PacDyingEvent(this, pac));
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            //TODO clarify in MAME
            level.optBonus().ifPresent(Bonus::setInactive);
        }
        else if (tick == TICK_PACMAN_DYING_PAC_DEAD) {
            flow().publishGameEvent(new PacDeadEvent(this, pac));
        }
        else {
            level.blinking().doTick();
            pac.update(level);
        }
    }

    @Override
    public void onEatGhost(GameLevel level, Ghost eatenGhost) {
        final int killedBefore = level.killedGhostsForCurrentEnergizer().size();
        final int points = rules.pointsForGhost(killedBefore);

        eatenGhost.setState(GhostState.EATEN);
        eatenGhost.animations().selectAtFrame(ArcadePacMan_AnimationID.GHOST_POINTS, killedBefore);

        scorePoints(points, level.number());

        Logger.info("Scored {} points for killing {} at tile {}", points, eatenGhost.name(), eatenGhost.computeTile());

        level.entities().pac().hide();
        level.entities().ghosts().forEach(g -> g.animations().stopSelected());

        level.killedGhostsForCurrentEnergizer().add(eatenGhost);

        flow().publishGameEvent(new GhostEatenEvent(this, eatenGhost));
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

    private HuntingTimer createHuntingTimer() {
        final var huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", rules().numHuntingPhases());
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