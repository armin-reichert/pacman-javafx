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

    private final Pulse movementAnimation;
    private RouteBasedSteering steering;

    public Bonus(byte symbol, int points, Pulse movementAnimation) {
        this.symbol = symbol;
        this.points = points;
        this.movementAnimation = movementAnimation;
        reset();
        canTeleport = false; // override default value
        edibleTicks = TickTimer.secToTicks(9.5);
        eatenTicks = TickTimer.secToTicks(2);
        ticksRemaining = 0;
        state = BonusState.INACTIVE;
    }

    @Override
    public void tick(GameContext gameContext) {
        switch (state) {
            case EDIBLE -> {
                countdown();
                expireOnWorldLeftOrTimeout(gameContext);
            }
            case EATEN -> {
                countdown();
                expireOnTimeout(gameContext);
            }
            case INACTIVE -> {}
        }
    }

    private boolean canMove() {
        return movementAnimation != null;
    }

    private boolean move(GameLevel gameLevel) {
        steering.steer(this, gameLevel);
        boolean complete = steering.isComplete();
        if (!complete) {
            navigateTowardsTarget(gameLevel);
            moveThroughThisCruelWorld(gameLevel);
            movementAnimation.tick();
        }
        return complete;
    }

    private void expireOnWorldLeftOrTimeout(GameContext gameContext) {
        boolean expired = ticksRemaining == 0 || (canMove() && move(gameContext.gameLevel()));
        if (expired) {
            expire(gameContext);
        }
    }

    private void expireOnTimeout(GameContext gameContext) {
        if (ticksRemaining == 0) {
            expire(gameContext);
        }
    }

    private void expire(GameContext gameContext) {
        setInactive();
        gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
        Logger.info("{} expired", this);
    }

    public void setEdibleTicks(long edibleTicks) {
        this.edibleTicks = edibleTicks;
    }

    public void setEatenTicks(long eatenTicks) {
        this.eatenTicks = eatenTicks;
    }

    public void setRoute(List<Waypoint> route, boolean leftToRight) {
        requireNonNull(route);
        var mutableRoute = new ArrayList<>(route);
        placeAtTile(mutableRoute.getFirst().tile());
        setMoveDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        setWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        mutableRoute.removeFirst();
        steering = new RouteBasedSteering(mutableRoute);
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, countdown=%d, state=%s, animation=%s}"
            .formatted(symbol, points, ticksRemaining, state, movementAnimation);
    }

    @Override
    public String name() {
        return "%sBonus_symbol=%s_points=%s".formatted((movementAnimation != null ? "Moving" : "Static"), symbol, points);
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
        if (movementAnimation != null) {
            movementAnimation.stop();
            setSpeed(0);
        }
        state = BonusState.INACTIVE;
        hide();
        Logger.trace("Bonus inactive: {}", this);
    }

    public void setEdible() {
        if (movementAnimation != null) {
            movementAnimation.restart();
            setSpeed(0.5f); // how fast in the original game?
            setTargetTile(null);
        }
        ticksRemaining = edibleTicks;
        state = BonusState.EDIBLE;
        show();
        Logger.trace("Bonus edible: {}", this);
    }

    public void setEaten() {
        if (movementAnimation != null) {
            movementAnimation.stop();
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

    //TODO check in emulator what's really going on
    public float jumpHeight() {
        if (movementAnimation == null || !movementAnimation.isRunning()) {
            return 0;
        }
        return movementAnimation.state() == Pulse.State.ON ? -3f : 3f;
    }
}