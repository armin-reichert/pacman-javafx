/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.event.BonusExpiredEvent;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2b;
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

    private final TickTimer timer = new TickTimer("Bonus-Timer");
    private final byte symbol;
    private final int points;

    private BonusState state;

    // moving bonus only
    private Pulse jumpingAnimation;
    private RouteBasedSteering steering;

    public Bonus(byte symbol, int points) {
        super("Bonus-symbol:%d-points:%d".formatted(symbol, points));
        this.symbol = symbol;
        this.points = points;
        reset();
        canTeleport = false; // override default value (true)
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
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
            jumpingAnimation.reset();
        }
        setSpeed(0);
        hide();
    }

    public void setEdibleSeconds(float seconds) {
        state = BonusState.EDIBLE;
        timer.restartSeconds(seconds);
        show();
    }

    public void setEdibleAndStartJumpingAtSpeed(float speed) {
        jumpingAnimation = new Pulse(10, Pulse.State.OFF);
        jumpingAnimation.restart();
        setSpeed(speed);
        setTargetTile(null);
        state = BonusState.EDIBLE;
        timer.restartIndefinitely();
        show();
    }

    public void setEatenSeconds(float seconds) {
        state = BonusState.EATEN;
        timer.restartSeconds(seconds);
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
        }
        show();
    }

    @Override
    public void tick(Game game) {
        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean reachedExit = false;
                if (jumpingAnimation != null) {
                     reachedExit = jumpThroughMaze(game.level());
                }
                if (timer.hasExpired() || reachedExit) {
                    setInactive();
                    game.publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive();
                    game.publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean jumpThroughMaze(GameLevel gameLevel) {
        steering.steer(this, gameLevel);
        boolean reachedExit = steering.isComplete() || gameLevel.worldMap().terrainLayer().isTileInPortalSpace(tile());
        if (!reachedExit) {
            navigateTowardsTarget(gameLevel);
            moveThroughThisCruelWorld(gameLevel);
            jumpingAnimation.tick();
        }
        return reachedExit;
    }

    public void initRoute(List<Vector2b> waypoints, boolean leftToRight) {
        requireNonNull(waypoints);
        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }
        var route = new ArrayList<>(waypoints);
        Vector2b first = route.removeFirst();
        placeAtTile(first.toVector2i());
        setMoveDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        setWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        steering = new RouteBasedSteering(route);
    }

    public float verticalElongation() {
        if (jumpingAnimation == null || !jumpingAnimation.isRunning()) {
            return 0;
        }
        //TODO check in emulator what's exactly going on
        int pixels = moveDir().isVertical() ? 2 : 1;
        return jumpingAnimation.state() == Pulse.State.ON ? -pixels : pixels;
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s, animation=%s}"
            .formatted(symbol, points, timer.remainingTicks(), state, jumpingAnimation);
    }

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
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().isTileInHouseArea(tile)) {
            return false;
        }
        return !terrain.isTileBlocked(tile);
    }
}