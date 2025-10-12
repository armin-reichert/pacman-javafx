/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.steering.RouteBasedSteering;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
import static java.util.Objects.requireNonNull;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends MovingActor {

    private final byte symbol;
    private final int points;
    private BonusState state;

    private final TickTimer timer = new TickTimer("Bonus-Timer");

    private long edibleTicks;
    private long eatenTicks;

    // moving bonus only
    private Pulse jumpingAnimation;
    private RouteBasedSteering steering;

    public Bonus(byte symbol, int points) {
        super.reset();
        this.symbol = symbol;
        this.points = points;
        canTeleport = false; // override default value
        state = BonusState.INACTIVE;
        edibleTicks = secToTicks(9.5);
        eatenTicks  = secToTicks(2);
    }

    public void setInactive() {
        state = BonusState.INACTIVE;
        timer.restartIndefinitely();
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
            setSpeed(0);
        }
        hide();
    }

    public void setEdible() {
        state = BonusState.EDIBLE;
        timer.restartTicks(edibleTicks);
        show();
    }

    public void setEdibleAndStartJumping(float speed) {
        jumpingAnimation = new Pulse(10, Pulse.State.OFF);
        jumpingAnimation.restart();
        setSpeed(speed);
        setTargetTile(null);
        setEdible();
    }

    public void setEaten() {
        state = BonusState.EATEN;
        timer.restartTicks(eatenTicks);
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
        }
        show();
    }

    @Override
    public void tick(GameContext gameContext) {
        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean reachedExit = false;
                if (jumpingAnimation != null) {
                     reachedExit = jumpThroughMaze(gameContext.gameLevel());
                }
                if (timer.hasExpired() || reachedExit) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean jumpThroughMaze(GameLevel gameLevel) {
        steering.steer(this, gameLevel);
        boolean reachedExit = steering.isComplete();
        if (!reachedExit) {
            navigateTowardsTarget(gameLevel);
            moveThroughThisCruelWorld(gameLevel);
            jumpingAnimation.tick();
        }
        return reachedExit;
    }

    public void setEdibleDuration(long ticks) {
        this.edibleTicks = ticks;
    }

    public void setEatenDuration(long ticks) {
        this.eatenTicks = ticks;
    }

    public void initRoute(List<Waypoint> waypoints, boolean leftToRight) {
        requireNonNull(waypoints);
        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }
        var route = new ArrayList<>(waypoints);
        Waypoint first = route.removeFirst();
        placeAtTile(first.tile());
        setMoveDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        setWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        steering = new RouteBasedSteering(route);
    }

    public float verticalElongation() {
        if (jumpingAnimation == null || !jumpingAnimation.isRunning()) {
            return 0;
        }
        //TODO check in emulator what's exactly going on
        int pixels = moveDir().isVertical() ? 4 : 2;
        return jumpingAnimation.state() == Pulse.State.ON ? -pixels : pixels;
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s, animation=%s}"
            .formatted(symbol, points, timer.remainingTicks(), state, jumpingAnimation);
    }

    @Override
    public String name() {
        return "%sBonus_symbol=%s_points=%s".formatted((jumpingAnimation != null ? "Jumping" : "Static"), symbol, points);
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

    public BonusState state() {
        return state;
    }

    public byte symbol() {
        return symbol;
    }

    public int points() {
        return points;
    }
}