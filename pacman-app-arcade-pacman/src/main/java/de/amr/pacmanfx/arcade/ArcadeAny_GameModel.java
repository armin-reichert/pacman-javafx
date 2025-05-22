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

    public static final byte PELLET_VALUE = 10;
    public static final byte ENERGIZER_VALUE = 50;
    public static final int ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS = 12_000;
    public static final int EXTRA_LIFE_SCORE = 10_000;
    public static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    protected MapSelector mapSelector;
    protected LevelCounter levelCounter;
    protected HuntingTimer huntingTimer;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;
    protected int cruiseElroy;

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

    // Components

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
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        level.registerGhostKilled();
        if (level.numGhostsKilled() == 16) {
            scoreManager.scorePoints(ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS);
            Logger.info("Scored {} points for killing all ghosts in level {}",
                ALL_GHOSTS_IN_LEVEL_KILLED_BONUS_POINTS, level.number());
        }
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

    private void updateCruiseElroyMode() {
        if (level.uneatenFoodCount() == level.data().elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (level.uneatenFoodCount() == level.data().elroy2DotsLeft()) {
            cruiseElroy = 2;
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
            Logger.debug("Hunting stopped (Pac-Man got power)");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, (float) powerTicks / NUM_TICKS_PER_SEC);
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
        gateKeeper.setLevelNumber(levelNumber);
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
        gateKeeper.setLevelNumber(1);
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