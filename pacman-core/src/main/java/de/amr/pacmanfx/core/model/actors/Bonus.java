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
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
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
public class Bonus extends Actor implements UpdatableEntity {

    private static final int PULSE_CHANGE_TICKS = 10;

    private final TickTimer timer;
    private final int symbolCode;
    private final int points;

    private BonusState state;

    // moving bonus only
    private final Pulse jumpingAnimation;
    private RouteBasedSteering routeNavigation;

    public Bonus(int symbolCode, int points) {
        setComponent(Movement.class, new Movement());
        setComponent(WorldNavigation.class, new WorldNavigation());

        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.timer = new TickTimer("Bonus-Timer");

        this.jumpingAnimation = new Pulse(PULSE_CHANGE_TICKS, Pulse.State.OFF);

        reset();
        worldMovement().setCanTeleport(false); // override default value (true)
    }

    public Movement movement() {
        return assertComponent(Movement.class);
    }

    public WorldNavigation worldMovement() {
        return assertComponent(WorldNavigation.class);
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

    public void setInactive(GameContext gameContext) {
        final WorldNavigationSystem navigator = gameContext.systems().navigator();

        visibility().hide();
        navigator.setSpeed(this, 0);

        jumpingAnimation.reset();

        state = BonusState.INACTIVE;
        timer.restartIndefinitely();
    }

    public void showEdibleForSeconds(float seconds) {
        visibility().show();

        state = BonusState.EDIBLE;
        timer.restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(GameContext gameContext, float speed) {
        final WorldNavigationSystem navigator = gameContext.systems().navigator();

        visibility().show();

        navigator.setSpeed(this, speed);
        worldMovement().setTargetTile(null);

        jumpingAnimation.restart();

        state = BonusState.EDIBLE;
        timer.restartIndefinitely();
    }

    public void setMazeRoute(GameContext gameContext, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(waypoints);
        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }
        final var route = new ArrayList<>(waypoints);
        final Vector2i first = route.removeFirst();

        final WorldNavigationSystem navigator = gameContext.systems().navigator();

        navigator.placeAtTile(this, first);
        navigator.setMoveDir(this, leftToRight ? Direction.RIGHT : Direction.LEFT);
        navigator.setWishDir(this, leftToRight ? Direction.RIGHT : Direction.LEFT);

        routeNavigation = new RouteBasedSteering(route);
    }

    public void showEatenForSeconds(GameContext gameContext, float seconds) {
        final WorldNavigationSystem navigator = gameContext.systems().navigator();

        visibility().show();
        navigator.setSpeed(this, 0);

        jumpingAnimation.stop();

        state = BonusState.EATEN;
        timer.restartSeconds(seconds);
    }

    @Override
    public void update(GameContext gameContext) {
        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean edibleStateOver;
                if (movement().hasZeroSpeed()) {
                    edibleStateOver = timer.hasExpired();
                }
                else {
                    boolean mazeExitReached = wanderMaze(gameContext);
                    edibleStateOver = mazeExitReached || timer.hasExpired();
                }
                if (edibleStateOver) {
                    setInactive(gameContext);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive(gameContext);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean wanderMaze(GameContext gameContext) {
        final GameSystems sys =  gameContext.systems();
        final GameLevel level = gameContext.assertLevel();

        routeNavigation.steer(this, gameContext);

        final Vector2i tile = WorldNavigationSystem.computeTile(this);
        boolean mazeExitReached = routeNavigation.isRouteTraversed() || level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (!mazeExitReached) {
            sys.navigator().navigateTowardsTarget(this, level, sys.bonusWorldMovementPolicy());
            sys.navigator().tryMovingOrTeleporting(this, level, sys.bonusWorldMovementPolicy());
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