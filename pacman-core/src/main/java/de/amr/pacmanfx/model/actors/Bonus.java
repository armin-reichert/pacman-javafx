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
 * A bonus that either stays at a fixed position or tumbles through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends MovingActor {

    private final byte symbol;
    private final int points;
    private long ticksRemaining;
    private BonusState state;
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
        edibleTicks = secToTicks(9.5);
        eatenTicks  = secToTicks(2);
        ticksRemaining = 0;
        state = BonusState.INACTIVE;
    }

    @Override
    public void tick(GameContext gameContext) {
        switch (state) {
            case EDIBLE -> {
                countdown();
                boolean expired = ticksRemaining == 0 || (canJump() && moveThroughMaze(gameContext.gameLevel()));
                if (expired) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case EATEN -> {
                countdown();
                if (ticksRemaining == 0) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case INACTIVE -> {}
        }
    }

    private boolean canJump() {
        return jumpingAnimation != null;
    }

    private boolean moveThroughMaze(GameLevel gameLevel) {
        steering.steer(this, gameLevel);
        boolean complete = steering.isComplete();
        if (!complete) {
            navigateTowardsTarget(gameLevel);
            moveThroughThisCruelWorld(gameLevel);
            jumpingAnimation.tick();
        }
        return complete;
    }

    public void setEdibleDuration(long ticks) {
        this.edibleTicks = ticks;
    }

    public void setEatenDuration(long ticks) {
        this.eatenTicks = ticks;
    }

    public void setRoute(List<Waypoint> waypoints, boolean leftToRight) {
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

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, countdown=%d, state=%s, animation=%s}"
            .formatted(symbol, points, ticksRemaining, state, jumpingAnimation);
    }

    @Override
    public String name() {
        return "%sBonus_symbol=%s_points=%s".formatted((jumpingAnimation != null ? "Moving" : "Static"), symbol, points);
    }

    @Override
    public boolean canTurnBack() {
        return false;
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
            return false;
        }
        return !terrainLayer.isTileBlocked(tile);
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
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
            setSpeed(0);
        }
        state = BonusState.INACTIVE;
        hide();
        Logger.trace("Bonus inactive: {}", this);
    }

    public void setEdibleAndStartMoving(float speed) {
        setSpeed(speed);
        setTargetTile(null);
        jumpingAnimation = new Pulse(10, Pulse.State.OFF);
        jumpingAnimation.restart();
        setEdible();
    }

    public void setEdible() {
        state = BonusState.EDIBLE;
        ticksRemaining = edibleTicks;
        show();
    }

    public void setEaten() {
        if (jumpingAnimation != null) {
            jumpingAnimation.stop();
        }
        ticksRemaining = eatenTicks;
        state = BonusState.EATEN;
        show();
        Logger.trace("Bonus eaten: {}", this);
    }

    private void countdown() {
        if (ticksRemaining > 0 && ticksRemaining != TickTimer.INDEFINITE) {
            --ticksRemaining;
        }
    }

    //TODO check in emulator what's exactly going on
    public float jumpElongation() {
        if (jumpingAnimation == null || !jumpingAnimation.isRunning()) {
            return 0;
        }
        int pixels = moveDir().isVertical() ? 4 : 2;
        return jumpingAnimation.state() == Pulse.State.ON ? -pixels : pixels;
    }
}