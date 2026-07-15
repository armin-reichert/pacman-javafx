/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends MovingActor {

    public static final byte REST_FOREVER = -1;

    private final TickTimer powerTimer = new TickTimer("Pac-PowerTimer");

    private final BooleanProperty dead = new SimpleBooleanProperty(false);

    private final BooleanProperty immune = new SimpleBooleanProperty(false);

    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    private long restingTicks;
    private long starvingTicks;

    private Steering automaticSteering;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Pac{" +
            "immune=" + isImmune() +
            ", autopilot=" + isUsingAutopilot() +
            ", dead=" + isDead() +
            ", restingTime=" + restingTicks +
            ", starvingTime=" + starvingTicks +
            ", visible=" + isVisible() +
            ", x=" + x() +
            ", y=" + y() +
            ", velocityX=" + velX() +
            ", velocityY=" + velY() +
            ", accelerationX=" + accX() +
            ", accelerationY=" + accY() +
            '}';
    }

    public void setAutomaticSteering(Steering steering) {
        automaticSteering = requireNonNull(steering);
    }

    @Override
    public boolean canTurnBack() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(GameLevel gameLevel, Vector2i tile) {
        requireNonNull(gameLevel);
        requireNonNull(tile);
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        // Portal tiles are the only tiles outside the world that can be accessed
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
            return false; // Schieb ab, Alter!
        }
        return !terrain.isTileBlocked(tile);
    }

    @Override
    public void reset() {
        super.reset();
        setDead(false);
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing
        animations.select(ArcadePacMan_AnimationID.PAC_MUNCHING);
    }

    public BooleanProperty deadProperty() {
        return dead;
    }

    public boolean isDead() {
        return dead.get();
    }

    public boolean isAlive() {
        return !isDead(); // Not sure if the opposite of being dead is being alive ;-)
    }

    public void setDead(boolean dead) {
        deadProperty().set(dead);
    }

    public BooleanProperty immuneProperty() {
        return immune;
    }

    public boolean isImmune() {
        return immune.get();
    }

    public void setImmune(boolean value) {
        immuneProperty().set(value);
    }

    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    public boolean isUsingAutopilot() {
        return usingAutopilot.get();
    }

    public void setUsingAutopilot(boolean value) {
        usingAutopilotProperty().set(value);
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public boolean isPowerFading(GameLevel gameLevel) {
        long fadingTicks = TickTimer.secToTicks(gameLevel.pacPowerFadingSeconds());
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerFadingStarting(GameLevel gameLevel) {
        long fadingTicks = TickTimer.secToTicks(gameLevel.pacPowerFadingSeconds());
        return powerTimer.isRunning() && powerTimer.remainingTicks() == fadingTicks
            || powerTimer.durationTicks() < fadingTicks && powerTimer.tickCount() == 1;
    }

    @Override
    public void init(GameLevel level) {}

    @Override
    public void update(GameLevel level, GameEventManager eventManager) {

        if (isDead() || restingTicks == REST_FOREVER) {
            return;
        }

        if (restingTicks > 0) {
            restingTicks -= 1;
            return;
        }

        if (isUsingAutopilot()) {
            automaticSteering.steer(this, level);
        }

        final ActorSpeedSettings speedControl = level.gameModel().rules().actorSpeedControl();
        setSpeed(powerTimer.isRunning() ? speedControl.pacSpeedWhenHasPower(level) : speedControl.pacSpeed(level));
        tryMovingOrTeleporting(level);

        if (moveInfo.moved) {
            animations.playSelected();
        } else {
            animations.stopSelected();
        }
    }

    /**
     * @return number of ticks Pac is resting
     */
    public long restingTicks() { return restingTicks; }

    /**
     * Sets the number of ticks Pac-Man is resting.
     *
     * @param ticks number of ticks
     */
    public void setRestingTicks(int ticks) {
        restingTicks = ticks;
    }

    /**
     *  @return number of ticks passed since a pellet or an energizer has been eaten.
     */
    public long starvingTicks() { return starvingTicks; }

    public void continueStarving() {
        ++starvingTicks;
    }

    public void endStarving() {
        starvingTicks = 0;
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isParalyzed() {
        return (velX() == 0 && velY() == 0)
            || !moveInfo.moved
            || restingTicks == REST_FOREVER;
    }
}