/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GateKeeper;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NUMBER;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static java.util.Objects.requireNonNull;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;
    public static final int ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS = 12_000;
    public static final int EXTRA_LIFE_SCORE = 10_000;
    public static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    protected final GameContext gameContext;
    protected GameLevel gameLevel;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;
    protected int cruiseElroy;

    protected Arcade_GameModel(GameContext gameContext) {
        this.gameContext = requireNonNull(gameContext);
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(gameLevel);
    }

    // GameEvents interface

    @Override
    public void onPelletEaten() {
        scoreManager().scorePoints(PELLET_VALUE);
        gameLevel.pac().setRestingTicks(1);
        updateCruiseElroyMode();
    }

    @Override
    public void onEnergizerEaten(Vector2i tile) {
        simulationStep.foundEnergizerAtTile = tile;
        scoreManager().scorePoints(ENERGIZER_VALUE);
        gameLevel.pac().setRestingTicks(3);
        updateCruiseElroyMode();

        gameLevel.victims().clear();
        gameLevel.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);

        double powerSeconds = pacPowerSeconds(gameLevel);
        if (powerSeconds > 0) {
            huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            long ticks = TickTimer.secToTicks(powerSeconds);
            gameLevel.pac().powerTimer().restartTicks(ticks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", ticks, powerSeconds);
            gameLevel.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            simulationStep.pacGotPower = true;
            eventManager().publishEvent(GameEventType.PAC_GETS_POWER);
        }
    }

    @Override
    public void onPacKilled() {
        gateKeeper.resetCounterAndSetEnabled(true);
        huntingTimer().stop();
        activateCruiseElroyMode(false);
        gameLevel.pac().powerTimer().stop();
        gameLevel.pac().powerTimer().reset(0);
        gameLevel.pac().sayGoodbyeCruelWorld();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        simulationStep.killedGhosts.add(ghost);
        int killedSoFar = gameLevel.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        gameLevel.victims().add(ghost);
        ghost.setState(GhostState.EATEN);
        ghost.selectAnimationFrame(ANIM_GHOST_NUMBER, killedSoFar);
        scoreManager().scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        gameLevel.registerGhostKilled();
        if (gameLevel.numGhostsKilled() == 16) {
            scoreManager().scorePoints(ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS);
            Logger.info("Scored {} points for killing all ghosts in level {}",
                    ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS, gameLevel.number());
        }
    }

    @Override
    public void onGameEnding() {
        setPlaying(false);
        if (!gameContext.coinMechanism().isEmpty()) {
            gameContext.coinMechanism().consumeCoin();
        }
        scoreManager().updateHighScore();
        showMessage(gameLevel, MessageType.GAME_OVER);
    }

    // GameLifecycle interface

    @Override
    public void init() {
        setInitialLifeCount(3);
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        hudData().theLevelCounter().clear();
    }

    @Override
    public void prepareForNewGame() {
        setPlaying(false);
        setLifeCount(initialLifeCount());
        gameLevel = null;
        scoreManager().loadHighScore();
        scoreManager().resetScore();
        gateKeeper.reset();
        huntingTimer().reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        hudData().theLevelCounter().clear();
        buildNormalLevel(1);
        eventManager().publishEvent(GameEventType.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !gameContext.coinMechanism().isEmpty(); }

    @Override
    public boolean canContinueOnGameOver() { return false; }

    @Override
    public Optional<GateKeeper> optGateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public double pacPowerSeconds(GameLevel level) {
        return level.data().pacPowerSeconds();
    }

    @Override
    public double pacPowerFadingSeconds(GameLevel level) {
        return level.data().numFlashes() * 0.5;
    }

    @Override
    protected void checkIfPacManFindsFood() {
        Vector2i tile = gameLevel.pac().tile();
        if (gameLevel.tileContainsFood(tile)) {
            gameLevel.pac().starvingIsOver();
            gameLevel.registerFoodEatenAt(tile);
            if (gameLevel.isEnergizerPosition(tile)) {
                onEnergizerEaten(tile);
            } else {
                onPelletEaten();
            }
            gateKeeper.registerFoodEaten(gameLevel);
            if (isBonusReached()) {
                activateNextBonus();
                simulationStep.bonusIndex = gameLevel.currentBonusIndex();
            }
            eventManager().publishEvent(GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            gameLevel.pac().starve();
        }
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        gameLevel.setDemoLevel(false);
        gameLevel.pac().immuneProperty().bind(gameContext.gameController().propertyImmunity());
        gameLevel.pac().usingAutopilotProperty().bind(gameContext.gameController().propertyUsingAutopilot());
        hudData().theLevelCounter().setEnabled(true);
        huntingTimer().reset();
        scoreManager().setGameLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        gameLevel.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        int levelNumber = 1;
        createLevel(levelNumber);
        gameLevel.setDemoLevel(true);
        gameLevel.pac().setImmune(false);
        gameLevel.pac().setUsingAutopilot(true);
        gameLevel.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        hudData().theLevelCounter().setEnabled(true);
        huntingTimer().reset();
        scoreManager().setGameLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        gameLevel.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void startLevel() {
        gameLevel.setStartTime(System.currentTimeMillis());
        gameLevel.getReadyToPlay();
        resetPacManAndGhostAnimations();
        if (gameLevel.isDemoLevel()) {
            showMessage(gameLevel, MessageType.GAME_OVER);
            scoreManager().score().setEnabled(false);
            scoreManager().highScore().setEnabled(false);
            Logger.info("Demo level {} started", gameLevel.number());
        } else {
            hudData().theLevelCounter().update(gameLevel.number(), gameLevel.bonusSymbol(0));
            showMessage(gameLevel, MessageType.READY);
            scoreManager().score().setEnabled(true);
            scoreManager().highScore().setEnabled(true);
            Logger.info("Level {} started", gameLevel.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager().publishEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(gameLevel.number() + 1);
        startLevel();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }

    /**
     * @return "Cruise Elroy" state (changes behavior of red ghost).
     * <p>0=off, 1=Elroy1, 2=Elroy2, -1=Elroy1 (disabled), -2=Elroy2 (disabled).</p>
     */
    public int cruiseElroy() { return cruiseElroy; }

    public boolean isCruiseElroyModeActive() { return cruiseElroy > 0; }

    protected void activateCruiseElroyMode(boolean active) {
        int absValue = Math.abs(cruiseElroy);
        cruiseElroy = active ? absValue : -absValue;
    }

    private void updateCruiseElroyMode() {
        if (gameLevel.uneatenFoodCount() == gameLevel.data().elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (gameLevel.uneatenFoodCount() == gameLevel.data().elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
    }
}