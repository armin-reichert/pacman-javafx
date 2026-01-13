/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2b;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.steering.Steering;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengenmspacman.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.inClosedRange;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomByte;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState.HUNTING;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends AbstractGameModel implements LevelCounter {

    static final short TICK_SHOW_READY = 10;
    static final short TICK_NEW_GAME_SHOW_GUYS = 70;
    static final short TICK_NEW_GAME_START_HUNTING = 250;
    static final short TICK_RESUME_HUNTING = 240;
    static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
    static final short TICK_EATING_GHOST_COMPLETE = 60;

    static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    static final short TICK_PACMAN_DYING_START_PAC_ANIMATION = 90;
    static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    static final short TICK_PACMAN_DYING_PAC_DEAD = 240;

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";
    public static final String READY_MESSAGE_TEXT = "READY!";
    public static final String LEVEL_TEST_MESSAGE_TEXT_PATTERN = "TEST    L%02d";

    public static final Vector2i HOUSE_MIN_TILE = Vector2i.of(10, 15);

    public static final byte FIRST_LEVEL_NUMBER = 1;
    public static final byte LAST_LEVEL_NUMBER = 32;

    public static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;
    public static final byte GAME_OVER_MESSAGE_DELAY_SEC = 2;

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;

    // Bonus symbols in Arcade, Mini and Big mazes
    public static final byte BONUS_CHERRY      = 0;
    public static final byte BONUS_STRAWBERRY  = 1;
    public static final byte BONUS_ORANGE      = 2;
    public static final byte BONUS_PRETZEL     = 3;
    public static final byte BONUS_APPLE       = 4;
    public static final byte BONUS_PEAR        = 5;
    public static final byte BONUS_BANANA      = 6;

    // Additional bonus symbols in Strange mazes
    public static final byte BONUS_MILK        = 7;
    public static final byte BONUS_ICE_CREAM   = 8;
    public static final byte BONUS_HIGH_HEELS  = 9;
    public static final byte BONUS_STAR        = 10;
    public static final byte BONUS_HAND        = 11;
    public static final byte BONUS_RING        = 12;
    public static final byte BONUS_FLOWER      = 13;

    // Bonus value = factor * 100
    private static final byte[] BONUS_VALUE_FACTORS = new byte[14];

    private static final int FIRST_BONUS_PELLETS_EATEN = 64;
    private static final int SECOND_BONUS_PELLETS_EATEN = 176;
    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    private static final PacBooster DEFAULT_PAC_BOOSTER = PacBooster.OFF;
    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    private static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;
    private static final int DEFAULT_START_LEVEL = 1;
    private static final int DEFAULT_NUM_CONTINUES = 4;

    static {
        BONUS_VALUE_FACTORS[BONUS_CHERRY]        = 1;
        BONUS_VALUE_FACTORS[BONUS_STRAWBERRY]    = 2;
        BONUS_VALUE_FACTORS[BONUS_ORANGE]        = 5;
        BONUS_VALUE_FACTORS[BONUS_PRETZEL]       = 7;
        BONUS_VALUE_FACTORS[BONUS_APPLE]         = 10;
        BONUS_VALUE_FACTORS[BONUS_PEAR]          = 20;
        BONUS_VALUE_FACTORS[BONUS_BANANA]        = 50; // !!
        BONUS_VALUE_FACTORS[BONUS_MILK]          = 30; // !!
        BONUS_VALUE_FACTORS[BONUS_ICE_CREAM]     = 40; // !!
        BONUS_VALUE_FACTORS[BONUS_HIGH_HEELS]    = 60;
        BONUS_VALUE_FACTORS[BONUS_STAR]          = 70;
        BONUS_VALUE_FACTORS[BONUS_HAND]          = 80;
        BONUS_VALUE_FACTORS[BONUS_RING]          = 90;
        BONUS_VALUE_FACTORS[BONUS_FLOWER]        = 100;
    }

    private static final float BONUS_EATEN_SECONDS = 2;

    private static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    private static final byte[] POWER_PELLET_TIMES = {
        0x60, 0x50, 0x40, 0x30, 0x20, 0x50, 0x20, 0x1C, // levels 1-8
        0x18, 0x40, 0x20, 0x1C, 0x18, 0x20, 0x1C, 0x18, // levels 9-16
        0x00, 0x18, 0x20                                // levels 17, 18, then 19+
    };

    private final TengenMsPacMan_HeadsUpDisplay hud;
    private final TengenMsPacMan_MapSelector mapSelector;
    private final GateKeeper gateKeeper;
    private final Steering automaticSteering;
    private final Steering demoLevelSteering;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBooster;
    private boolean boosterActive;
    private int startLevelNumber; // 1-7
    private boolean canStartNewGame;
    private int numContinues;

    public TengenMsPacMan_GameModel(File highScoreFile) {
        super(highScoreFile);

        this.mapSelector = new TengenMsPacMan_MapSelector();
        this.hud = new TengenMsPacMan_HeadsUpDisplay();
        this.gateKeeper = new GateKeeper(); //TODO implement original logic from Tengen game
        this.automaticSteering = new RuleBasedPacSteering();
        this.demoLevelSteering = new RuleBasedPacSteering();
        this.pelletPoints = 10;
        this.energizerPoints = 50;
        setCollisionStrategy(CollisionStrategy.CENTER_DISTANCE);
        setGameControl(new TengenMsPacMan_GameController());
    }

    public boolean allOptionsDefault() {
        return pacBooster == DEFAULT_PAC_BOOSTER
            && difficulty == DEFAULT_DIFFICULTY
            && mapCategory == DEFAULT_MAP_CATEGORY
            && startLevelNumber == DEFAULT_START_LEVEL
            && numContinues == DEFAULT_NUM_CONTINUES;
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay hud() {
        return hud;
    }

    @Override
    public void boot() {
        setInitialLifeCount(3);
        cheatUsedProperty().set(false);
        immuneProperty().set(false);
        usingAutopilotProperty().set(false);
        prepareNewGame();
        hud.all(false);
        setPacBooster(DEFAULT_PAC_BOOSTER);
        setDifficulty(DEFAULT_DIFFICULTY);
        setMapCategory(DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(DEFAULT_START_LEVEL);
        numContinues = DEFAULT_NUM_CONTINUES;
    }

    @Override
    public void prepareNewGame() {
        lifeCountProperty().set(initialLifeCount());
        clearLevelCounter();
        setPlaying(false);
        boosterActive = false;
        highScore().setEnabled(true);
        gateKeeper.reset();
        levelProperty().set(null);
        score().reset();
        try {
            loadHighScore();
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Error loading highscore file {}", highScoreFile.getAbsolutePath());
        }
    }

    @Override
    public void onGameOver() {
        setPlaying(false);
        showLevelMessage(GameLevelMessageType.GAME_OVER);
        try {
            updateHighScore();
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Error updating highscore file {}", highScoreFile.getAbsolutePath());
        }
    }

    @Override
    public WorldMapSelector mapSelector() { return mapSelector; }

    public void setPacBooster(PacBooster mode) {
        pacBooster = mode;
    }

    public PacBooster pacBooster() {
        return pacBooster;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = requireNonNull(mapCategory);
        if (mapCategory == MapCategory.ARCADE) {
            /* see https://tcrf.net/Ms._Pac-Man_(NES,_Tengen):
            Humorously, instead of adding a check to disable multiple extra lives,
            the "Arcade" maze set sets the remaining 3 extra life scores to over 970,000 points,
            a score normally unachievable without cheat codes, since all maze sets end after 32 stages.
            This was most likely done to simulate the Arcade game only giving one extra life per game.
            */
            setExtraLifeScores(10_000, 970_000, 980_000, 990_000);
        } else {
            setExtraLifeScores(10_000, 50_000, 100_000, 300_000);
        }
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setStartLevelNumber(int number) {
        if (number < FIRST_LEVEL_NUMBER || number > LAST_LEVEL_NUMBER) {
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

    public boolean isBoosterActive() {
        return boosterActive;
    }

    @Override
    public boolean canStartNewGame() {
        return canStartNewGame;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    @Override
    public void startNewGame(long tick) {
        if (tick == 1) {
            prepareNewGame();
            buildNormalLevel(startLevelNumber);
            publishGameEvent(GameEvent.Type.GAME_STARTED);
        }
        else if (tick == TICK_SHOW_READY) {
            startLevel();
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level().showPacAndGhosts();
        }
        else if (tick == TICK_NEW_GAME_START_HUNTING) {
            setPlaying(true);
            control().enterState(HUNTING);
        }
    }

    @Override
    public void continuePlaying(long tick) {
        if (tick == 1) {
            final GameLevel level = level();
            level.getReadyToPlay();
            level.showPacAndGhosts();
            publishGameEvent(GameEvent.Type.GAME_CONTINUED);
        } else if (tick == TICK_RESUME_HUNTING) {
            control().enterState(HUNTING);
        }
    }

    @Override
    public void startLevel() {
        final GameLevel level = level();
        level.recordStartTime(System.currentTimeMillis());
        level.getReadyToPlay();
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(level.pac(), true);
        }
        if (level.isDemoLevel()) {
            showLevelMessage(GameLevelMessageType.GAME_OVER);
            score().setEnabled(true);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showLevelMessage(GameLevelMessageType.READY);
            updateLevelCounter(level.number(), level.bonusSymbol(0));
            score().setEnabled(true);
            updateCheatingProperties(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        publishGameEvent(GameEvent.Type.LEVEL_STARTED);
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
            level().showPacAndGhosts();
        }
        else if (tick == TICK_DEMO_LEVEL_START_HUNTING) {
            control().enterState(GameState.HUNTING);
        }
    }

    @Override
    public void startNextLevel() {
        if (level().number() < LAST_LEVEL_NUMBER) {
            buildNormalLevel(level().number() + 1);
            startLevel();
            level().showPacAndGhosts();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", LAST_LEVEL_NUMBER);
        }
    }

    @Override
    public void showLevelMessage(GameLevelMessageType type) {
        optGameLevel().ifPresent(level -> {
            requireNonNull(type);
            final Vector2f center = level.worldMap().terrainLayer().messageCenterPosition();
            // Non-Arcade maps have a moving "Game Over" message
            final GameLevelMessage message = type == GameLevelMessageType.GAME_OVER && mapCategory != MapCategory.ARCADE
                    ? new MovingGameLevelMessage(type, center, GAME_OVER_MESSAGE_DELAY_SEC * NUM_TICKS_PER_SEC)
                    : new GameLevelMessage(type, center);
            level.setMessage(message);
        });
    }

    @Override
    public void clearLevelMessage() {
        optGameLevel().ifPresent(GameLevel::clearMessage);
    }

    public void activatePacBooster(Pac pac, boolean active) {
        boosterActive = active;
        pac.selectAnimation(boosterActive
            ? TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_BOOSTER : Pac.AnimationID.PAC_MUNCHING);
    }

    private int cutSceneNumberAfterLevel(int levelNumber) {
        if (levelNumber == LAST_LEVEL_NUMBER) {
            return 4;
        }
        return switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    @Override
    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            // Ghosts inside house sit at bottom of house
            ghost.setStartPosition(halfTileRightOf(tile).plus(0, HTS));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
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
        float powerSeconds = POWER_PELLET_TIMES[index] / 16.0f;
        level.setPacPowerSeconds(powerSeconds);
        level.setPacPowerFadingSeconds(0.5f * 3);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        level.setGameOverStateTicks(mapCategory == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        final MsPacMan msPacMan = new MsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        activatePacBooster(msPacMan, pacBooster == PacBooster.ALWAYS_ON);

        final Blinky blinky = new Blinky();
        blinky.setHome(house);
        final Vector2i blinkyStartTile = terrain.getTileProperty(POS_GHOST_1_RED);
        if (blinkyStartTile != null) {
            blinky.setStartPosition(halfTileRightOf(blinkyStartTile));
        } else {
            Logger.error("No start position in map specified for {}", blinky.name());
            blinky.setStartPosition(house.entryPosition());
        }

        final Pinky pinky = new Pinky();
        pinky.setHome(house);
        setGhostStartPosition(pinky, terrain.getTileProperty(POS_GHOST_2_PINK));

        final Inky inky = new Inky();
        inky.setHome(house);
        setGhostStartPosition(inky, terrain.getTileProperty(POS_GHOST_3_CYAN));

        final Sue sue = new Sue();
        sue.setHome(house);
        setGhostStartPosition(sue, terrain.getTileProperty(POS_GHOST_4_ORANGE));

        level.setPac(msPacMan);
        level.setGhosts(blinky, pinky, inky, sue);
        //TODO not sure about this:
        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        setLevelCounterEnabled(levelNumber < 8);

        return level;
    }

    private AbstractHuntingTimer createHuntingTimer() {
        final var huntingTimer = new TengenMsPacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((_, _, newPhaseIndex) -> {
            optGameLevel().ifPresent(level -> {
                if (newPhaseIndex.intValue() > 0) {
                    level.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                        .forEach(Ghost::requestTurnBack);
                }
            });
            huntingTimer.logPhase();
        });
        return huntingTimer;
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        level.setCutSceneNumber(cutSceneNumberAfterLevel(levelNumber));
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        final GameLevel level = createLevel(1, true);
        level.setCutSceneNumber(0);
        level.setGameOverStateTicks(120);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        score().setLevelNumber(1);
        gateKeeper.setLevelNumber(1);

        levelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }

    @Override
    public int lastLevelNumber() { return LAST_LEVEL_NUMBER; }

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
    public void updateHunting(GameLevel level) {
        doHuntingStep(level);
        gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
    }

    @Override
    public boolean isBonusReached(GameLevel level) {
        int eatenFoodCount = level.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == FIRST_BONUS_PELLETS_EATEN || eatenFoodCount == SECOND_BONUS_PELLETS_EATEN;
    }

    private byte computeBonusSymbol(int levelNumber) {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (levelNumber - 1 <= maxBonus) {
            return (byte) (levelNumber - 1);
        }
        return randomByte(0, maxBonus + 1);
    }

    @Override
    public void activateNextBonus(GameLevel level) {
        //TODO Find out how Tengen really implemented this
        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        // compute possible bonus route
        if (level.worldMap().terrainLayer().horizontalPortals().isEmpty()) {
            Logger.error("No portal found in current maze");
            return; // TODO: can this happen?
        }
        House house = level.worldMap().terrainLayer().optHouse().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        List<HPortal> portals = level.worldMap().terrainLayer().horizontalPortals();
        boolean leftToRight = new Random().nextBoolean();
        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        HPortal entryPortal = portals.get(new Random().nextInt(portals.size()));
        HPortal exitPortal  = portals.get(new Random().nextInt(portals.size()));
        List<Vector2b> route = Stream.of(
            leftToRight ? entryPortal.leftBorderEntryTile() : entryPortal.rightBorderEntryTile(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightBorderEntryTile().plus(1, 0) : exitPortal.leftBorderEntryTile().minus(1, 0)
        ).map(Vector2b::new).toList();

        level.selectNextBonus();
        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.initRoute(route, leftToRight);
        bonus.setEdibleAndStartJumpingAtSpeed(level.game().bonusSpeed(level));
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        publishGameEvent(GameEvent.Type.BONUS_ACTIVATED, bonus.tile());
    }

    @Override
    protected void eatPellet(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(level, pelletPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
    }

    @Override
    public void eatEnergizer(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(level, energizerPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());

        if (!isLevelCompleted()) {
            level.ghosts(GhostState.FRIGHTENED, GhostState.HUNTING_PAC).forEach(MovingActor::requestTurnBack);
            level.energizerVictims().clear();
            final float powerSeconds = level.pacPowerSeconds();
            if (powerSeconds > 0) {
                level.huntingTimer().stop();
                Logger.debug("Hunting stopped (Pac-Man got power)");
                long ticks = TickTimer.secToTicks(powerSeconds);
                level.pac().powerTimer().restartTicks(ticks);
                Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
                level.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
                simStep.pacGotPower = true;
                publishGameEvent(GameEvent.Type.PAC_GETS_POWER);
            }
        }
    }

    @Override
    protected void eatBonus(GameLevel level, Bonus bonus) {
        scorePoints(level, bonus.points());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.setEatenSeconds(BONUS_EATEN_SECONDS);
        publishGameEvent(GameEvent.Type.BONUS_EATEN);
    }

    @Override
    public void updatePacManDying(long tick) {
        final GameLevel level = level();
        final Pac pac = level.pac();
        if (tick == 1) {
            level.huntingTimer().stop();
            gateKeeper.resetCounterAndSetEnabled(true);

            pac.powerTimer().stop();
            pac.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero.");

            pac.setSpeed(0);
            pac.setDead(true);
            pac.stopAnimation();

            level.ghosts().forEach(ghost -> ghost.onPacKilled(level));
            publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.ghosts().forEach(Ghost::hide);
            pac.optAnimationManager().ifPresent(animations -> {
                animations.select(Pac.AnimationID.PAC_DYING);
                animations.reset();
            });
        }
        else if (tick == TICK_PACMAN_DYING_START_PAC_ANIMATION) {
            pac.optAnimationManager().ifPresent(AnimationManager::play);
            publishGameEvent(GameEvent.Type.PAC_DYING, pac.tile());
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            //TODO clarify in MAME
            level.optBonus().ifPresent(Bonus::setInactive);
        }
        else if (tick == TICK_PACMAN_DYING_PAC_DEAD) {
            publishGameEvent(GameEvent.Type.PAC_DEAD);
        }
        else {
            level.blinking().tick();
            pac.tick(this);
        }
    }

    @Override
    public void onEatGhost(GameLevel level, Ghost ghost) {
        final List<Ghost> victims = level.energizerVictims();
        final int killedSoFar = victims.size();
        final int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        victims.add(ghost);
        ghost.setState(GhostState.EATEN);
        ghost.selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, killedSoFar);
        scorePoints(level, points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    @Override
    public void updateEatingGhost(long tick) {
        final GameLevel level = level();
        if (tick == 1) {
            level.pac().hide();
            level.ghosts().forEach(Ghost::stopAnimation);
            publishGameEvent(GameEvent.Type.GHOST_EATEN);
        }
        else if (tick < TICK_EATING_GHOST_COMPLETE) {
            level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.tick(this));
            level.blinking().tick();
        }
        else if (tick == TICK_EATING_GHOST_COMPLETE) {
            level.pac().show();
            level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        }
    }

    // ActorSpeedControl interface

    public float speedUnitsToPixels(float units) {
        return units / 32f;
    }

    public float pacBoosterSpeedDelta() {
        return 0.5f;
    }

    public float pacDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -4;
            case NORMAL -> 0;
            case HARD -> 12;
            case CRAZY -> 24;
        });
    }

    public float ghostDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -8;
            case NORMAL -> 0;
            case HARD -> 16;
            case CRAZY -> 32;
        });
    }

    public float ghostSpeedDelta(byte personality) {
        return speedUnitsToPixels(switch (personality) {
            case RED_GHOST_SHADOW -> 3;
            case ORANGE_GHOST_POKEY -> 2;
            case CYAN_GHOST_BASHFUL -> 1;
            case PINK_GHOST_SPEEDY -> 0;
            default -> throw GameException.invalidGhostPersonality(personality);
        });
    }

    /**
     * Fellow friend @RussianManSMWC told me on Discord:
     * <p>
     * By the way, there's an additional quirk regarding ghosts' speed.
     * On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
     * if there are 31 or fewer dots, the speed is increased. the base increase value is 2, which is further increased
     * by 1 for every 8 dots eaten. (I should note it is in subunits. If it was times 2, that would've been crazy).
     * </p>
     */
    public float ghostSpeedIncreaseByFoodRemaining(GameLevel level) {
        byte units = 0;
        TengenMsPacMan_GameModel game = (TengenMsPacMan_GameModel) level.game();
        if (game.difficulty() == Difficulty.NORMAL && level.number() >= 5) {
            int dotsLeft = level.worldMap().foodLayer().uneatenFoodCount();
            if (dotsLeft <= 7) {
                units = 5;
            } else if (dotsLeft <= 15) {
                units = 4;
            } else if (dotsLeft <= 23) {
                units = 3;
            } else if (dotsLeft <= 31) {
                units = 2;
            }
        }
        return speedUnitsToPixels(units);
    }

    public float pacBaseSpeedInLevel(int levelNumber) {
        int units = 0;
        if (inClosedRange(levelNumber, 1, 4)) {
            units = 0x20;
        } else if (inClosedRange(levelNumber, 5, 12)) {
            units = 0x24;
        } else if (inClosedRange(levelNumber, 13, 16)) {
            units = 0x28;
        } else if (inClosedRange(levelNumber, 17, 20)) {
            units = 0x27;
        } else if (inClosedRange(levelNumber, 21, 24)) {
            units = 0x26;
        } else if (inClosedRange(levelNumber, 25, 28)) {
            units = 0x25;
        } else if (levelNumber >= 29) {
            units = 0x24;
        }

        return speedUnitsToPixels(units);
    }

    // TODO: do they all have the same base speed? Unclear from disassembly data.
    public float ghostBaseSpeedInLevel(int levelNumber) {
        int units = 0x20; // default: 32
        if (inClosedRange(levelNumber, 1, 4)) {
            units = 0x18;
        } else if (inClosedRange(levelNumber, 5, 12)) {
            units = 0x20 + (levelNumber - 5);
        } // 0x20-0x27
        else if (levelNumber >= 13) {
            units = 0x28;
        }
        return speedUnitsToPixels(units);
    }

    @Override
    public float bonusSpeed(GameLevel level) {
        //TODO clarify exact speed
        return 0.5f * pacSpeed(level);
    }

    @Override
    public float pacSpeed(GameLevel level) {
        if (level == null) {
            return 0;
        }
        TengenMsPacMan_GameModel game = (TengenMsPacMan_GameModel) level.game();
        float speed = pacBaseSpeedInLevel(level.number());
        speed += pacDifficultySpeedDelta(game.difficulty());
        if (game.pacBooster() == PacBooster.ALWAYS_ON
            || game.pacBooster() == PacBooster.USE_A_OR_B && game.isBoosterActive()) {
            speed += pacBoosterSpeedDelta();
        }
        return speed;
    }

    @Override
    public float pacSpeedWhenHasPower(GameLevel level) {
        //TODO correct?
        return level.pac() != null ? 1.1f * pacSpeed(level) : 0;
    }

    @Override
    public float ghostSpeed(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i tile = ghost.tile();
        final GhostState state = ghost.state();
        final boolean insideHouse = terrain.house().isVisitedBy(ghost);
        final boolean tunnelSlowdown = terrain.isTunnel(tile);
        return switch (state) {
            case LOCKED -> insideHouse ? 0.5f : 0;
            case LEAVING_HOUSE -> 0.5f;
            case HUNTING_PAC -> tunnelSlowdown ? ghostSpeedTunnel(levelNumber) : ghostSpeedAttacking(level, ghost);
            case FRIGHTENED -> tunnelSlowdown ? ghostSpeedTunnel(levelNumber) : ghostSpeedWhenFrightened(level);
            case EATEN -> 0;
            case RETURNING_HOME, ENTERING_HOUSE -> 2;
        };
    }

    @Override
    public float ghostSpeedAttacking(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty());
        speed += ghostSpeedDelta(ghost.personality());
        float foodDelta = ghostSpeedIncreaseByFoodRemaining(level);
        if (foodDelta > 0) {
            speed += foodDelta;
            Logger.debug("Ghost speed increased by {} units to {0.00} px/tick for {}", foodDelta, speed, ghost.name());
        }
        return speed;
    }

    @Override
    public float ghostSpeedWhenFrightened(GameLevel level) {
        final int levelNumber = level.number();
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty());
        return 0.5f * speed; //TODO check with @RussianMan or disassembly
    }

    @Override
    public float ghostSpeedTunnel(int levelNumber) {
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty());
        return 0.4f * speed; //TODO check with @RussianMan or disassembly
    }

    // LevelCounter

    private final BooleanProperty levelCounterEnabled = new SimpleBooleanProperty(true);
    private final List<Byte> levelCounterSymbols = new ArrayList<>();

    public BooleanProperty levelCounterEnabledProperty() {
        return levelCounterEnabled;
    }

    @Override
    public List<Byte> levelCounterSymbols() {
        return Collections.unmodifiableList(levelCounterSymbols);
    }

    @Override
    public void clearLevelCounter() {
        levelCounterSymbols.clear();
    }

    @Override
    public void updateLevelCounter(int levelNumber, byte symbol) {
        if (levelNumber == 1) {
            clearLevelCounter();
        }
        if (isLevelCounterEnabled()) {
            levelCounterSymbols.add(symbol);
            if (levelCounterSymbols.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounterSymbols.removeFirst();
            }
        }
    }

    @Override
    public void setLevelCounterEnabled(boolean enabled) {
        levelCounterEnabledProperty().set(enabled);
    }

    @Override
    public boolean isLevelCounterEnabled() {
        return levelCounterEnabledProperty().get();
    }

}