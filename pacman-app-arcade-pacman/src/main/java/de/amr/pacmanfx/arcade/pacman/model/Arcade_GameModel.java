/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.model.actors.Blinky;
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
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    // Level data as given in the "Pac-Man Dossier"
    protected static final LevelData[] LEVEL_DATA = {
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

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = Vector2i.of(10, 15);

    private static final float BONUS_EATEN_SECONDS = 2;

    protected final CoinMechanism coinMechanism;

    protected HeadsUpDisplay hud;
    protected GateKeeper gateKeeper;
    protected Steering automaticSteering;
    protected Steering demoLevelSteering;

    protected int restingTicksAfterPelletEaten;
    protected int restingTicksAfterEnergizerEaten;

    protected int bonus1PelletsEaten;
    protected int bonus2PelletsEaten;

    protected Arcade_GameModel(CoinMechanism coinMechanism, File highscoreFile) {
        super(highscoreFile);
        this.coinMechanism = requireNonNull(coinMechanism);
        this.hud = new HeadsUpDisplay(coinMechanism);

        pelletPoints = 10;
        energizerPoints = 50;
        restingTicksAfterPelletEaten = 1;
        restingTicksAfterEnergizerEaten = 3;
        setExtraLifeScores(10_000);
        setCollisionStrategy(CollisionStrategy.SAME_TILE);

        setGameControl(new Arcade_GameController());
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
    public void eatPellet(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(level, pelletPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.pac().setRestingTicks(restingTicksAfterPelletEaten);
        checkCruiseElroyActivation(level);
    }

    @Override
    public void eatEnergizer(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        scorePoints(level, energizerPoints);
        gateKeeper.registerFoodEaten(level, level.worldMap().terrainLayer().house());
        level.pac().setRestingTicks(restingTicksAfterEnergizerEaten);
        checkCruiseElroyActivation(level);

        if (!isLevelCompleted(level)) {
            level.ghosts(GhostState.FRIGHTENED, GhostState.HUNTING_PAC).forEach(MovingActor::requestTurnBack);
            level.energizerVictims().clear();
            final float powerSeconds = level.pacPowerSeconds();
            if (powerSeconds > 0) {
                level.huntingTimer().stop();
                Logger.debug("Hunting stopped (Pac-Man got power)");
                final long powerTicks = TickTimer.secToTicks(powerSeconds);
                level.pac().powerTimer().restartTicks(powerTicks);
                Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
                level.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
                simStep.pacGotPower = true;
                publishGameEvent(new PacGetsPowerEvent());
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

    protected void checkCruiseElroyActivation(GameLevel level) {
        final LevelData data = levelData(level.number());
        final int uneatenFoodCount = level.worldMap().foodLayer().uneatenFoodCount();
        if (uneatenFoodCount == data.numDotsLeftElroy1()) {
            optBlinky(level).ifPresent(blinky -> blinky.setElroyMode(Blinky.ElroyMode._1));
        } else if (uneatenFoodCount == data.numDotsLeftElroy2()) {
            optBlinky(level).ifPresent(blinky -> blinky.setElroyMode(Blinky.ElroyMode._2));
        }
    }

    private Optional<Blinky> optBlinky(GameLevel level) {
        return level.ghosts().filter(Blinky.class::isInstance).map(Blinky.class::cast).findAny();
    }

    // GameEvents interface

    @Override
    public void whileHunting(GameLevel level) {
        doHuntingStep(level);
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
    }

    @Override
    public void whilePacManDying(GameLevel level, Pac pac, long tick) {
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
            publishGameEvent(new StopAllSoundsEvent());
        }
        else if (tick == Arcade_GameController.TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.ghosts().forEach(Ghost::hide);
            pac.optAnimationManager().ifPresent(am -> {
                am.select(Pac.AnimationID.PAC_DYING);
                am.reset();
            });
        }
        else if (tick == Arcade_GameController.TICK_PACMAN_DYING_START_ANIMATION) {
            pac.optAnimationManager().ifPresent(AnimationManager::play);
            publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == Arcade_GameController.TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == Arcade_GameController.TICK_PACMAN_DYING_PAC_DEAD) {
            publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.blinking().tick();
            pac.tick(this);
        }
    }

    @Override
    public void onEatGhost(GameLevel level, Ghost ghost) {
        final int alreadyKilled = level.energizerVictims().size();
        final int points = killedGhostValue(alreadyKilled);
        scorePoints(level, points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());

        ghost.setState(GhostState.EATEN);
        // Animation index is 0-based, so use animation frame 0 to show points for first killed ghost...
        ghost.selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, alreadyKilled);

        level.energizerVictims().add(ghost);
        level.incrementGhostKillCount();
        level.pac().hide();
        level.ghosts().forEach(Ghost::stopAnimation);
        publishGameEvent(new GhostEatenEvent(ghost));
    }

    @Override
    public void whileEatingGhost(GameLevel level, long tick) {
        if (tick < Arcade_GameController.TICK_EATING_GHOST_COMPLETE) {
            level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                .forEach(ghost -> ghost.tick(this));
            level.blinking().tick();
        }
        else if (tick == Arcade_GameController.TICK_EATING_GHOST_COMPLETE) {
            level.pac().show();
            level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        }
    }

    @Override
    public void onGameOver() {
        setPlaying(false);
        if (!coinMechanism.isEmpty()) {
            coinMechanism.consumeCoin();
        }
        showLevelMessage(GameLevelMessageType.GAME_OVER);
        try {
            updateHighScore();
        } catch (IOException x) {
            Logger.error(x, "Error updating highscore file {}", highScoreFile.getAbsolutePath());
        }
        Logger.info("Game ended with level number {}", level().number());
    }

    // GameLifecycle interface

    @Override
    public void boot() {
        setInitialLifeCount(3);
        clearCheatingProperties();
        prepareNewGame();
        clearLevelCounter();
        hud().hide();
    }

    @Override
    public void prepareNewGame() {
        score().reset();
        try {
            loadHighScore();
        } catch (IOException x) {
            Logger.error(x, "Error updating highscore file {}", highScoreFile.getAbsolutePath());
        }
        highScore().setEnabled(true);
        gateKeeper.reset();
        levelProperty().set(null);
        setPlaying(false);
        lifeCountProperty().set(initialLifeCount());
    }

    @Override
    public void startNewGame(long tick) {
        if (tick == 1) {
            prepareNewGame();
            clearLevelCounter();
            buildNormalLevel(1);
            publishGameEvent(new GameStartedEvent(this));
        }
        else if (tick == 2) {
            startLevel(level());
        }
        else if (tick == Arcade_GameController.TICK_NEW_GAME_SHOW_GUYS) {
            level().showPacAndGhosts();
        }
        else if (tick == Arcade_GameController.TICK_NEW_GAME_START_HUNTING) {
            setPlaying(true);
            control().enterState(GameState.HUNTING);
        }
    }

    @Override
    public void continuePlaying(GameLevel level, long tick) {
        if (tick == 1) {
            level.getReadyToPlay();
            level.showPacAndGhosts();
            showLevelMessage(GameLevelMessageType.READY);
        }
        else if (tick == 60) {
            publishGameEvent(new GameContinuedEvent(this));
        }
        else if (tick == Arcade_GameController.TICK_RESUME_HUNTING) {
            control().enterState(GameState.HUNTING);
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
        bonus.setEatenSeconds(BONUS_EATEN_SECONDS);
        publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public int lastCutSceneNumber() {
        return 3;
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        level.setCutSceneNumber(cutSceneNumberAfterLevel(levelNumber));
        setLevelCounterEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(new LevelCreatedEvent(level));
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
        setLevelCounterEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void startDemoLevel(long tick) {
        if (tick == 1) {
            buildDemoLevel();
        }
        else if (tick == 2) {
            startLevel(level());
        }
        else if (tick == 3) {
            // Now, actor animations are available
            level().showPacAndGhosts();
        }
        else if (tick == Arcade_GameController.TICK_RESUME_HUNTING) {
            control().enterState(GameState.HUNTING);
        }
    }

    @Override
    public void startLevel(GameLevel level) {
        level.recordStartTime(System.currentTimeMillis());
        level.getReadyToPlay();
        if (level.isDemoLevel()) {
            showLevelMessage(GameLevelMessageType.GAME_OVER);
            score().setEnabled(false);
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
        publishGameEvent(new LevelStartedEvent(level));
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(level().number() + 1);
        startLevel(level());
    }

    @Override
    public void showLevelMessage(GameLevelMessageType type) {
        requireNonNull(type);
        optGameLevel().ifPresent(level -> {
            final var message = new GameLevelMessage(type);
            message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
            level.setMessage(message);
        });
    }

    @Override
    public void clearLevelMessage() {
        optGameLevel().ifPresent(GameLevel::clearMessage);
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }

    // ActorSpeedControl interface

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
        if (ghost instanceof Blinky blinky) {
            return switch (blinky.elroyMode()) {
                case NONE -> data.pctGhostSpeed() * BASE_SPEED_1_PERCENT;
                case _1 -> data.pctElroy1Speed() * BASE_SPEED_1_PERCENT;
                case _2 -> data.pctElroy2Speed() * BASE_SPEED_1_PERCENT;
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