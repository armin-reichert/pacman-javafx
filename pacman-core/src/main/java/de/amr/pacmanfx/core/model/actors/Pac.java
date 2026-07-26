/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.Movement;
import de.amr.pacmanfx.core.model.component.PacCheats;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.core.model.component.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends Actor implements UpdatableEntity {

    public static class PacManWorldMovementPolicy implements WorldMovementPolicy {

        @Override
        public void reset() {
        }

        @Override
        public boolean canTurnBack(Actor actor) {
            final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
            return worldMovement.isNewTileEntered();
        }

        @Override
        public boolean canAccessTile(GameContext gameContext, Actor actor, Vector2i tile) {
            requireNonNull(gameContext);
            requireNonNull(tile);

            final GameLevel level = gameContext.assertLevel();
            final TerrainLayer terrain = level.worldMap().terrainLayer();

            // Portal tiles are the only tiles outside the world that can be accessed
            if (terrain.outOfBounds(tile)) {
                return terrain.isTileInPortalSpace(tile);
            }
            if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
                return false; // Schieb ab, Alter!
            }
            return !terrain.isTileBlocked(tile);
        }
    }

    public static final byte REST_FOREVER = -1;

    private final TickTimer powerTimer = new TickTimer("Pac-PowerTimer");

    private final BooleanProperty dead = new SimpleBooleanProperty(false);


    private long restingTicks;

    private long starvingTicks;

    private Steering automaticSteering;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new PacManWorldMovementPolicy());
        registerComponent(PacCheats.class, new PacCheats());

        this.name = requireNonNull(name);
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
    }

    public PacCheats pacCheats() {
        return assertComponent(PacCheats.class);
    }

    @Override
    public String toString() {
        return "Pac{" +
            ", dead=" + isDead() +
            ", restingTime=" + restingTicks +
            ", starvingTime=" + starvingTicks +
            ", visible=" + visibility() +
            ", position=" + position() +
            ", movement=" + movement() +
            '}';
    }

    public void setAutomaticSteering(Steering steering) {
        automaticSteering = requireNonNull(steering);
    }

    @Override
    public void reset() {
        super.reset();

        setDead(false);
        restingTicks = 0;
        starvingTicks = 0;
        worldMovement().corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing
        animations.select(CommonAnimationID.PAC_MUNCHING);
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
    public void update(GameContext gameContext) {

        if (isDead() || restingTicks == REST_FOREVER) {
            return;
        }

        if (restingTicks > 0) {
            restingTicks -= 1;
            return;
        }

        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final GameLevel level = gameContext.assertLevel();
        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();

        if (pacCheats().isUsingAutopilot()) {
            automaticSteering.steer(this, gameContext);
        }

        worldMovementSystem.setSpeed(this, powerTimer.isRunning()
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level));

        worldMovementSystem.tryMovingOrTeleporting(this, gameContext);

        if (worldMovement().info.moved) {
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
        return (movement().velX == 0 && movement().velY == 0)
            || !worldMovement().info.moved
            || restingTicks == REST_FOREVER;
    }
}