/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomByte;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationManager.ANIM_MS_PAC_MAN_BOOSTER;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends AbstractGameModel {

    public static final byte FIRST_LEVEL_NUMBER = 1;
    public static final byte LAST_LEVEL_NUMBER = 32;
    public static final byte DEMO_LEVEL_MIN_DURATION_SEC = 20;

    public static final int GAME_OVER_MESSAGE_DELAY = 120;

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    private static final byte[] POWER_PELLET_TIMES = {
        0x60, 0x50, 0x40, 0x30, 0x20, 0x50, 0x20, 0x1C, // levels 1-8
        0x18, 0x40, 0x20, 0x1C, 0x18, 0x20, 0x1C, 0x18, // levels 9-16
        0x00, 0x18, 0x20                                // levels 17, 18, then 19+
    };

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

    private static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    public static Pac createMsPacMan() {
        var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public static Pac createPacMan() {
        var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public static class Blinky extends Ghost {

        @Override
        public String name() {
            return "Blinky";
        }

        @Override
        public byte personality() {
            return RED_GHOST_SHADOW;
        }

        @Override
        public void hunt(GameLevel gameLevel) {
            float speed = gameLevel.game().actorSpeedControl().ghostAttackSpeed(gameLevel, this);
            setSpeed(speed);
            if (gameLevel.game().huntingTimer().phaseIndex() == 0) {
                roam(gameLevel);
            } else {
                boolean chase = gameLevel.game().huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile(gameLevel) : gameLevel.ghostScatterTile(personality());
                tryMovingTowardsTargetTile(gameLevel, targetTile);
            }
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            return gameLevel.pac().tile();
        }
    }

    public static class Pinky extends Ghost {

        @Override
        public String name() {
            return "Pinky";
        }

        @Override
        public byte personality() {
            return PINK_GHOST_SPEEDY;
        }

        @Override
        public void hunt(GameLevel gameLevel) {
            float speed = gameLevel.game().actorSpeedControl().ghostAttackSpeed(gameLevel, this);
            setSpeed(speed);
            if (gameLevel.game().huntingTimer().phaseIndex() == 0) {
                roam(gameLevel);
            } else {
                boolean chase = gameLevel.game().huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile(gameLevel) : gameLevel.ghostScatterTile(personality());
                tryMovingTowardsTargetTile(gameLevel, targetTile);
            }
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            return gameLevel.pac().tilesAhead(4);
        }
    }

    public static class Inky extends Ghost {

        @Override
        public String name() {
            return "Inky";
        }

        @Override
        public byte personality() {
            return CYAN_GHOST_BASHFUL;
        }

        @Override
        public void hunt(GameLevel gameLevel) {
            float speed = gameLevel.game().actorSpeedControl().ghostAttackSpeed(gameLevel, this);
            boolean chase = gameLevel.game().huntingTimer().phase() == HuntingPhase.CHASING;
            Vector2i targetTile = chase ? chasingTargetTile(gameLevel) : gameLevel.ghostScatterTile(personality());
            setSpeed(speed);
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            Ghost blinky = gameLevel.ghost(RED_GHOST_SHADOW);
            return gameLevel.pac().tilesAhead(2).scaled(2).minus(blinky.tile());
        }
    }

    public static class Sue extends Ghost {

        @Override
        public String name() {
            return "Sue";
        }

        @Override
        public byte personality() {
            return ORANGE_GHOST_POKEY;
        }

        @Override
        public void hunt(GameLevel gameLevel) {
            float speed = gameLevel.game().actorSpeedControl().ghostAttackSpeed(gameLevel, this);
            boolean chase = gameLevel.game().huntingTimer().phase() == HuntingPhase.CHASING;
            Vector2i targetTile = chase ? chasingTargetTile(gameLevel) : gameLevel.ghostScatterTile(personality());
            setSpeed(speed);
            tryMovingTowardsTargetTile(gameLevel, targetTile);
        }

        @Override
        public Vector2i chasingTargetTile(GameLevel gameLevel) {
            return tile().euclideanDist(gameLevel.pac().tile()) < 8 ? gameLevel.ghostScatterTile(personality()) : gameLevel.pac().tile();
        }
    }

    private final GameContext gameContext;
    private final ScoreManager scoreManager;
    private final TengenMsPacMan_HUD hud = new TengenMsPacMan_HUD();
    private final TengenMsPacMan_MapSelector mapSelector;
    private final TengenActorSpeedControl actorSpeedControl;
    private final GateKeeper gateKeeper;
    private final HuntingTimer huntingTimer;
    private final Steering autopilot;
    private final Steering demoLevelSteering;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBooster;
    private boolean boosterActive;
    private int startLevelNumber; // 1-7
    private boolean canStartNewGame;
    private int numContinues;

    public TengenMsPacMan_GameModel(GameContext gameContext, File highScoreFile) {
        this.gameContext = requireNonNull(gameContext);
        scoreManager = new DefaultScoreManager(this, highScoreFile);
        actorSpeedControl = new TengenActorSpeedControl();
        mapSelector = new TengenMsPacMan_MapSelector();
        gateKeeper = new GateKeeper(this); //TODO implement original house logic
        huntingTimer = new TengenMsPacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) {
                gameLevel().ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::requestTurnBack);
            }
        });
        autopilot = new RuleBasedPacSteering(gameContext);
        demoLevelSteering = new RuleBasedPacSteering(gameContext);
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public TengenMsPacMan_HUD hud() {
        return hud;
    }

    public void init() {
        mapSelector.loadAllMaps();
        setInitialLifeCount(3);
        resetEverything();
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        setPacBooster(PacBooster.OFF);
        setDifficulty(Difficulty.NORMAL);
        setMapCategory(MapCategory.ARCADE);
        setStartLevelNumber(1);
        numContinues = 4;
    }

    @Override
    public void prepareForNewGame() {
        setLifeCount(initialLifeCount());
        setGameLevel(null);
        clearLevelCounter();
        setPlaying(false);
        boosterActive = false;
        scoreManager.loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
    }

    @Override
    public void onGameEnding() {
        setPlaying(false);
        scoreManager.updateHighScore();
        showMessage(gameLevel(), MessageType.GAME_OVER);
    }

    @Override
    public ActorSpeedControl actorSpeedControl() { return actorSpeedControl; }

    @Override
    public HuntingTimer huntingTimer() { return huntingTimer; }

    @Override
    public Optional<GateKeeper> optGateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public MapSelector mapSelector() { return mapSelector; }

    public boolean optionsAreInitial() {
        return pacBooster == PacBooster.OFF
            && difficulty == Difficulty.NORMAL
            && mapCategory == MapCategory.ARCADE
            && startLevelNumber == 1
            && numContinues == 4;
    }

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
            scoreManager.setExtraLifeScores(Set.of(10_000, 970_000, 980_000, 990_000));
        } else {
            scoreManager.setExtraLifeScores(Set.of(10_000, 50_000, 100_000, 300_000));
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
    public void startLevel() {
        gameLevel().setStartTime(System.currentTimeMillis());
        gameLevel().getReadyToPlay();
        resetPacManAndGhostAnimations();
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(gameLevel().pac(), true);
        }
        if (gameLevel().isDemoLevel()) {
            showMessage(gameLevel(), MessageType.GAME_OVER);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", gameLevel().number());

        } else {
            showMessage(gameLevel(), MessageType.READY);
            updateLevelCounter(gameLevel().number(), gameLevel().bonusSymbol(0));
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", gameLevel().number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager().publishEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void showMessage(GameLevel gameLevel, MessageType type) {
        if (type == MessageType.GAME_OVER && mapCategory != MapCategory.ARCADE) {
            var message = new MovingGameLevelMessage(type, gameLevel.defaultMessagePosition(), GAME_OVER_MESSAGE_DELAY);
            gameLevel.setMessage(message);
        } else {
            super.showMessage(gameLevel, type);
        }
    }

    @Override
    public void startNextLevel() {
        if (gameLevel().number() < LAST_LEVEL_NUMBER) {
            buildNormalLevel(gameLevel().number() + 1);
            startLevel();
            gameLevel().showPacAndGhosts();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", LAST_LEVEL_NUMBER);
        }
    }

    @Override
    public double pacPowerSeconds(GameLevel level) {
        if (level == null) return 0;
        int index = level.number() <= 19 ? level.number() - 1 : 18;
        return POWER_PELLET_TIMES[index] / 16.0;
    }

    @Override
    public double pacPowerFadingSeconds(GameLevel level) {
        return level != null ? level.data().numFlashes() * 0.5 : 0; // TODO check in emulator
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        //hud.levelCounter().setStartLevel(startLevelNumber);
        buildNormalLevel(startLevelNumber);
        eventManager().publishEvent(GameEventType.GAME_STARTED);
    }

    @Override
    public void resetPacManAndGhostAnimations() {
        gameLevel().pac().animationManager().ifPresent(am -> {
            am.select(boosterActive ? ANIM_MS_PAC_MAN_BOOSTER : AnimationSupport.ANIM_PAC_MUNCHING);
            am.reset();
        });
        gameLevel().ghosts().forEach(ghost -> ghost.animationManager().ifPresent(am -> {
            am.select(AnimationSupport.ANIM_GHOST_NORMAL);
            am.reset();
        }));
    }

    public void activatePacBooster(Pac pac, boolean active) {
        boosterActive = active;
        pac.selectAnimation(boosterActive ? ANIM_MS_PAC_MAN_BOOSTER : AnimationSupport.ANIM_PAC_MUNCHING);
    }

    @Override
    public OptionalInt optCutSceneNumber(int levelNumber) {
         return switch (levelNumber) {
             case 2 -> OptionalInt.of(1);
             case 5 -> OptionalInt.of(2);
             case 9, 13, 17 -> OptionalInt.of(3);
             default -> levelNumber == LAST_LEVEL_NUMBER ? OptionalInt.of(4) : OptionalInt.empty();
        };
    }

    @Override
    public void createLevel(int levelNumber) {
        setLevelCounterEnabled(levelNumber < 8);

        WorldMap worldMap = mapSelector.createConfiguredWorldMap(mapCategory, levelNumber);
        setGameLevel(new GameLevel(this, levelNumber, worldMap, createLevelData()));
        // For non-Arcade game levels, give some extra time for "game over" text animation
        gameLevel().setGameOverStateTicks(mapCategory == MapCategory.ARCADE ? 420 : 600);

        Pac msPacMan = createMsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        gameLevel().setPac(msPacMan);
        activatePacBooster(msPacMan, pacBooster == PacBooster.ALWAYS_ON);

        gameLevel().setGhosts(new Blinky(), new Pinky(), new Inky(), new Sue());
        gameLevel().ghosts().forEach(ghost -> {
            ghost.reset();
            // Ghosts inside house start at bottom of house instead at middle (as stored in terrain tile property)
            if (ghost.personality() != RED_GHOST_SHADOW) {
                ghost.setStartPosition(ghost.startPosition().plus(0, HTS));
            }
        });

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        gameLevel().setBonusSymbol(0, computeBonusSymbol(gameLevel().number()));
        gameLevel().setBonusSymbol(1, computeBonusSymbol(gameLevel().number()));
    }

    //TODO needed?
    private LevelData createLevelData() {
        // Note: only number of flashes is taken from level data
        return new LevelData(
            (byte) 0, // Pac speed %
            (byte) 0, // Ghost speed %
            (byte) 0, // Ghost tunnel speed %
            (byte) 0, // Elroy dots 1
            (byte) 0, // Elroy speed 1 %
            (byte) 0, // Elroy dots 2
            (byte) 0, // Elroy speed 2 %
            (byte) 0, // Pac speed powered %
            (byte) 0, // Ghost speed frightened %
            (byte) 0, // Pac power seconds
            (byte) 5  // Number of flashes
        );
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        gameLevel().setDemoLevel(false);
        gameLevel().pac().immuneProperty().bind(gameContext.gameController().propertyImmunity());
        gameLevel().pac().usingAutopilotProperty().bind(gameContext.gameController().propertyUsingAutopilot());
        huntingTimer().reset();
        scoreManager.setGameLevelNumber(levelNumber);
        optGateKeeper().ifPresent(gateKeeper -> {
            gateKeeper.setLevelNumber(levelNumber);
            gameLevel().optHouse().ifPresent(gateKeeper::setHouse); //TODO what if no house exists?
        });
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        createLevel(1);
        gameLevel().setDemoLevel(true);
        gameLevel().setGameOverStateTicks(120);
        gameLevel().pac().setImmune(false);
        gameLevel().pac().setUsingAutopilot(true);
        gameLevel().pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        huntingTimer.reset();
        scoreManager.setGameLevelNumber(1);
        optGateKeeper().ifPresent(gateKeeper -> {
            gateKeeper.setLevelNumber(1);
            gameLevel().optHouse().ifPresent(gateKeeper::setHouse); //TODO what if no house exists?
        });
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public int lastLevelNumber() { return LAST_LEVEL_NUMBER; }

    @Override
    protected boolean isPacManSafeInDemoLevel() {
        float levelRunningSeconds = (System.currentTimeMillis() - gameLevel().startTime()) / 1000f;
        if (gameLevel().isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isBonusReached() {
        return gameLevel().foodStore().eatenFoodCount() == 64 || gameLevel().foodStore().eatenFoodCount() == 176;
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
    public void activateNextBonus() {
        //TODO Find out how Tengen really implemented this
        if (gameLevel().isBonusEdible()) {
            Logger.info("Previous bonus is still active, skip");
            return;
        }

        // compute possible bonus route
        if (gameLevel().portals().isEmpty()) {
            Logger.error("No portal found in current maze");
            return; // TODO: can this happen?
        }
        House house = gameLevel().optHouse().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        boolean leftToRight = new Random().nextBoolean();
        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        Portal entryPortal = gameLevel().portals().get(new Random().nextInt(gameLevel().portals().size()));
        Portal exitPortal  = gameLevel().portals().get(new Random().nextInt(gameLevel().portals().size()));
        List<Waypoint> route = Stream.of(
                leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
                houseEntry,
                houseEntryOpposite,
                houseEntry,
                leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(Waypoint::new).toList();

        gameLevel().selectNextBonus();
        byte symbol = gameLevel().bonusSymbol(gameLevel().currentBonusIndex());
        var bonus = new Bonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100, new Pulse(10, false));
        bonus.setEdible(TickTimer.INDEFINITE);
        bonus.setRoute(route, leftToRight);
        //bonus.setBaseSpeed(0.9f * level.speedControl().pacNormalSpeed(level)); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        gameLevel().setBonus(bonus);
        eventManager().publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }

    @Override
    protected void checkIfPacManFindsFood() {
        optGameLevel().ifPresent(gameLevel -> {
            final Pac pac = gameLevel.pac();
            final Vector2i tile = pac.tile();
            if (gameLevel.foodStore().tileContainsFood(tile)) {
                pac.setStarvingTicks(0);
                gameLevel.foodStore().registerFoodEatenAt(tile);
                optGateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(gameLevel));
                if (gameLevel.isEnergizerPosition(tile)) {
                    simulationStep.foundEnergizerAtTile = tile;
                    onEnergizerEaten();
                } else {
                    scoreManager.scorePoints(PELLET_VALUE);
                }
                if (isBonusReached()) {
                    activateNextBonus();
                    simulationStep.bonusIndex = gameLevel.currentBonusIndex();
                }
                eventManager().publishEvent(GameEventType.PAC_FOUND_FOOD, tile);
            } else {
                pac.setStarvingTicks(pac.starvingTicks() + 1);
            }
        });
    }

    @Override
    public void onPelletEaten() {
        scoreManager().scorePoints(PELLET_VALUE);
        gameLevel().pac().setRestingTicks(1);
    }

    @Override
    public void onEnergizerEaten(Vector2i tile) {
        simulationStep.foundEnergizerAtTile = tile;
        scoreManager().scorePoints(ENERGIZER_VALUE);
        gameLevel().pac().setRestingTicks(3);
        gameLevel().victims().clear();
        gameLevel().ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::requestTurnBack);
        double powerSeconds = pacPowerSeconds(gameLevel());
        if (powerSeconds > 0) {
            huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            gameLevel().pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            gameLevel().ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            simulationStep.pacGotPower = true;
            eventManager().publishEvent(GameEventType.PAC_GETS_POWER);
        }
    }

    private void onEnergizerEaten() {
        scoreManager.scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        gameLevel().victims().clear();
        double powerSeconds = pacPowerSeconds(gameLevel());
        long powerTicks = secToTicks(powerSeconds);
        if (powerTicks > 0) {
            huntingTimer.stop();
            Logger.info("Hunting Pac-Man stopped as he got power");
            gameLevel().pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerSeconds);
            gameLevel().ghosts(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            gameLevel().ghosts(GhostState.FRIGHTENED).forEach(Ghost::requestTurnBack);
            simulationStep.pacGotPower = true;
            eventManager().publishEvent(GameEventType.PAC_GETS_POWER);
        } else {
            gameLevel().ghosts(GhostState.FRIGHTENED, GhostState.HUNTING_PAC).forEach(Ghost::requestTurnBack);
        }
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        gameLevel().pac().powerTimer().stop();
        gameLevel().pac().powerTimer().reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true); // TODO how is that realized in original game?
        gameLevel().pac().sayGoodbyeCruelWorld();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        simulationStep.killedGhosts.add(ghost);
        int killedSoFar = gameLevel().victims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        gameLevel().victims().add(ghost);
        ghost.setState(GhostState.EATEN);
        ghost.selectAnimationAt(AnimationSupport.ANIM_GHOST_NUMBER, killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    @Override
    public GameEventManager eventManager() {
        return gameContext.eventManager();
    }
}