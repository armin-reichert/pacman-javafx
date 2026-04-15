/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static de.amr.pacmanfx.lib.math.Vector2i.vec2_int;
import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    protected static final LevelData[] LEVEL_DATA_TABLE = {
        /* 1*/ LevelData.of( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
        /* 2*/ LevelData.of( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
        /* 3*/ LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
        /* 4*/ LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
        /* 5*/ LevelData.of(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
        /* 6*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
        /* 7*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 8*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 9*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
        /*10*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
        /*11*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
        /*12*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*13*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*14*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
        /*15*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*16*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*17*/ LevelData.of(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
        /*18*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*19*/ LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*20*/ LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*21*/ LevelData.of( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
    };

    public class SpeedControl implements ActorSpeedControl {
        /** Base speed is 75 px per second (=1.25 px/tick). */
        public static final float BASE_SPEED = 1.25f;
        public static final float BASE_SPEED_1_PERCENT = 0.01f * BASE_SPEED;

        @Override
        public float bonusSpeed(GameLevel level) {
            //TODO clarify exact speed
            return 0.5f * pacSpeed(level);
        }

        @Override
        public float pacSpeed(GameLevel level) {
            final LevelData data = levelData(level.number());
            byte percentage = data.pctPacSpeed();
            return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
        }

        @Override
        public float pacSpeedWhenHasPower(GameLevel level) {
            final LevelData data = levelData(level.number());
            byte percentage = data.pctPacSpeedPowered();
            return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacSpeed(level);
        }

        @Override
        public float ghostSpeed(GameLevel level, Ghost ghost) {
            final int levelNumber = level.number();
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean insideHouse  = terrain.house().isVisitedBy(ghost);
            final boolean insideTunnel = terrain.isTunnel(ghost.tile());
            return switch (ghost.state()) {
                case LOCKED -> insideHouse ? 0.5f : 0;
                case LEAVING_HOUSE -> 0.5f;
                case HUNTING_PAC -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedAttacking(level, ghost);
                case FRIGHTENED -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedWhenFrightened(level);
                case EATEN -> 0;
                case RETURNING_HOME, ENTERING_HOUSE -> 2;
            };
        }

        @Override
        public float ghostSpeedAttacking(GameLevel level, Ghost ghost) {
            final int levelNumber = level.number();
            final LevelData data = levelData(levelNumber);
            if (ghost instanceof RedGhostShadow redGhostShadow) {
                return switch (redGhostShadow.elroyState().mode()) {
                    case ZERO -> data.pctGhostSpeed()  * BASE_SPEED_1_PERCENT;
                    case ONE -> data.pctElroy1Speed() * BASE_SPEED_1_PERCENT;
                    case TWO -> data.pctElroy2Speed() * BASE_SPEED_1_PERCENT;
                };
            } else {
                return data.pctGhostSpeed() * BASE_SPEED_1_PERCENT;
            }
        }

        @Override
        public float ghostSpeedWhenFrightened(GameLevel level) {
            final int levelNumber = level.number();
            final LevelData data = levelData(levelNumber);
            float percentage = data.pctGhostSpeedFrightened();
            return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
        }

        @Override
        public float ghostSpeedTunnel(int levelNumber) {
            final LevelData data = levelData(levelNumber);
            return data.pctGhostSpeedTunnel() * BASE_SPEED_1_PERCENT;
        }
    }

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = vec2_int(10, 15);

    private static final float BONUS_EATEN_SECONDS = 2;

    protected final Arcade_GameFlow gameFlow;
    protected final CoinMechanism coinMechanism;

    protected HeadsUpDisplay hud;
    protected GateKeeper gateKeeper;
    protected Steering automaticSteering;
    protected Steering demoLevelSteering;
    protected ActorSpeedControl actorSpeedControl;

    protected int restingTicksAfterPelletEaten;
    protected int restingTicksAfterEnergizerEaten;

    protected int bonus1PelletsEaten;
    protected int bonus2PelletsEaten;

    protected Arcade_GameModel(CoinMechanism coinMechanism, File highscoreFile) {
        super(highscoreFile);

        this.coinMechanism = requireNonNull(coinMechanism);
        hud = new HeadsUpDisplay(coinMechanism);
        gameFlow = new Arcade_GameFlow(this);
        actorSpeedControl = new SpeedControl();
        pelletPoints = 10;
        energizerPoints = 50;
        restingTicksAfterPelletEaten = 1;
        restingTicksAfterEnergizerEaten = 3;
        setExtraLifeScores(10_000);
        setCollisionStrategy(CollisionStrategy.SAME_TILE);
    }

    protected int cutSceneNumberAfterLevel(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> 1; // after level #2, play cut scene #1
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    public abstract LevelData levelData(int levelNumber);

    @Override
    public GameFlow flow() {
        return gameFlow;
    }

    @Override
    public ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public void eatPellet(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(level, pelletPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.pac().setRestingTicks(restingTicksAfterPelletEaten);
        checkRedGhostCruiseElroyActivation(level);
    }

    @Override
    public void eatEnergizer(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final Pac pac = level.pac();

        scorePoints(level, energizerPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        pac.setRestingTicks(restingTicksAfterEnergizerEaten);
        checkRedGhostCruiseElroyActivation(level);

        if (!isLevelCompleted()) {
            level.ghosts(GhostState.FRIGHTENED, GhostState.HUNTING_PAC).forEach(MovingActor::requestTurnBack);
            level.energizerVictims().clear();
            final float powerSeconds = level.pacPowerSeconds();
            if (powerSeconds > 0) {
                level.huntingTimer().stop();
                Logger.debug("Hunting stopped (Pac-Man got power)");
                final long powerTicks = TickTimer.secToTicks(powerSeconds);
                pac.powerTimer().restartTicks(powerTicks);
                Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
                level.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
                simStep.pacGotPower = true;
                flow().publishGameEvent(new PacGetsPowerEvent(this, pac));
            }
        }
    }

    protected int killedGhostValue(int alreadyKilled) {
        return switch (alreadyKilled) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            case 3 -> 1600;
            default -> throw new IllegalArgumentException("Illegal number of already killed ghosts: " + alreadyKilled);
        };
    }

    protected void checkRedGhostCruiseElroyActivation(GameLevel level) {
        final RedGhostShadow redGhost = (RedGhostShadow) level.ghost(Globals.RED_GHOST_SHADOW);
        if (redGhost != null) {
            final LevelData data = levelData(level.number());
            final int uneatenFoodCount = level.worldMap().foodLayer().remainingFoodCount();
            if (uneatenFoodCount == data.numDotsLeftElroy1()) {
                redGhost.elroyState().setMode(ElroyState.Mode.ONE);
            } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
                redGhost.elroyState().setMode(ElroyState.Mode.TWO);
            }
        } else {
            throw new IllegalStateException("Red ghost not existing in this level");
        }
    }

    protected void showLevelMessage(GameLevel level, GameLevelMessageType type) {
        final var message = new GameLevelMessage(type);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
    }

    @Override
    public void doLevelPlaying() {
        final GameLevel level = optGameLevel().orElseThrow();
        level.pac().show();
        level.ghosts().forEach(Ghost::show);
        doHuntingStep(level);
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
    }

    @Override
    public void doPacManDying(Pac pac, long tick) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (tick == 1) {
            gateKeeper.resetCounterAndSetEnabled(true);
            level.huntingTimer().stop();
            pac.stopAnimation();
            pac.powerTimer().stop();
            pac.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero.");
            pac.setSpeed(0);
            pac.setDead(true);
            level.ghosts().forEach(ghost -> ghost.onPacKilled(level));
            flow().publishGameEvent(new StopAllSoundsEvent(this));
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.ghosts().forEach(Ghost::hide);
            pac.animations().selectAnimation(Pac.AnimationID.PAC_DYING);
            pac.animations().resetSelectedAnimation();
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_START_ANIMATION) {
            pac.playAnimation();
            flow().publishGameEvent(new PacDyingEvent(this, pac));
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == Arcade_GameState.TICK_PACMAN_DYING_PAC_DEAD) {
            flow().publishGameEvent(new PacDeadEvent(this, pac));
        }
        else {
            level.blinking().doTick();
            pac.tick(this);
        }
    }

    @Override
    public void onEatGhost(Ghost ghost) {
        final GameLevel level = optGameLevel().orElseThrow();
        final int alreadyKilled = level.energizerVictims().size();
        final int points = killedGhostValue(alreadyKilled);
        scorePoints(level, points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());

        ghost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        ghost.selectAnimationAndSetFrame(Ghost.AnimationID.GHOST_POINTS, alreadyKilled);

        level.energizerVictims().add(ghost);
        level.incrementGhostKillCount();
        level.pac().hide();
        level.ghosts().forEach(Ghost::stopAnimation);
        flow().publishGameEvent(new GhostEatenEvent(this, ghost));
    }

    @Override
    public void doEatingGhost(long tick) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (tick < Arcade_GameState.TICK_EATING_GHOST_DURATION) {
            level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                .forEach(ghost -> ghost.tick(this));
            level.blinking().doTick();
        }
        else if (tick == Arcade_GameState.TICK_EATING_GHOST_DURATION) {
            level.pac().show();
            level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            level.ghosts().forEach(Actor::playAnimation);
        }
    }

    @Override
    public void onGameOver() {
        if (!coinMechanism.isEmpty()) {
            coinMechanism.consumeCoin(); //TODO not sure if coin should be consumed after game is over
        }
        setPlayingLevel(false);
        final GameLevel level = optGameLevel().orElseThrow();
        showLevelMessage(level, GameLevelMessageType.GAME_OVER);
        try {
            updateHighScore();
        } catch (IOException x) {
            Logger.error(x, "Error updating highscore file {}", highScore.file().getAbsolutePath());
        }
        Logger.info("Game ended with level number {}", level.number());
    }

    // GameLifecycle interface

    @Override
    public void init() {
        setInitialLifeCount(3);
        clearCheatingProperties();
        prepareNewGame();
        levelCounter().clear();
    }

    @Override
    public void prepareNewGame() {
        score.reset();
        try {
            highScore.load();
            highScore.setEnabled(true);
        } catch (IOException x) {
            Logger.error(x, "Error loading highscore file {}", highScore.file().getAbsolutePath());
        }
        gateKeeper.reset();
        levelProperty().set(null);
        lifeCountProperty().set(initialLifeCount());
        levelCounter().clear();
        setPlayingLevel(false);
    }

    @Override
    public void startNewGame(long tick) {
        if (tick == 1) {
            prepareNewGame();
            buildNormalLevel(1);
            flow().publishGameEvent(new GameStartedEvent(this));
        }
        else if (tick == 2) {
            startLevel();
        }
        else if (tick == Arcade_GameState.TICK_NEW_GAME_SHOW_GUYS) {
            final GameLevel level = optGameLevel().orElseThrow();
            level.pac().show();
            level.ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.TICK_NEW_GAME_START_HUNTING) {
            setPlayingLevel(true);
            flow().enterState(Arcade_GameState.LEVEL_PLAYING);
        }
    }

    @Override
    public void continuePlayingLevel(long tick) {
        final GameLevel level = optGameLevel().orElseThrow();
        if (tick == 1) {
            makeReadyForPlaying(level);
            level.pac().show();
            level.ghosts().forEach(Ghost::show);
            showLevelMessage(level, GameLevelMessageType.READY);
        }
        else if (tick == 60) {
            flow().publishGameEvent(new GameContinuedEvent(this));
        }
        else if (tick == Arcade_GameState.TICK_RESUME_HUNTING) {
            flow().enterState(Arcade_GameState.LEVEL_PLAYING);
        }
    }

    @Override
    public boolean canStartNewGame() { return !coinMechanism.isEmpty(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    protected void eatBonus(GameLevel level, Bonus bonus) {
        scorePoints(level, bonus.points());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(BONUS_EATEN_SECONDS);
        flow().publishGameEvent(new BonusEatenEvent(this, bonus));
    }

    @Override
    public int lastIntermissionNumber() {
        return 3;
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        level.setCutSceneNumber(cutSceneNumberAfterLevel(levelNumber));
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        flow().publishGameEvent(new LevelCreatedEvent(this, level));
    }

    @Override
    public void buildDemoLevel() {
        int levelNumber = 1;
        final GameLevel level = createLevel(levelNumber, true);
        level.setCutSceneNumber(0);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        flow().publishGameEvent(new LevelCreatedEvent(this, level));
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
            // Now, actor animations are available, show them
            final GameLevel level = optGameLevel().orElseThrow();
            level.pac().show();
            level.ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.TICK_RESUME_HUNTING) {
            flow().enterState(Arcade_GameState.LEVEL_PLAYING);
        }
    }

    @Override
    public void startLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        level.recordStartTime(System.currentTimeMillis());
        makeReadyForPlaying(level);
        if (level.isDemoLevel()) {
            showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            score().setEnabled(false);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showLevelMessage(level, GameLevelMessageType.READY);
            levelCounter().update(level.number(), level.bonusSymbol(0));
            score().setEnabled(true);
            updateCheatingProperties(level);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        flow().publishGameEvent(new LevelStartedEvent(this, level));
    }

    @Override
    public void startNextLevel() {
        final GameLevel level = optGameLevel().orElseThrow();
        buildNormalLevel(level.number() + 1);
        startLevel();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }
}