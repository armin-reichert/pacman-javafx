/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class AbstractGameModel implements Game {

    protected final BooleanProperty playing = new SimpleBooleanProperty(false);
    protected final IntegerProperty lifeCount = new SimpleIntegerProperty(0);
    protected final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);
    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    protected final SimulationStep simulationStep = new SimulationStep();

    protected final GameContext gameContext;
    protected GameLevel level;

    protected AbstractGameModel(GameContext gameContext) {
        this.gameContext = requireNonNull(gameContext);
    }

    // Game interface

    @Override
    public SimulationStep simulationStep() {
        return simulationStep;
    }

    @Override
    public void resetPacManAndGhostAnimations() {
        level.pac().selectAnimation(ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    @Override
    public int lifeCount() {
        return lifeCount.get();
    }

    @Override
    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }

    @Override
    public boolean isPlaying() { return playingProperty().get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    @Override
    public void startHunting() {
        level.pac().playAnimation();
        level.ghosts().forEach(Ghost::playAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer().startFirstHuntingPhase(level.number());
        gameContext.eventManager().publishEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    @Override
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

    @Override
    public boolean hasPacManBeenKilled() { return simulationStep.pacKilledTile != null; }

    @Override
    public boolean haveGhostsBeenKilled() { return !simulationStep.killedGhosts.isEmpty(); }

    @Override
    public boolean areCutScenesEnabled() { return cutScenesEnabled.get(); }

    @Override
    public void setCutScenesEnabled(boolean enabled) { cutScenesEnabled.set(enabled); }

    @Override
    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    @Override
    public void setInitialLifeCount(int lifeCount) {
        initialLifeCount.set(lifeCount);
    }

    @Override
    public boolean isLevelCompleted() { return level.uneatenFoodCount() == 0; }

    @Override
    public void onLevelCompleted() {
        Logger.info("Level completed, stop hunting timer");
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

    public BooleanProperty playingProperty() { return playing; }

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCount.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
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
            gameContext.eventManager().publishEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer().start();
            Logger.info("Hunting timer restarted because Pac-Man lost power");
            level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            simulationStep.pacLostPower = true;
            gameContext.eventManager().publishEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    protected void checkIfGhostsKilled() {
        level.ghosts(GhostState.FRIGHTENED).filter(ghost -> actorsCollide(ghost, level.pac())).forEach(this::onGhostKilled);
    }

    // Food handling

    protected abstract void checkIfPacManFindsFood(GameContext gameContext);

    // Bonus handling

    protected void checkIfPacManCanEatBonus(Bonus bonus) {
        if (bonus.state() == BonusState.EDIBLE && actorsCollide(level.pac(), bonus)) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scoreManager().scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            simulationStep.bonusEatenTile = bonus.tile();
            gameContext.eventManager().publishEvent(GameEventType.BONUS_EATEN);
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
}