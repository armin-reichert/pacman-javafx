/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.model.actors.Blinky;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    static final short TICK_NEW_GAME_SHOW_GUYS = 120;
    static final short TICK_NEW_GAME_START_HUNTING = 240;
    static final short TICK_RESUME_HUNTING =  90;
    static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
    static final short TICK_EATING_GHOST_COMPLETE = 60;
    static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
    static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    static final short TICK_PACMAN_DYING_PAC_DEAD = 240;

    // Level data as given in the "Pac-Man Dossier"
    protected static final Arcade_LevelData[] LEVEL_DATA = {
        /* 1*/ Arcade_LevelData.of( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
        /* 2*/ Arcade_LevelData.of( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
        /* 3*/ Arcade_LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
        /* 4*/ Arcade_LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
        /* 5*/ Arcade_LevelData.of(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
        /* 6*/ Arcade_LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
        /* 7*/ Arcade_LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 8*/ Arcade_LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 9*/ Arcade_LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
        /*10*/ Arcade_LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
        /*11*/ Arcade_LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
        /*12*/ Arcade_LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*13*/ Arcade_LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*14*/ Arcade_LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
        /*15*/ Arcade_LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*16*/ Arcade_LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*17*/ Arcade_LevelData.of(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
        /*18*/ Arcade_LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*19*/ Arcade_LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*20*/ Arcade_LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*21*/ Arcade_LevelData.of( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
    };

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = Vector2i.of(10, 15);

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;
    public static final int ALL_16_GHOSTS_KILLED_POINTS = 12_000;
    public static final int EXTRA_LIFE_SCORE = 10_000;
    public static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    private static final float BONUS_EATEN_SECONDS = 2;

    protected final CoinMechanism coinMechanism;

    protected GateKeeper gateKeeper;
    protected Steering automaticSteering;
    protected Steering demoLevelSteering;

    protected Arcade_GameModel(CoinMechanism coinMechanism) {
        super(new Arcade_GameController());
        this.coinMechanism = requireNonNull(coinMechanism);
        setCollisionStrategy(CollisionStrategy.SAME_TILE);
    }

    public abstract Arcade_LevelData levelData(int levelNumber);

    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{] home tile not found inside map", ghost.name());
        }
    }

    // public for access by tests
    public void onPelletEaten(GameLevel gameLevel) {
        scoreManager.scorePoints(PELLET_VALUE);
        gameLevel.pac().onFoodEaten(false);
        gameLevel.ghosts().forEach(ghost -> ghost.onFoodCountChange(gameLevel));
    }

    // public for access by tests
    public void onEnergizerEaten(GameLevel gameLevel, Vector2i tile) {
        simulationStepResult.foundEnergizerAtTile = tile;
        scoreManager.scorePoints(ENERGIZER_VALUE);
        gameLevel.pac().onFoodEaten(true);
        gameLevel.ghosts().forEach(ghost -> {
            ghost.onFoodCountChange(gameLevel);
            if (ghost.inAnyOfStates(FRIGHTENED, HUNTING_PAC)) {
                ghost.requestTurnBack();
            }
        });
        gameLevel.energizerVictims().clear();
        // Pac gets power?
        final float powerSeconds = gameLevel.pacPowerSeconds();
        if (powerSeconds > 0) {
            gameLevel.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            gameLevel.pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            gameLevel.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            simulationStepResult.pacGotPower = true;
            publishGameEvent(GameEvent.Type.PAC_GETS_POWER);
        }
    }

    // GameEvents interface

    @Override
    public void updateHunting() {
        doHuntingStep();
        if (gateKeeper != null) {
            gateKeeper.unlockGhosts(level());
        }
    }

    @Override
    public void updatePacManDying(long tick) {
        final GameLevel level = level();
        final Pac pac = level.pac();
        if (tick == 1) {
            gateKeeper.resetCounterAndSetEnabled(true);
            level.huntingTimer().stop();
            level.pac().onKilled();
            level.ghosts().forEach(ghost -> ghost.onPacKilled(level));
            publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.ghosts().forEach(Ghost::hide);
            pac.optAnimationManager().ifPresent(am -> {
                am.select(CommonAnimationID.ANIM_PAC_DYING);
                am.reset();
            });
        }
        else if (tick == TICK_PACMAN_DYING_START_ANIMATION) {
            pac.optAnimationManager().ifPresent(AnimationManager::play);
            publishGameEvent(GameEvent.Type.PAC_DYING, pac.tile());
        }
        else if (tick == TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
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
    public void onEatGhost(Ghost ghost) {
        final GameLevel level = level();
        final int killedSoFar = level.energizerVictims().size();
        final int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        level.energizerVictims().add(ghost);
        ghost.setState(GhostState.EATEN);
        ghost.selectAnimationAt(CommonAnimationID.ANIM_GHOST_NUMBER, killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        level.incrementGhostKillCount();
        if (level.ghostKillCount() == 16) {
            scoreManager.scorePoints(ALL_16_GHOSTS_KILLED_POINTS);
            Logger.info("Scored {} points for killing all ghosts in level {}", ALL_16_GHOSTS_KILLED_POINTS, level.number());
        }
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

    @Override
    public void onGameOver() {
        setPlaying(false);
        if (!coinMechanism.noCoin()) {
            coinMechanism.consumeCoin();
        }
        scoreManager.updateHighScore();
        showLevelMessage(MessageType.GAME_OVER);
        Logger.info("Game ended with level number {}", level().number());
    }

    // GameLifecycle interface

    @Override
    public void boot() {
        setInitialLifeCount(3);
        cheatUsedProperty().set(false);
        immunityProperty().set(false);
        usingAutopilotProperty().set(false);
        prepareForNewGame();
        levelCounter().clear();
        hud().all(false);
    }

    @Override
    public void prepareForNewGame() {
        setPlaying(false);
        lifeCountProperty().set(initialLifeCount());
        scoreManager.loadHighScore();
        scoreManager.score().reset();
        scoreManager.highScore().setEnabled(true);
        gateKeeper.reset();
        gameLevelProperty().set(null);
    }

    @Override
    public void startNewGame(long tick) {
        if (tick == 1) {
            prepareForNewGame();
            levelCounter().clear();
            buildNormalLevel(1);
            publishGameEvent(GameEvent.Type.GAME_STARTED);
        }
        else if (tick == 2) {
            if (!level().isDemoLevel()) {
                level().pac().immuneProperty().bind(immunityProperty());
                level().pac().usingAutopilotProperty().bind(usingAutopilotProperty());
                boolean cheating = immunity() || usingAutopilot();
                cheatUsedProperty().set(cheating);
            }
            startLevel();
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level().showPacAndGhosts();
        }
        else if (tick == TICK_NEW_GAME_START_HUNTING) {
            setPlaying(true);
            control().changeState(GameState.HUNTING);
        }
    }

    @Override
    public void continuePlaying(long tick) {
        if (tick == 1) {
            final GameLevel level = level();
            //resetPacManAndGhostAnimations(level);
            level.getReadyToPlay();
            level.showPacAndGhosts();
            publishGameEvent(GameEvent.Type.GAME_CONTINUED);
        } else if (tick == TICK_RESUME_HUNTING) {
            control().changeState(GameState.HUNTING);
        }
    }

    @Override
    public boolean canStartNewGame() { return !coinMechanism.noCoin(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    public void checkPacFindsFood(GameLevel gameLevel) {
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        final Pac pac = gameLevel.pac();
        final Vector2i tile = pac.tile();
        if (foodLayer.hasFoodAtTile(tile)) {
            foodLayer.registerFoodEatenAt(tile);
            pac.endStarving();
            if (foodLayer.isEnergizerTile(tile)) {
                onEnergizerEaten(gameLevel, tile);
            } else {
                onPelletEaten(gameLevel);
            }
            gateKeeper.registerFoodEaten(gameLevel);
            if (isBonusReached()) {
                activateNextBonus();
                simulationStepResult.bonusIndex = gameLevel.currentBonusIndex();
            }
            publishGameEvent(GameEvent.Type.PAC_FOUND_FOOD, tile);
        } else {
            pac.starve();
        }
    }

    @Override
    protected void checkPacFindsBonus(GameLevel gameLevel) {
        gameLevel.optBonus().filter(bonus -> bonus.state() == BonusState.EDIBLE).ifPresent(bonus -> {
            if (collisionStrategy().collide(gameLevel.pac(), bonus)) {
                bonus.setEatenSeconds(BONUS_EATEN_SECONDS);
                scoreManager.scorePoints(bonus.points());
                Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
                simulationStepResult.bonusEatenTile = bonus.tile();
                publishGameEvent(GameEvent.Type.BONUS_EATEN);
            }
        });
    }

    protected int cutSceneNumberAfter(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> 1; // after level #2, play cut scene #1
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> 0;
        };
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        level.setCutSceneNumber(cutSceneNumberAfter(levelNumber));
        levelCounter().setEnabled(true);
        scoreManager.score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));

        gameLevelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
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
        scoreManager.score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));

        gameLevelProperty().set(level);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
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
            control().changeState(GameState.HUNTING);
        }
    }

    @Override
    public void startLevel() {
        final GameLevel level = level();
        level.setStartTimeMillis(System.currentTimeMillis());
        level.getReadyToPlay();
        if (level.isDemoLevel()) {
            showLevelMessage(MessageType.GAME_OVER);
            scoreManager.score().setEnabled(false);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            showLevelMessage(MessageType.READY);
            levelCounter().update(level.number(), level.bonusSymbol(0));
            scoreManager.score().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        publishGameEvent(GameEvent.Type.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(level().number() + 1);
        startLevel();
    }

    @Override
    public void showLevelMessage(MessageType type) {
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
    public float bonusSpeed(GameLevel gameLevel) {
        //TODO clarify exact speed
        return 0.5f * pacSpeed(gameLevel);
    }

    @Override
    public float pacSpeed(GameLevel gameLevel) {
        final Arcade_LevelData data = levelData((gameLevel.number()));
        byte percentage = data.pctPacSpeed();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacSpeedWhenHasPower(GameLevel gameLevel) {
        final Arcade_LevelData data = levelData((gameLevel.number()));
        byte percentage = data.pctPacSpeedPowered();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacSpeed(gameLevel);
    }

    @Override
    public float ghostSpeedWhenAttacking(GameLevel gameLevel, Ghost ghost) {
        final Arcade_LevelData data = levelData((gameLevel.number()));
        if (gameLevel.worldMap().terrainLayer().isTunnel(ghost.tile())) {
            return ghostSpeedInsideTunnel(gameLevel, ghost);
        }
        if (ghost instanceof Blinky blinky) {
            if (blinky.cruiseElroyValue() == 1) {
                return data.pctElroy1Speed() * BASE_SPEED_1_PERCENT;
            }
            if (blinky.cruiseElroyValue() == 2) {
                return data.pctElroy2Speed() * BASE_SPEED_1_PERCENT;
            }
        }
        return data.pctGhostSpeed() * BASE_SPEED_1_PERCENT;
    }

    @Override
    public float ghostSpeedInsideHouse(GameLevel gameLevel, Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(GameLevel gameLevel, Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostSpeedWhenFrightened(GameLevel gameLevel, Ghost ghost) {
        final Arcade_LevelData data = levelData((gameLevel.number()));
        float percentage = data.pctGhostSpeedFrightened();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostSpeedInsideTunnel(GameLevel gameLevel, Ghost ghost) {
        final Arcade_LevelData data = levelData((gameLevel.number()));
        return data.pctGhostSpeedTunnel() * BASE_SPEED_1_PERCENT;
    }
}