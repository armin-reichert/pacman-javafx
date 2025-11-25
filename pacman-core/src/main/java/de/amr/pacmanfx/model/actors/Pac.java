/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends MovingActor {

    public static final boolean DEFAULT_USING_AUTOPILOT = false;
    public static final boolean DEFAULT_IMMUNITY = false;

    private static final byte INDEFINITELY = -1;

    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    private BooleanProperty immune;
    private BooleanProperty usingAutopilot;
    private boolean dead;
    private int restingTime;
    private long starvingTime;
    private Steering autopilotSteering;

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
            ", dead=" + dead +
            ", restingTime=" + restingTime +
            ", starvingTime=" + starvingTime +
            ", visible=" + isVisible() +
            ", position=" + position() +
            ", velocity=" + velocity() +
            ", acceleration=" + acceleration() +
            '}';
    }

    @Override
    public boolean canTurnBack() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(GameLevel gameLevel, Vector2i tile) {
        requireNonNull(gameLevel);
        requireNonNull(tile);
        TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
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
        dead = false;
        restingTime = 0;
        starvingTime = 0;
        corneringSpeedUp = 1.5f; // no real cornering implementation but better than nothing
        selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
    }

    public BooleanProperty immuneProperty() {
        if (immune == null) {
            immune = new SimpleBooleanProperty(DEFAULT_IMMUNITY);
        }
        return immune;
    }

    public boolean isImmune() {
        return immune != null ? immune.get() : DEFAULT_IMMUNITY;
    }

    public void setImmune(boolean value) {
        if (immune == null && value == DEFAULT_IMMUNITY) return;
        immuneProperty().set(value);
    }

    public BooleanProperty usingAutopilotProperty() {
        if (usingAutopilot == null) {
            usingAutopilot = new SimpleBooleanProperty(DEFAULT_USING_AUTOPILOT);
        }
        return usingAutopilot;
    }

    public boolean isUsingAutopilot() {
        return usingAutopilot != null ? usingAutopilot.get() : DEFAULT_USING_AUTOPILOT;
    }

    public void setUsingAutopilot(boolean value) {
        if (usingAutopilot == null && value == DEFAULT_USING_AUTOPILOT) return;
        usingAutopilotProperty().set(value);
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public boolean isPowerFading(GameLevel gameLevel) {
        long fadingTicks = secToTicks(gameLevel.game().pacPowerFadingSeconds(gameLevel));
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerFadingStarting(GameLevel gameLevel) {
        long fadingTicks = secToTicks(gameLevel.game().pacPowerFadingSeconds(gameLevel));
        return powerTimer.isRunning() && powerTimer.remainingTicks() == fadingTicks
            || powerTimer.durationTicks() < fadingTicks && powerTimer.tickCount() == 1;
    }

    @Override
    public void tick(GameContext gameContext) {
        if (gameContext.optGameLevel().isEmpty()) return;
        final GameLevel gameLevel = gameContext.gameLevel();

        if (dead || restingTime == INDEFINITELY) {
            return;
        }

        if (restingTime > 0) {
            restingTime -= 1;
            return;
        }

        if (isUsingAutopilot()) {
            autopilotSteering.steer(this, gameLevel);
        }

        setSpeed(powerTimer.isRunning()
            ? gameLevel.game().pacSpeedWhenHasPower(gameLevel)
            : gameLevel.game().pacSpeed(gameLevel));
        moveThroughThisCruelWorld(gameLevel);

        if (moveInfo.moved) {
            optAnimationManager().ifPresent(AnimationManager::play);
        } else {
            stopAnimation();
        }
    }

    public void onFoodEaten(boolean energizer) {
        setRestingTime(energizer ? 3 : 1);
        endStarving();
    }

    public void onLevelCompleted() {
        stopAnimation();
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and reset to zero.");
        setSpeed(0);
        setRestingTime(INDEFINITELY);
        selectAnimation(CommonAnimationID.ANIM_PAC_FULL);
    }

    public void onKilled() {
        stopAnimation();
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and reset to zero.");
        setSpeed(0);
        dead = true;
    }

    public boolean isAlive() {
        return !dead; // Not sure if the opposite of being dead is being alive ;-)
    }

    /**
     * @return number of ticks Pac is resting
     */
    public int restingTime() { return restingTime; }

    /**
     * Sets the number of ticks Pac-Man is resting.
     *
     * @param ticks number of ticks
     */
    public void setRestingTime(int ticks) {
        restingTime = ticks;
    }

    /**
     *  @return number of ticks passed since a pellet or an energizer has been eaten.
     */
    public long starvingTime() { return starvingTime; }

    public void starve() {
        ++starvingTime;
    }

    public void endStarving() {
        starvingTime = 0;
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isParalyzed() {
        return velocity().equals(Vector2f.ZERO) || !moveInfo.moved || restingTime == INDEFINITELY;
    }

    public void setAutopilotSteering(Steering steering) {
        autopilotSteering = requireNonNull(steering);
    }
}