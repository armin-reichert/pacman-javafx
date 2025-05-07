/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;

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

    @Override
    public Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends LevelCounter> T levelCounter() {
        return (T) levelCounter;
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public void init() {
        setInitialLives(3);
        mapSelector.loadAllMaps(this);
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
    }

    @Override
    public void prepareForNewGame() {
        playingProperty().set(false);
        livesProperty().set(initialLives());
        level = null;
        levelCounter().reset();
        scoreManager.loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
        huntingTimer.reset();
    }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        buildNormalLevel(1);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.GAME_STARTED);
    }

    @Override
    public boolean canStartNewGame() { return !THE_COIN_MECHANISM.isEmpty(); }

    @Override
    public boolean continueOnGameOver() { return false; }

    @Override
    public boolean isOver() {
        return livesProperty().get() == 0;
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
        level.ghost(RED_GHOST_ID).enableCruiseElroyMode(false);
        level.pac().die();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        THE_SIMULATION_STEP.killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        if (level.victims().size() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_EATEN_IN_LEVEL;
            scoreManager.scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number());
        }
    }

    // Food handling

    @Override
    protected void onPelletEaten(Vector2i tile) {
        scoreManager.scorePoints(PELLET_VALUE);
        level.pac().setRestingTicks(1);
        level.ghost(RED_GHOST_ID).updateCruiseElroyMode();
    }

    @Override
    protected void onEnergizerEaten(Vector2i tile) {
        scoreManager.scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        level.pac().setRestingTicks(3);
        Logger.info("Resting 3 ticks");
        level.ghost(RED_GHOST_ID).updateCruiseElroyMode();
        level.victims().clear();
        level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);
        long powerTicks = pacPowerTicks(level);
        if (powerTicks > 0) {
            level.huntingTimer().stop();
            Logger.info("Hunting stopped because Pac-Man got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / NUM_TICKS_PER_SEC);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            THE_SIMULATION_STEP.setPacGotPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_GETS_POWER);
        }
    }

    @Override
    public void onGameEnding() {
        playingProperty().set(false);
        if (!THE_COIN_MECHANISM.isEmpty()) {
            THE_COIN_MECHANISM.consumeCoin();
        }
        scoreManager.updateHighScore();
        level.showMessage(GameLevel.Message.GAME_OVER);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        level.setDemoLevel(false);
        scoreManager.setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        level.huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        createLevel(1);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
        scoreManager.setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(1));
        level.huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }

    @Override
    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.makeReadyForPlaying();
        initAnimationOfPacManAndGhosts();
        levelCounter().update(level);
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.Message.GAME_OVER);
            scoreManager.score().setEnabled(false);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
        } else {
            level.showMessage(GameLevel.Message.READY);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_STARTED);
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