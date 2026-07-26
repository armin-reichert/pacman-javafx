/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.event.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.component.Movement;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.core.model.component.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.steering.RouteBasedSteering;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends Actor {

    private static final int PULSE_CHANGE_TICKS = 10;

    static class BonusWorldMovementPolicy implements WorldMovementPolicy {

        @Override
        public void reset() {}

        @Override
        public boolean canTurnBack() {
            return false;
        }

        @Override
        public boolean canAccessTile(GameLevel gameLevel, Vector2i tile) {
            requireNonNull(gameLevel);
            requireNonNull(tile);
            final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
            if (terrain.outOfBounds(tile)) {
                return terrain.isTileInPortalSpace(tile);
            }
            if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
                return false;
            }
            return !terrain.isTileBlocked(tile);
        }
    }

    private final TickTimer timer;
    private final int symbolCode;
    private final int points;

    private BonusState state;

    // moving bonus only
    private final Pulse jumpingAnimation;
    private RouteBasedSteering routeNavigation;

    public Bonus(int symbolCode, int points) {
        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new BonusWorldMovementPolicy());

        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.timer = new TickTimer("Bonus-Timer");

        this.jumpingAnimation = new Pulse(PULSE_CHANGE_TICKS, Pulse.State.OFF);

        reset();
        worldMovement().setCanTeleport(false); // override default value (true)

        setInactive();
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
    }

    public Vector2i tile() {
        return GameContext.SYSTEMS.worldMovement.computeTile(this);
    }

    public BonusState state() {
        return state;
    }

    public int symbolCode() {
        return symbolCode;
    }

    public int points() {
        return points;
    }

    public void setInactive() {
        state = BonusState.INACTIVE;
        timer.restartIndefinitely();
        GameContext.SYSTEMS.worldMovement.setSpeed(this, 0);
        jumpingAnimation.reset();
        visibility().hide();
    }

    public void showEdibleForSeconds(float seconds) {
        state = BonusState.EDIBLE;
        timer.restartSeconds(seconds);
        visibility().show();
    }

    public void showEdibleAndStartWandering(float speed) {
        state = BonusState.EDIBLE;
        timer.restartIndefinitely();

        GameContext.SYSTEMS.worldMovement.setSpeed(this, speed);
        worldMovement().setTargetTile(null);

        jumpingAnimation.restart();

        visibility().show();
    }

    public void setMazeRoute(List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(waypoints);
        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }
        final var route = new ArrayList<>(waypoints);
        final Vector2i first = route.removeFirst();

        GameContext.SYSTEMS.worldMovement.placeAtTile(this, first);
        GameContext.SYSTEMS.worldMovement.setMoveDir(this, leftToRight ? Direction.RIGHT : Direction.LEFT);
        GameContext.SYSTEMS.worldMovement.setWishDir(this, leftToRight ? Direction.RIGHT : Direction.LEFT);

        routeNavigation = new RouteBasedSteering(route);
    }

    public void showEatenForSeconds(float seconds) {
        state = BonusState.EATEN;
        timer.restartSeconds(seconds);

        GameContext.SYSTEMS.worldMovement.setSpeed(this, 0);

        jumpingAnimation.stop();

        visibility().show();
    }

    @Override
    public void update(GameContext gameContext) {
        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean edibleStateOver;
                if (movement().velX == 0 && movement().velY == 0) {
                    edibleStateOver = timer.hasExpired();
                }
                else {
                    boolean mazeExitReached = wanderMaze(gameContext.assertLevel());
                    edibleStateOver = mazeExitReached || timer.hasExpired();
                }
                if (edibleStateOver) {
                    setInactive();
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive();
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean wanderMaze(GameLevel level) {
        routeNavigation.steer(this, level);
        final Vector2i tile = GameContext.SYSTEMS.worldMovement.computeTile(this);
        boolean mazeExitReached = routeNavigation.isRouteTraversed()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (!mazeExitReached) {
            GameContext.SYSTEMS.worldMovement.navigateTowardsTarget(this, level);
            GameContext.SYSTEMS.worldMovement.tryMovingOrTeleporting(this, level);
            jump();
        }
        return mazeExitReached;
    }

    //TODO check in emulator what's exactly going on
    private void jump() {
        jumpingAnimation.triggerPulse();
        if (jumpingAnimation.pulseTriggered()) {
            float pixels = worldMovement().moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = jumpingAnimation.state() == Pulse.State.ON ? -pixels : pixels;
            position().y += dy;
        }
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s, animation=%s}"
            .formatted(symbolCode, points, timer.remainingTicks(), state, jumpingAnimation);
    }
}