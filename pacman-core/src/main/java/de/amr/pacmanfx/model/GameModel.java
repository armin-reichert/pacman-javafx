/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static de.amr.pacmanfx.Globals.theSimulationStep;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;

/**
 * Common base class of all Pac-Man game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    private final BooleanProperty cutScenesEnabledPy = new SimpleBooleanProperty(true);
    private final BooleanProperty playingPy = new SimpleBooleanProperty(false);

    protected GameLevel level;
    protected ScoreManager scoreManager;

    protected GameModel() {
        scoreManager = new DefaultScoreManager();
        scoreManager.score().pointsProperty().addListener(
                (py, ov, nv) -> scoreManager.onScoreChanged(this, ov.intValue(), nv.intValue()));
    }

    public abstract ActorSpeedControl actorSpeedControl();
    public abstract HuntingTimer huntingTimer();
    public abstract LevelCounter levelCounter();
    public abstract MapSelector mapSelector();

    public ScoreManager scoreManager() { return scoreManager; }
    public Optional<GateKeeper> gateKeeper() { return Optional.empty(); }
    public Optional<GameLevel> level() { return Optional.ofNullable(level); }

    public BooleanProperty cutScenesEnabledProperty() { return cutScenesEnabledPy; }
    public BooleanProperty playingProperty() { return playingPy; }
    public boolean isPlaying() { return playingProperty().get(); }

    // Game lifecycle

    public abstract void    init();
    public abstract void    resetEverything();
    public abstract void    prepareForNewGame();
    public abstract boolean canStartNewGame();
    public abstract void    startNewGame();
    public abstract void    createLevel(int levelNumber);
    public abstract void    buildNormalLevel(int levelNumber);
    public abstract void    buildDemoLevel();
    public abstract boolean isPacManSafeInDemoLevel();
    public abstract void    startLevel();
    public abstract void    startNextLevel();
    public abstract int     lastLevelNumber();
    public abstract boolean continueOnGameOver();

    public void startHunting() {
        level.pac().playAnimation();
        level.ghosts().forEach(Ghost::playAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(level.number());
        theGameEventManager().publishEvent(this, GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep() {
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.unlockGhosts(level));

        huntingTimer().update(level.number());
        level.blinking().tick();

        level.pac().update(level);
        level.ghosts().forEach(ghost -> ghost.update(level));
        level.bonus().ifPresent(bonus -> bonus.update(this));

        checkIfPacManKilled();
        if (hasPacManBeenKilled()) return;

        checkIfGhostsKilled();
        if (haveGhostsBeenKilled()) return;

        checkIfPacManFindsFood();
        updatePacPower();
        level.bonus().ifPresent(this::checkIfBonusEaten);
    }

    public boolean isLevelCompleted() { return level.uneatenFoodCount() == 0; }

    public void onLevelCompleted(GameLevel level) {
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

    public abstract long pacPowerTicks(GameLevel level);
    public abstract long pacPowerFadingTicks(GameLevel level);

    public void initAnimationOfPacManAndGhosts() {
        level.pac().selectAnimation(ANIM_ANY_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    private void checkIfPacManKilled() {
        level.ghosts(GhostState.HUNTING_PAC)
            .filter(ghost -> actorsCollide(ghost, level.pac()))
            .findFirst().ifPresent(potentialKiller -> {
                boolean killed;
                if (level.isDemoLevel()) {
                    killed = !isPacManSafeInDemoLevel();
                } else {
                    killed = !level.pac().isImmune();
                }
                if (killed) {
                    theSimulationStep().setPacKiller(potentialKiller);
                    theSimulationStep().setPacKilledTile(potentialKiller.tile());
                }
            });
    }

    private void updatePacPower() {
        final TickTimer powerTimer = level.pac().powerTimer();
        powerTimer.doTick();
        if (level.pac().isPowerFadingStarting(level)) {
            theSimulationStep().setPacStartsLosingPower();
            theGameEventManager().publishEvent(this, GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer restarted because Pac-Man lost power");
            level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            theSimulationStep().setPacLostPower();
            theGameEventManager().publishEvent(this, GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean hasPacManBeenKilled() { return theSimulationStep().pacKilledTile() != null; }
    public abstract void onPacKilled();

    public boolean haveGhostsBeenKilled() { return !theSimulationStep().killedGhosts().isEmpty(); }
    public abstract void onGhostKilled(Ghost ghost);

    protected void checkIfGhostsKilled() {
        level.ghosts(GhostState.FRIGHTENED).filter(ghost -> actorsCollide(ghost, level.pac())).forEach(this::onGhostKilled);
    }

    // Food handling

    protected abstract void checkIfPacManFindsFood();

    // Bonus handling

    public abstract boolean isBonusReached();
    public abstract void activateNextBonus();

    protected void checkIfBonusEaten(Bonus bonus) {
        if (bonus.state() != Bonus.STATE_EDIBLE) return;
        if (actorsCollide(level.pac(), bonus.actor())) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scoreManager.scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            theSimulationStep().setBonusEatenTile(bonus.actor().tile());
            theGameEventManager().publishEvent(this, GameEventType.BONUS_EATEN);
        }
    }

    // Life management

    private int initialLifeCount;
    private final IntegerProperty lifeCountPy = new SimpleIntegerProperty(0);

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    public int lifeCount() { return lifeCountPy.get(); }

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCountPy.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
    }

    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }
}