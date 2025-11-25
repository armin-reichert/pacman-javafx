/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.actors.Blinky;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    // Level data as given in the "Pac-Man Dossier"
    protected static final Arcade_LevelData[] LEVEL_DATA = {
        /* 1*/ new Arcade_LevelData( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
        /* 2*/ new Arcade_LevelData( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
        /* 3*/ new Arcade_LevelData( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
        /* 4*/ new Arcade_LevelData( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
        /* 5*/ new Arcade_LevelData(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
        /* 6*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
        /* 7*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 8*/ new Arcade_LevelData(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 9*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
        /*10*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
        /*11*/ new Arcade_LevelData(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
        /*12*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*13*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*14*/ new Arcade_LevelData(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
        /*15*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*16*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*17*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
        /*18*/ new Arcade_LevelData(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*19*/ new Arcade_LevelData(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*20*/ new Arcade_LevelData(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*21*/ new Arcade_LevelData( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
    };

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = Vector2i.of(10, 15);

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;
    public static final int ALL_GHOSTS_IN_LEVEL_KILLED_POINTS = 12_000;
    public static final int EXTRA_LIFE_SCORE = 10_000;
    public static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    private static final float BONUS_EATEN_SECONDS = 2;

    protected final CoinMechanism coinMechanism;

    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;

    protected Arcade_GameModel(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);
        setCollisionStrategy(CollisionStrategy.SAME_TILE);

        stateMachine = new GameStateMachine(this);
        List<FsmState<GameContext>> states = new ArrayList<>(List.of(GameState.values()));
        states.add(new LevelShortTestState());
        states.add(new LevelMediumTestState());
        states.add(new CutScenesTestState());
        stateMachine.setStates(states);
        stateMachine.setContext(THE_GAME_BOX);
        stateMachine.setName("Arcade Game State Machine");
    }

    public abstract Arcade_LevelData levelData(GameLevel gameLevel);

    public void onPelletEaten(GameLevel gameLevel) {
        scoreManager.scorePoints(PELLET_VALUE);
        gameLevel.pac().onFoodEaten(false);
        gameLevel.ghosts().forEach(ghost -> ghost.onFoodCountChange(gameLevel));
    }

    public void onEnergizerEaten(GameLevel gameLevel, Vector2i tile) {
        thisStep.foundEnergizerAtTile = tile;
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
        double powerSeconds = pacPowerSeconds(gameLevel);
        if (powerSeconds > 0) {
            gameLevel.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            gameLevel.pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            gameLevel.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            thisStep.pacGotPower = true;
            publishGameEvent(GameEvent.Type.PAC_GETS_POWER);
        }
    }

    // GameEvents interface

    @Override
    public void updateHunting(GameLevel gameLevel) {
        makeHuntingStep(gameLevel);
        if (gateKeeper != null) {
            gateKeeper.unlockGhosts(gameLevel);
        }
    }

    @Override
    public void onPacKilled(GameLevel gameLevel) {
        gateKeeper.resetCounterAndSetEnabled(true);
        gameLevel.huntingTimer().stop();
        gameLevel.pac().onKilled();
        gameLevel.ghosts().forEach(ghost -> ghost.onPacKilled(gameLevel));
    }

    @Override
    public void onGhostKilled(GameLevel gameLevel, Ghost ghost) {
        int killedSoFar = gameLevel.energizerVictims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        gameLevel.energizerVictims().add(ghost);
        ghost.setState(GhostState.EATEN);
        ghost.selectAnimationAt(CommonAnimationID.ANIM_GHOST_NUMBER, killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        gameLevel.registerGhostKilled();
        if (gameLevel.numGhostsKilled() == 16) {
            scoreManager.scorePoints(ALL_GHOSTS_IN_LEVEL_KILLED_POINTS);
            Logger.info("Scored {} points for killing all ghosts in level {}", ALL_GHOSTS_IN_LEVEL_KILLED_POINTS, gameLevel.number());
        }
    }

    @Override
    public void onGameEnding(GameLevel gameLevel) {
        setPlaying(false);
        if (!coinMechanism.noCoin()) {
            coinMechanism.consumeCoin();
        }
        scoreManager.updateHighScore();
        showMessage(gameLevel, MessageType.GAME_OVER);
        Logger.info("Game ended with level number {}", gameLevel.number());
    }

    // GameLifecycle interface

    @Override
    public void init() {
        setInitialLifeCount(3);
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        levelCounter().clear();
        hud().all(false);
    }

    @Override
    public void prepareForNewGame() {
        setPlaying(false);
        setLifeCount(initialLifeCount());
        setGameLevel(null);
        scoreManager.loadHighScore();
        scoreManager.score().reset();
        scoreManager.highScore().setEnabled(true);
        gateKeeper.reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        levelCounter().clear();
        buildNormalLevel(1);
        publishGameEvent(GameEvent.Type.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !coinMechanism.noCoin(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    public double pacPowerSeconds(GameLevel level) {
        return levelData(level).pacPowerSeconds();
    }

    @Override
    public double pacPowerFadingSeconds(GameLevel level) {
        return levelData(level).numFlashes() * 0.5;
    }

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
            if (isBonusReached(gameLevel)) {
                activateNextBonus(gameLevel);
                thisStep.bonusIndex = gameLevel.currentBonusIndex();
            }
            publishGameEvent(GameEvent.Type.PAC_FOUND_FOOD, tile);
        } else {
            pac.starve();
        }
    }

    @Override
    protected void checkPacFindsBonus(GameLevel gameLevel) {
        gameLevel.bonus().filter(bonus -> bonus.state() == BonusState.EDIBLE).ifPresent(bonus -> {
            if (actorsCollide(gameLevel.pac(), bonus)) {
                bonus.setEaten(BONUS_EATEN_SECONDS);
                scoreManager.scorePoints(bonus.points());
                Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
                thisStep.bonusEatenTile = bonus.tile();
                publishGameEvent(GameEvent.Type.BONUS_EATEN);
            }
        });
    }

    @Override
    public Optional<Integer> optCutSceneNumber(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> Optional.of(1); // after level #2, play cut scene #1
            case 5 -> Optional.of(2);
            case 9, 13, 17 -> Optional.of(3);
            default -> Optional.empty();
        };
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        final GameLevel normalLevel = createLevel(levelNumber, false);
        levelCounter().setEnabled(true);
        scoreManager.score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        //TODO handle case when no house exists
        normalLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));
        setGameLevel(normalLevel);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        int levelNumber = 1;
        final GameLevel demoLevel = createLevel(levelNumber, true);
        demoLevel.pac().setImmune(false);
        demoLevel.pac().setUsingAutopilot(true);
        demoLevel.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter().setEnabled(true);
        scoreManager.score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        //TODO handle case when no house exists
        demoLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> gateKeeper.setHouse(house));
        setGameLevel(demoLevel);
        publishGameEvent(GameEvent.Type.LEVEL_CREATED);
    }

    @Override
    public void startLevel(GameLevel gameLevel) {
        gameLevel.setStartTimeMillis(System.currentTimeMillis());
        gameLevel.getReadyToPlay();
        resetPacManAndGhostAnimations(gameLevel);
        if (gameLevel.isDemoLevel()) {
            showMessage(gameLevel, MessageType.GAME_OVER);
            scoreManager.score().setEnabled(false);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", gameLevel.number());
        } else {
            levelCounter().update(gameLevel.number(), gameLevel.bonusSymbol(0));
            showMessage(gameLevel, MessageType.READY);
            scoreManager.score().setEnabled(true);
            //scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", gameLevel.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        publishGameEvent(GameEvent.Type.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(gameLevel().number() + 1);
        startLevel(gameLevel());
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int numFlashes(GameLevel gameLevel) {
        return levelData(gameLevel).numFlashes();
    }

    @Override
    public void showMessage(GameLevel gameLevel, MessageType type) {
        requireNonNull(type);
        GameLevelMessage message = new GameLevelMessage(type);
        message.setPosition(gameLevel.worldMap().terrainLayer().messageCenterPosition());
        gameLevel.setMessage(message);
    }

    @Override
    protected void resetPacManAndGhostAnimations(GameLevel gameLevel) {
        gameLevel.pac().optAnimationManager().ifPresent(am -> {
            am.select(CommonAnimationID.ANIM_PAC_MUNCHING);
            am.reset();
        });
        gameLevel.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(am -> {
            am.select(CommonAnimationID.ANIM_GHOST_NORMAL);
            am.reset();
        }));
    }

    // ActorSpeedControl interface

    /** Base speed is 75 px per second (=1.25 px/tick). */
    public static final float BASE_SPEED = 1.25f;
    public static final float BASE_SPEED_1_PERCENT = 0.0125f;

    @Override
    public float bonusSpeed(GameLevel gameLevel) {
        //TODO clarify exact speed
        return 0.5f * pacSpeed(gameLevel);
    }

    @Override
    public float pacSpeed(GameLevel gameLevel) {
        byte percentage = levelData(gameLevel).pacSpeedPct();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacSpeedWhenHasPower(GameLevel gameLevel) {
        byte percentage = levelData(gameLevel).pacSpeedPoweredPct();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacSpeed(gameLevel);
    }

    @Override
    public float ghostSpeedWhenAttacking(GameLevel gameLevel, Ghost ghost) {
        if (gameLevel.worldMap().terrainLayer().isTunnel(ghost.tile())) {
            return ghostSpeedInsideTunnel(gameLevel, ghost);
        }
        if (ghost instanceof Blinky blinky) {
            if (blinky.cruiseElroyValue() == 1) {
                return levelData(gameLevel).elroy1SpeedPct() * BASE_SPEED_1_PERCENT;
            }
            if (blinky.cruiseElroyValue() == 2) {
                return levelData(gameLevel).elroy2SpeedPct() * BASE_SPEED_1_PERCENT;
            }
        }
        return levelData(gameLevel).ghostSpeedPct() * BASE_SPEED_1_PERCENT;
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
        float percentage = levelData(gameLevel).ghostSpeedFrightenedPct();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostSpeedInsideTunnel(GameLevel gameLevel, Ghost ghost) {
        return levelData(gameLevel).ghostSpeedTunnelPct() * BASE_SPEED_1_PERCENT;
    }
}