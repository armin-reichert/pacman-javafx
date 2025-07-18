/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class GameModel implements ScoreManager {

    private final BooleanProperty playingProperty = new SimpleBooleanProperty(false);
    private final IntegerProperty lifeCountProperty = new SimpleIntegerProperty(0);

    protected GameLevel level;
    protected boolean cutScenesEnabled;

    private final Score score = new Score();
    private final Score highScore = new Score();
    private Set<Integer> extraLifeScores = Set.of();
    private int initialLifeCount;

    protected final File highScoreFile;
    protected final GameEventManager gameEventManager;
    protected final SimulationStep simulationStep = new SimulationStep();

    protected GameModel(GameEventManager gameEventManager, File highScoreFile) {
        this.gameEventManager = requireNonNull(gameEventManager);
        this.highScoreFile = requireNonNull(highScoreFile);
        cutScenesEnabled = true;
        score.pointsProperty().addListener((py, ov, nv) -> onScoreChanged(this, ov.intValue(), nv.intValue()));
    }

    public SimulationStep simulationStep() {
        return simulationStep;
    }

    public abstract ActorSpeedControl actorSpeedControl();
    public abstract HuntingTimer huntingTimer();
    public abstract MapSelector mapSelector();
    public abstract OptionalInt cutSceneNumber(int levelNumber);

    public abstract HUD theHUD();
    public Optional<GateKeeper> gateKeeper() { return Optional.empty(); }
    public Optional<GameLevel> level() { return Optional.ofNullable(level); }

    public BooleanProperty playingProperty() { return playingProperty; }
    public boolean isPlaying() { return playingProperty().get(); }

    public boolean areCutScenesEnabled() { return cutScenesEnabled; }
    public void setCutScenesEnabled(boolean enabled) { cutScenesEnabled = enabled; }

    // Game lifecycle

    public abstract void    init();
    public abstract void    resetEverything();
    public abstract void    prepareForNewGame();
    public abstract boolean canStartNewGame();
    public abstract void    startNewGame(GameContext gameContext);
    public abstract void    createLevel(GameContext gameContext, int levelNumber);
    public abstract void    buildNormalLevel(GameContext gameContext, int levelNumber);
    public abstract void    buildDemoLevel(GameContext gameContext);
    public abstract boolean isPacManSafeInDemoLevel();
    public abstract void    startLevel();
    public abstract void    startNextLevel(GameContext gameContext);
    public abstract int     lastLevelNumber();
    public abstract boolean continueOnGameOver();

    public void startHunting() {
        level.pac().playAnimation();
        level.ghosts().forEach(Ghost::playAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(level.number());
        gameEventManager.publishEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep(GameContext gameContext) {
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.unlockGhosts(level));

        huntingTimer().update(level.number());
        level.blinking().tick();

        level.pac().tick();
        level.ghosts().forEach(Ghost::tick);
        level.bonus().ifPresent(Bonus::tick);

        checkIfPacManGetsKilled(level.pac());
        if (hasPacManBeenKilled()) return;

        checkIfGhostsKilled();
        if (haveGhostsBeenKilled()) return;

        checkIfPacManFindsFood(gameContext);
        updatePacPower();
        level.bonus().ifPresent(this::checkIfPacManCanEatBonus);
    }

    public boolean isLevelCompleted() { return level.uneatenFoodCount() == 0; }

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

    public abstract void onGameEnding();

    // Life count management

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    public int lifeCount() { return lifeCountProperty.get(); }

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCountProperty.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
    }

    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }

    // Actors

    /**
     * Checks actor collision. Default implementation uses tile equality.
     *
     * @param either an actor
     * @param other another actor
     * @return if both actors collide
     */
    public boolean actorsCollide(Actor either, Actor other) {
        return either.atSameTileAs(other);
    }

    public abstract long pacPowerTicks(GameLevel level);
    public abstract long pacPowerFadingTicks(GameLevel level);

    public void initAnimationOfPacManAndGhosts() {
        level.pac().selectAnimation(ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    protected void checkIfPacManGetsKilled(Pac pac) {
        level.ghosts(GhostState.HUNTING_PAC)
            .filter(ghost -> actorsCollide(ghost, pac))
            .findFirst().ifPresent(potentialKiller -> {
                boolean killed;
                if (level.isDemoLevel()) {
                    killed = !isPacManSafeInDemoLevel();
                } else {
                    killed = !pac.isImmune();
                }
                if (killed) {
                    simulationStep.pacKiller = potentialKiller;
                    simulationStep.pacKilledTile = potentialKiller.tile();
                }
            });
    }

    protected void updatePacPower() {
        final TickTimer powerTimer = level.pac().powerTimer();
        powerTimer.doTick();
        if (level.pac().isPowerFadingStarting()) {
            simulationStep.pacStartsLosingPower = true;
            gameEventManager.publishEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer restarted because Pac-Man lost power");
            level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            simulationStep.pacLostPower = true;
            gameEventManager.publishEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean hasPacManBeenKilled() { return simulationStep.pacKilledTile != null; }
    public abstract void onPacKilled();

    public boolean haveGhostsBeenKilled() { return !simulationStep.killedGhosts.isEmpty(); }
    public abstract void onGhostKilled(Ghost ghost);

    protected void checkIfGhostsKilled() {
        level.ghosts(GhostState.FRIGHTENED).filter(ghost -> actorsCollide(ghost, level.pac())).forEach(this::onGhostKilled);
    }

    // Food handling

    protected abstract void checkIfPacManFindsFood(GameContext gameContext);

    // Bonus handling

    public abstract boolean isBonusReached();
    public abstract void activateNextBonus(GameContext gameContext);

    protected void checkIfPacManCanEatBonus(Bonus bonus) {
        if (bonus.state() == BonusState.EDIBLE && actorsCollide(level.pac(), bonus)) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            simulationStep.bonusEatenTile = bonus.tile();
            gameEventManager.publishEvent(GameEventType.BONUS_EATEN);
        }
    }

    // Generic property store
    // TODO remove, is only used by cutscene tests

    private Map<String, Object> propertyMap;

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) propertyMap().get(key);
    }

    public void setProperty(String key, Object value) {
        propertyMap().put(key, value);
    }

    protected Map<String, Object> propertyMap() {
        if (propertyMap == null) {
            propertyMap = new HashMap<>(4);
        }
        return propertyMap;
    }

    // ScoreManager implementation

    @Override
    public void setExtraLifeScores(Set<Integer> scores) {
        extraLifeScores = new HashSet<>(scores);
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

    @Override
    public void onScoreChanged(GameModel game, int oldScore, int newScore) {
        for (int extraLifeScore : extraLifeScores) {
            // has extra life score been crossed?
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                simulationStep.extraLifeWon = true;
                simulationStep.extraLifeScore = extraLifeScore;
                addLives(1);
                GameEvent event = new GameEvent(game, GameEventType.SPECIAL_SCORE_REACHED);
                event.setPayload("score", extraLifeScore); // just for testing payload implementation
                gameEventManager.publishEvent(event);
                break;
            }
        }
    }

    @Override
    public void loadHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        try {
            highScore.read(highScoreFile);
            Logger.info("High Score loaded from file '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
        } catch (IOException x) {
            Logger.error("High Score file could not be opened: '{}'", highScoreFile);
        }
    }

    @Override
    public void updateHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        var oldHighScore = Score.fromFile(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            try {
                highScore.save(highScoreFile, "High Score updated at %s".formatted(LocalTime.now()));
            } catch (IOException x) {
                Logger.error("High Score file could not be saved: '{}'", highScoreFile);
            }
        }
    }

    @Override
    public void resetScore() {
        score.reset();
    }

    @Override
    public void saveHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        try {
            new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
        } catch (IOException x) {
            Logger.error("High Score could not be saved to file '{}'", highScoreFile);
            Logger.error(x);
        }
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