/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.steering.Steering;
import de.amr.pacmanfx.ui.PacManGamesEnv;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 */
public abstract class ArcadeAny_GameModel extends GameModel {

    protected static final byte PELLET_VALUE = 10;
    protected static final byte ENERGIZER_VALUE = 50;
    protected static final int POINTS_ALL_GHOSTS_EATEN_IN_LEVEL = 12_000;
    protected static final int EXTRA_LIFE_SCORE = 10_000;
    protected static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // points = value * 100

    protected MapSelector mapSelector;
    protected LevelCounter levelCounter;
    protected HuntingTimer huntingTimer;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;
    protected int cruiseElroy;

    @Override
    public Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public void init() {
        setInitialLifeCount(3);
        mapSelector.loadAllMaps(this);
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        levelCounter.clear();
    }

    @Override
    public void prepareForNewGame() {
        playingProperty().set(false);
        setLifeCount(initialLifeCount());
        level = null;
        scoreManager.loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
        huntingTimer.reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        levelCounter.clear();
        buildNormalLevel(1);
        theGameEventManager().publishEvent(this, GameEventType.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !theCoinMechanism().isEmpty(); }

    @Override
    public boolean continueOnGameOver() { return false; }

    @Override
    public boolean isOver() {
        return lifeCount() == 0;
    }

    // Actors

    @Override
    public long pacPowerTicks(GameLevel level) {
        return level != null ? 60 * level.data().pacPowerSeconds() : 0;
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        gateKeeper.resetCounterAndSetEnabled(true);
        activateCruiseElroyMode(false);
        level.pac().die();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        theSimulationStep().killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        level.registerGhostKilled();
        if (level.numGhostsKilled() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_EATEN_IN_LEVEL;
            scoreManager.scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number());
        }
    }

    public int cruiseElroy() {
        return cruiseElroy;
    }

    public boolean isCruiseElroyModeActive() { return cruiseElroy > 0; }

    public void updateCruiseElroyMode() {
        if (level.uneatenFoodCount() == level.data().elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (level.uneatenFoodCount() == level.data().elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
    }

    protected void activateCruiseElroyMode(boolean active) {
        int absValue = Math.abs(cruiseElroy);
        cruiseElroy = active ? absValue : -absValue;
    }

    // Food handling

    protected void checkIfPacManFindsFood() {
        Vector2i tile = level.pac().tile();
        if (level.hasFoodAt(tile)) {
            level.pac().starvingEnds();
            level.registerFoodEatenAt(tile);
            if (level.isEnergizerPosition(tile)) {
                onEnergizerEaten(tile);
            } else {
                onPelletEaten();
            }
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (isBonusReached()) {
                activateNextBonus();
                theSimulationStep().setBonusIndex(level.currentBonusIndex());
            }
            theGameEventManager().publishEvent(this, GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starvingContinues();
        }
    }

    public void onPelletEaten() {
        scoreManager.scorePoints(PELLET_VALUE);
        level.pac().setRestingTicks(1);
        updateCruiseElroyMode();
    }

    public void onEnergizerEaten(Vector2i tile) {
        theSimulationStep().setFoundEnergizerAtTile(tile);
        scoreManager.scorePoints(ENERGIZER_VALUE);
        level.pac().setRestingTicks(3);
        updateCruiseElroyMode();
        level.victims().clear();
        level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);
        long powerTicks = pacPowerTicks(level);
        if (powerTicks > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped because Pac-Man got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / NUM_TICKS_PER_SEC);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            theSimulationStep().setPacGotPower();
            theGameEventManager().publishEvent(this, GameEventType.PAC_GETS_POWER);
        }
    }

    @Override
    public void onGameEnding() {
        playingProperty().set(false);
        if (!theCoinMechanism().isEmpty()) {
            theCoinMechanism().consumeCoin();
        }
        scoreManager.updateHighScore();
        level.showMessage(LevelMessage.GAME_OVER);
        theGameEventManager().publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        level.setDemoLevel(false);
        level.pac().immuneProperty().bind(PacManGamesEnv.PY_IMMUNITY);
        level.pac().usingAutopilotProperty().bind(PacManGamesEnv.PY_USING_AUTOPILOT);
        scoreManager.setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        level.huntingTimer().reset();
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        createLevel(1);
        level.setDemoLevel(true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotAlgorithm(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter.setEnabled(true);
        scoreManager.setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(1));
        level.huntingTimer().reset();
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.makeReadyForPlaying();
        initAnimationOfPacManAndGhosts();
        if (level.isDemoLevel()) {
            level.showMessage(LevelMessage.GAME_OVER);
            scoreManager.score().setEnabled(false);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            levelCounter().update(level.number(), level.bonusSymbol(0));
            level.showMessage(LevelMessage.READY);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_STARTED);
    }

    @Override
    public void startNextLevel() {
        buildNormalLevel(level.number() + 1);
        startLevel();
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }
}