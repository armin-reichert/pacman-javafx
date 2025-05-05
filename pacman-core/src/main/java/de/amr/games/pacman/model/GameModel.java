/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_EVENT_MANAGER;
import static de.amr.games.pacman.Globals.THE_SIMULATION_STEP;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;

/**
 * Common base class of all Pac-Man game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel implements ScoreManager {

    private final BooleanProperty cutScenesEnabledPy = new SimpleBooleanProperty(true);
    private final BooleanProperty playingPy = new SimpleBooleanProperty(false);

    protected GameLevel level;

    protected GameModel() {
        score.pointsProperty().addListener((py, ov, nv) -> onScoreChanged(ov.intValue(), nv.intValue()));
    }

    public abstract void    init();
    public abstract void    resetEverything();
    public abstract boolean continueOnGameOver();

    public abstract MapSelector mapSelector();
    public abstract HuntingTimer huntingTimer();
    protected Optional<GateKeeper> gateKeeper() { return Optional.empty(); }
    public abstract <T extends LevelCounter> T levelCounter();

    public Optional<GameLevel> level() { return Optional.ofNullable(level); }

    public BooleanProperty cutScenesEnabledProperty() { return cutScenesEnabledPy; }
    public BooleanProperty playingProperty() { return playingPy; }
    public boolean isPlaying() { return playingProperty().get(); }

    // Game lifecycle

    public abstract void prepareForNewGame();
    public abstract boolean canStartNewGame();
    public abstract void startNewGame();

    public abstract void createLevel(int levelNumber);
    public abstract void buildNormalLevel(int levelNumber);

    public abstract void buildDemoLevel();
    public abstract void assignDemoLevelBehavior(Pac pac);
    protected abstract boolean isPacManSafeInDemoLevel();

    public abstract void startLevel();
    public abstract void startNextLevel();
    public abstract int lastLevelNumber();

    public void startHunting() {
        level.pac().startAnimation();
        level.ghosts().forEach(Ghost::startAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(level.number());
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep() {
        huntingTimer().update(level.number());
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.unlockGhosts(level, THE_SIMULATION_STEP));
        level.blinking().tick();

        level.pac().update();
        level.ghosts().forEach(Ghost::update);
        level.bonus().ifPresent(bonus -> bonus.update(this));

        checkIfPacManKilled();
        if (hasPacManBeenKilled()) return;

        checkIfGhostsKilled();
        if (haveGhostsBeenKilled()) return;

        checkIfPacManFindsFood();
        updatePacPower();
        level.bonus().ifPresent(this::checkIfBonusEaten);
    }

    public void onLevelCompleted() {
        Logger.info("Level complete, stop hunting timer");
        huntingTimer().stop();
        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();
        level.pac().stopAndShowInFullBeauty();
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero");
        level.bonus().ifPresent(Bonus::setInactive);
        // when cheating to end level, there might still be food
        level.eatAllFood();
        Logger.trace("Game level {} completed.", level.number());
    }

    public boolean isLevelCompleted() { return level.uneatenFoodCount() == 0; }

    public abstract boolean isOver();
    public abstract void onGameEnding();

    // Actors

    /**
     * Checks actor collision based on same tile position.
     * @param either an actor
     * @param other another actor
     * @return if both actors have the same tile position
     */
    public boolean actorsCollide(Actor either, Actor other) {
        return either.sameTile(other);
    }

    public abstract ActorSpeedControl speedControl();
    public abstract long pacPowerTicks(GameLevel level);
    public abstract long pacPowerFadingTicks(GameLevel level);

    public void initAnimationOfPacManAndGhosts() {
        level.pac().selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    private void checkIfPacManKilled() {
        level.ghosts(HUNTING_PAC)
            .filter(ghost -> actorsCollide(ghost, level.pac()))
            .findFirst().ifPresent(potentialKiller -> {
                boolean killed;
                if (level.isDemoLevel()) {
                    killed = !isPacManSafeInDemoLevel();
                } else {
                    killed = !level.pac().isImmune();
                }
                if (killed) {
                    THE_SIMULATION_STEP.setPacKiller(potentialKiller);
                    THE_SIMULATION_STEP.setPacKilledTile(potentialKiller.tile());
                }
            });
    }

    private void updatePacPower() {
        final TickTimer timer = level.pac().powerTimer();
        timer.doTick();
        if (level.pac().isPowerFadingStarting()) {
            THE_SIMULATION_STEP.setPacStartsLosingPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (timer.hasExpired()) {
            timer.stop();
            timer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer restarted because Pac-Man lost power");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            THE_SIMULATION_STEP.setPacLostPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean hasPacManBeenKilled() { return THE_SIMULATION_STEP.pacKilledTile() != null; }
    public abstract void onPacKilled();

    public boolean haveGhostsBeenKilled() { return !THE_SIMULATION_STEP.killedGhosts().isEmpty(); }
    public abstract void onGhostKilled(Ghost ghost);

    protected void checkIfGhostsKilled() {
        level.ghosts(FRIGHTENED).filter(ghost -> actorsCollide(ghost, level.pac())).forEach(this::onGhostKilled);
    }

    // Food handling

    protected abstract void onEnergizerEaten(Vector2i tile);
    protected abstract void onPelletEaten(Vector2i tile);

    protected void checkIfPacManFindsFood() {
        Vector2i tile = level.pac().tile();
        if (level.hasFoodAt(tile)) {
            level.pac().endStarving();
            if (level.isEnergizerPosition(tile)) {
                THE_SIMULATION_STEP.setFoundEnergizerAtTile(tile);
                onEnergizerEaten(tile);
            } else {
                onPelletEaten(tile);
            }
            level.registerFoodEatenAt(tile);
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (isBonusReached()) {
                activateNextBonus();
                THE_SIMULATION_STEP.setBonusIndex(level.nextBonusIndex());
            }
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    // Bonus handling

    public abstract boolean isBonusReached();
    public abstract void activateNextBonus();

    protected void checkIfBonusEaten(Bonus bonus) {
        if (bonus.state() != Bonus.STATE_EDIBLE) return;
        if (actorsCollide(level.pac(), bonus.actor())) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            THE_SIMULATION_STEP.setBonusEatenTile(bonus.actor().tile());
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_EATEN);
        }
    }

    protected void handleExtraLifeScoreReached(int extraLifeScore) { addLives(1); }

    // Life management

    private final IntegerProperty initialLivesPy = new SimpleIntegerProperty(3);
    private final IntegerProperty livesPy = new SimpleIntegerProperty(0);

    public IntegerProperty initialLivesProperty() { return initialLivesPy; }
    public IntegerProperty livesProperty() { return livesPy; }

    public void loseLife() {
        int lives = livesProperty().get();
        if (lives > 0) {
            livesProperty().set(lives - 1);
        } else {
            Logger.error("Cannot lose life, no lives left");
        }
    }

    public void addLives(int lives) {
        livesProperty().set(livesProperty().get() + lives);
    }

    // Score management

    private final Score score = new Score();
    private final Score highScore = new Score();
    protected File highScoreFile;
    protected List<Integer> extraLifeScores = List.of();

    private boolean scoreVisible;

    @Override
    public boolean isScoreVisible() { return scoreVisible; }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void scorePoints(int points) {
        if (!score.isEnabled()) {
            return;
        }
        int oldScore = score.points(), newScore = oldScore + points;
        if (highScore().isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(score.levelNumber());
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    protected void onScoreChanged(int oldScore, int newScore) {
        for (int extraLifeScore : extraLifeScores) {
            // has extra life score been crossed?
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                THE_SIMULATION_STEP.setExtraLifeWon();
                THE_SIMULATION_STEP.setExtraLifeScore(extraLifeScore);
                handleExtraLifeScoreReached(extraLifeScore);
                GameEvent event = new GameEvent(this, GameEventType.SPECIAL_SCORE_REACHED);
                event.setPayload("score", extraLifeScore); // just for testing payload implementation
                THE_GAME_EVENT_MANAGER.publishEvent(event);
                break;
            }
        }
    }

    @Override
    public void loadHighScore() {
        highScore.read(highScoreFile);
        Logger.info("High Score loaded from '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
    }

    @Override
    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.read(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            highScore.save(highScoreFile, "High Score, last update %s".formatted(LocalTime.now()));
        }
    }

    @Override
    public void resetScore() {
        score.reset();
    }

    @Override
    public void saveHighScore() {
        new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public void setScoreLevelNumber(int levelNumber) {
        score.setLevelNumber(levelNumber);
    }
}