/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
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
            ", position=" + position() +
            ", velocity=" + velocity() +
            ", acceleration=" + acceleration() +
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
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().isTileInHouseArea(tile)) {
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
        selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
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
        long fadingTicks = secToTicks(gameLevel.pacPowerFadingSeconds());
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerFadingStarting(GameLevel gameLevel) {
        long fadingTicks = secToTicks(gameLevel.pacPowerFadingSeconds());
        return powerTimer.isRunning() && powerTimer.remainingTicks() == fadingTicks
            || powerTimer.durationTicks() < fadingTicks && powerTimer.tickCount() == 1;
    }

    @Override
    public void tick(Game game) {
        if (game.optGameLevel().isEmpty()) return;

        if (isDead() || restingTicks == REST_FOREVER) {
            return;
        }

        if (restingTicks > 0) {
            restingTicks -= 1;
            return;
        }

        if (isUsingAutopilot()) {
            automaticSteering.steer(this, game.level());
        }

        setSpeed(powerTimer.isRunning() ? game.pacSpeedWhenHasPower(game.level()) : game.pacSpeed(game.level()));
        moveThroughThisCruelWorld(game.level());

        if (moveInfo.moved) {
            optAnimationManager().ifPresent(AnimationManager::play);
        } else {
            stopAnimation();
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
        return velocity().equals(Vector2f.ZERO) || !moveInfo.moved || restingTicks == REST_FOREVER;
    }
}