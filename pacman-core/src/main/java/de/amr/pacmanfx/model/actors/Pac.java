/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static java.util.Objects.requireNonNull;

/**
 * Pac-Man / Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class Pac extends Creature {

    public static final byte REST_INDEFINITELY = -1;

    private final String name;
    private boolean dead;
    private int restingTicks;
    private long starvingTicks;
    private Steering autopilotAlgorithm;

    private final BooleanProperty immunePy = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilotPy = new SimpleBooleanProperty(false);

    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    public Pac(String name) {
        this.name = requireNonNull(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Pac{" +
            "immune=" + immunePy.get() +
            ", dead=" + dead +
            ", restingTicks=" + restingTicks +
            ", starvingTicks=" + starvingTicks +
            ", visible=" + visible +
            ", pos_x=" + posX +
            ", pos_y=" + posY +
            ", vel_x=" + velX +
            ", vel_y=" + velY +
            ", acc_x=" + accX +
            ", acc_y=" + accY +
            '}';
    }

    @Override
    public boolean canReverse() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        if (level.isCoveredByHouse(tile)) {
            return false;
        }
        if (level.isInsideWorld(tile)) {
            return !level.isBlockedTile(tile);
        }
        return level.isPortalAt(tile);
    }

    @Override
    public void reset() {
        super.reset();
        dead = false;
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedUp = 1.5f; // no real cornering implementation but better than nothing
        selectAnimation(ANIM_ANY_PAC_MUNCHING);
    }

    public boolean isImmune() {
        return immunePy.get();
    }

    public void setImmune(boolean immune) {
        immunePy.set(immune);
    }

    public BooleanProperty immuneProperty() { return immunePy; }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= theGame().pacPowerFadingTicks(level);
    }

    public boolean isPowerFadingStarting() {
        return powerTimer.isRunning() && powerTimer.remainingTicks() == theGame().pacPowerFadingTicks(level)
            || powerTimer.durationTicks() < theGame().pacPowerFadingTicks(level) && powerTimer.tickCount() == 1;
    }

    public void update() {
        if (dead || restingTicks == REST_INDEFINITELY) {
            return;
        }
        if (restingTicks == 0) {
            if (isUsingAutopilot()) {
                autopilotAlgorithm.steer(this, level);
            }
            setSpeed(powerTimer.isRunning()
                ? level.speedControl().pacPowerSpeed(level)
                : level.speedControl().pacNormalSpeed(level));
            tryMoving();
            if (moveInfo.moved) {
                startAnimation();
            } else {
                stopAnimation();
            }
        } else {
            --restingTicks;
        }
    }

    /**
     * Pac-Man is displayed in its full beauty and does not move anymore.
     */
    public void stopAndShowInFullBeauty() {
        setSpeed(0);
        setRestingTicks(Pac.REST_INDEFINITELY);
        stopAnimation();
        selectAnimation(ANIM_ANY_PAC_MUNCHING);
        resetAnimation();
    }

    public void die() {
        setSpeed(0);
        stopAnimation();
        dead = true;
    }

    public boolean isAlive() {
        return !dead;
    }

    public void setRestingTicks(int ticks) {
        if (ticks == REST_INDEFINITELY || ticks >= 0) {
            restingTicks = ticks;
        } else {
            throw new IllegalArgumentException("Resting time cannot be negative, but is: " + ticks);
        }
    }

    /**
     *  @return number of ticks passed since pellet or energizer was eaten.
     */
    public long starvingTicks() {
        return starvingTicks;
    }

    public void starvingContinues() {
        ++starvingTicks;
    }

    public void starvingEnds() { starvingTicks = 0; }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isStandingStill() {
        return velocity().length() == 0 || !moveInfo.moved || restingTicks == REST_INDEFINITELY;
    }

    public void setAutopilotAlgorithm(Steering steering) {
        autopilotAlgorithm = steering;
    }

    public boolean isUsingAutopilot() {
        return usingAutopilotPy.get();
    }

    public void setUsingAutopilot(boolean value) {
        usingAutopilotPy.set(value);
    }

    public BooleanProperty usingAutopilotProperty() { return usingAutopilotPy; }
}