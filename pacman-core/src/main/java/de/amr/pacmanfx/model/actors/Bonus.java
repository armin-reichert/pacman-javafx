/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.event.BonusExpiredEvent;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.steering.RouteBasedSteering;
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
public class Bonus extends MovingActor {

    private static final int PULSE_CHANGE_TICKS = 10;

    private final TickTimer timer = new TickTimer("Bonus-Timer");
    private final byte symbol;
    private final int points;

    private BonusState state;

    // moving bonus only
    private final Pulse jumpingAnimation;
    private RouteBasedSteering routeNavigation;

    public Bonus(byte symbol, int points) {
        super("Bonus-symbol:%d-points:%d".formatted(symbol, points));
        this.symbol = (byte) Validations.requireNonNegativeInt(symbol);
        this.points = Validations.requireNonNegativeInt(points);
        jumpingAnimation = new Pulse(PULSE_CHANGE_TICKS, Pulse.State.OFF);

        reset();
        canTeleport = false; // override default value (true)

        // initial state
        setInactive();
    }

    public BonusState state() {
        return state;
    }

    public byte symbol() {
        return symbol;
    }

    public int points() {
        return points;
    }

    public void setInactive() {
        state = BonusState.INACTIVE;
        timer.restartIndefinitely();

        setSpeed(0);
        jumpingAnimation.reset();

        hide();
    }

    public void showEdibleForSeconds(float seconds) {
        state = BonusState.EDIBLE;
        timer.restartSeconds(seconds);

        show();
    }

    public void showEdibleAndStartWandering(float speed) {
        state = BonusState.EDIBLE;
        timer.restartIndefinitely();

        setSpeed(speed);
        setTargetTile(null);
        jumpingAnimation.restart();

        show();
    }

    public void setMazeRoute(List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(waypoints);
        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }
        final var route = new ArrayList<>(waypoints);
        final Vector2i first = route.removeFirst();
        placeAtTile(first);
        setMoveDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        setWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        routeNavigation = new RouteBasedSteering(route);
    }

    public void showEatenForSeconds(float seconds) {
        state = BonusState.EATEN;
        timer.restartSeconds(seconds);

        setSpeed(0);
        jumpingAnimation.stop();

        show();
    }

    @Override
    public void tick(Game game) {
        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean edibleStateOver;
                if (velocity().equals(Vector2f.ZERO)) {
                    edibleStateOver = timer.hasExpired();
                }
                else {
                    final GameLevel level = game.optGameLevel().orElseThrow();
                    boolean mazeExitReached = wanderMaze(level);
                    edibleStateOver = mazeExitReached || timer.hasExpired();
                }
                if (edibleStateOver) {
                    setInactive();
                    game.publishGameEvent(new BonusExpiredEvent(game, this));
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive();
                    game.publishGameEvent(new BonusExpiredEvent(game, this));
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean wanderMaze(GameLevel level) {
        routeNavigation.steer(this, level);
        boolean mazeExitReached = routeNavigation.isComplete()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile());
        if (!mazeExitReached) {
            navigateTowardsTarget(level);
            tryMovingOrTeleporting(level);
            jump();
        }
        return mazeExitReached;
    }

    //TODO check in emulator what's exactly going on
    private void jump() {
        jumpingAnimation.doTick();
        if (jumpingAnimation.pulseTriggered()) {
            float pixels = moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = jumpingAnimation.state() == Pulse.State.ON ? -pixels : pixels;
            setY(y() + dy);
        }
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s, animation=%s}"
            .formatted(symbol, points, timer.remainingTicks(), state, jumpingAnimation);
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