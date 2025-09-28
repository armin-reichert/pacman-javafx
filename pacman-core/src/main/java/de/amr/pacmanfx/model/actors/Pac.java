/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Optional;

import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
import static java.util.Objects.requireNonNull;

/**
 * Pac-Man / Ms. Pac-Man.
 */
public class Pac extends MovingActor {

    public static final boolean DEFAULT_USING_AUTOPILOT = false;
    public static final boolean DEFAULT_IMMUNITY = false;

    private static final byte INDEFINITELY = -1;

    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    private final String name;
    private BooleanProperty immune;
    private BooleanProperty usingAutopilot;
    private boolean dead;
    private byte restingTicks;
    private long starvingTicks;
    private Steering autopilotSteering;
    private AnimationManager animationManager;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Pac{" +
                "immune=" + isImmune() +
                ", autopilot=" + isUsingAutopilot() +
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
    public boolean canTurnBack() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(GameLevel gameLevel, Vector2i tile) {
        requireNonNull(gameLevel);
        requireNonNull(tile);
        TerrainLayer terrainLayer = gameLevel.worldMap().terrainLayer();
        // Portal tiles are the only tiles outside the world map that can be accessed
        if (terrainLayer.outOfBounds(tile)) {
            return terrainLayer.isTileInPortalSpace(tile);
        }
        if (terrainLayer.optHouse().isPresent() && terrainLayer.optHouse().get().isTileInHouseArea(tile)) {
            return false; // Schieb ab, Alter!
        }
        return !terrainLayer.isTileBlocked(tile);
    }

    @Override
    public void reset() {
        super.reset();
        dead = false;
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedUp = 1.5f; // no real cornering implementation but better than nothing
        selectAnimation(AnimationSupport.ANIM_PAC_MUNCHING);
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
        if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
        GameLevel gameLevel = gameContext.gameLevel();

        if (dead || restingTicks == INDEFINITELY) {
            return;
        }
        if (restingTicks > 0) {
            restingTicks -= 1;
            return;
        }
        if (isUsingAutopilot()) {
            autopilotSteering.steer(this, gameContext.gameLevel());
        }
        setSpeed(powerTimer.isRunning()
            ? gameContext.game().pacPowerSpeed(gameLevel)
            : gameContext.game().pacNormalSpeed(gameLevel));
        moveThroughThisCruelWorld(gameContext.gameLevel());

        if (moveInfo.moved) {
            this.animationManager().ifPresent(AnimationManager::play);
        } else {
            stopAnimation();
        }
    }

    /**
     * Pac-Man is displayed in its full beauty and does not move anymore as it does at the end of each level.
     */
    public void stopAndShowInFullBeauty() {
        setSpeed(0);
        setRestingTicks(INDEFINITELY);
        this.animationManager().ifPresent(am -> {
            am.stop();
            am.select(AnimationSupport.ANIM_PAC_MUNCHING);
            am.reset();
        });
    }

    public void sayGoodbyeCruelWorld() {
        setSpeed(0);
        stopAnimation();
        dead = true;
    }

    public boolean isAlive() {
        return !dead; // Not sure if the opposite of being dead is being alive ;-)
    }

    public int restingTicks() { return restingTicks; }

    public void setRestingTicks(int ticks) {
        if (ticks != INDEFINITELY && (ticks < 0 || ticks > 127)) {
            throw new IllegalArgumentException("Resting ticks must be INDEFINITE or 0..127, but is " + ticks);
        }
        restingTicks = (byte) ticks;
    }

    /**
     *  @return number of ticks passed since a pellet or an energizer has been eaten.
     */
    public long starvingTicks() { return starvingTicks; }

    public void setStarvingTicks(long starvingTicks) {
        this.starvingTicks = starvingTicks;
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isParalyzed() {
        return velocity().equals(Vector2f.ZERO) || !moveInfo.moved || restingTicks == INDEFINITELY;
    }

    public void setAutopilotSteering(Steering steering) {
        autopilotSteering = requireNonNull(steering);
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = requireNonNull(animationManager);
    }

    public Optional<AnimationManager> animationManager() {
        return Optional.ofNullable(animationManager);
    }
}