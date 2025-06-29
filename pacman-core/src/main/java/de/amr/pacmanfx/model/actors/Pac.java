/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static java.util.Objects.requireNonNull;

/**
 * Pac-Man / Ms. Pac-Man.
 */
public class Pac extends MovingActor implements Animated {

    private static final byte INDEFINITELY = -1;

    private final BooleanProperty immunePy = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilotPy = new SimpleBooleanProperty(false);
    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    private final String name;
    private boolean dead;
    private byte restingTicks;
    private long starvingTicks;
    private Steering autopilotSteering;
    private ActorAnimationMap animationMap;

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
                ", visible=" + isVisible() +
                ", position=" + position() +
                ", velocity=" + velocity() +
                ", acceleration=" + acceleration() +
                '}';
    }

    @Override
    public boolean canReverse() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        // Portal tiles are the only tiles outside the world map that can be accessed
        if (!level.isTileInsideWorld(tile)) {
            return level.isTileInPortalSpace(tile);
        }
        if (level.house().isPresent() && level.house().get().isTileInHouseArea(tile)) {
            return false; // Du komms hier nich rein!
        }
        return !level.isTileBlocked(tile);
    }

    @Override
    public void reset() {
        super.reset();
        dead = false;
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedUp = 1.5f; // no real cornering implementation but better than nothing
        selectAnimation(ANIM_PAC_MUNCHING);
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

    public boolean isPowerFading(GameLevel level) {
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= theGame().pacPowerFadingTicks(level);
    }

    public boolean isPowerFadingStarting(GameLevel level) {
        return powerTimer.isRunning() && powerTimer.remainingTicks() == theGame().pacPowerFadingTicks(level)
            || powerTimer.durationTicks() < theGame().pacPowerFadingTicks(level) && powerTimer.tickCount() == 1;
    }

    public void update(GameLevel level) {
        if (dead || restingTicks == INDEFINITELY) {
            return;
        }
        if (restingTicks > 0) {
            restingTicks -= 1;
            return;
        }
        if (isUsingAutopilot()) {
            autopilotSteering.steer(this, level);
        }
        setSpeed(powerTimer.isRunning()
            ? theGame().actorSpeedControl().pacPowerSpeed(level)
            : theGame().actorSpeedControl().pacNormalSpeed(level));
        tryMoving(level);

        if (moveInfo.moved) {
            playAnimation();
        } else {
            stopAnimation();
        }
    }

    /**
     * Pac-Man is displayed in its full beauty and does not move anymore.
     */
    public void stopAndShowInFullBeauty() {
        setSpeed(0);
        setRestingTicks(INDEFINITELY);
        stopAnimation();
        selectAnimation(ANIM_PAC_MUNCHING);
        resetAnimation();
    }

    public void sayGoodbyeCruelWorld() {
        setSpeed(0);
        stopAnimation();
        dead = true;
    }

    public boolean isAlive() {
        return !dead;
    }


    public int restingTicks() { return restingTicks; }
    public void setRestingTicks(int ticks) {
        if (ticks != INDEFINITELY && (ticks < 0 || ticks > 127)) {
            throw new IllegalArgumentException("Resting ticks must be INDEFINITE or 0..127, but is " + ticks);
        }
        restingTicks = (byte) ticks;
    }

    /**
     *  @return number of ticks passed since pellet or energizer was eaten.
     */
    public long starvingTicks() { return starvingTicks; }
    public void starvingIsOver() { starvingTicks = 0; }
    public void starve() { ++starvingTicks; }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isStandingStill() {
        return velocity().equals(Vector2f.ZERO) || !moveInfo.moved || restingTicks == INDEFINITELY;
    }

    public void setAutopilotSteering(Steering steering) { autopilotSteering = steering; }
    public boolean isUsingAutopilot() { return usingAutopilotPy.get(); }
    public void setUsingAutopilot(boolean value) { usingAutopilotPy.set(value); }
    public BooleanProperty usingAutopilotProperty() { return usingAutopilotPy; }

    public void setAnimations(ActorAnimationMap animationMap) {
        this.animationMap = requireNonNull(animationMap);
    }

    @Override
    public Optional<ActorAnimationMap> animationMap() {
        return Optional.ofNullable(animationMap);
    }
}