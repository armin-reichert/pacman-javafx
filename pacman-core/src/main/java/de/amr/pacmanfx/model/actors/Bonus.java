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

    public static Bonus createMovingBonus(GameContext gameContext, byte symbol, int points) {
        return new Bonus(gameContext, symbol, points, new Pulse(10, false));
    }

    public static Bonus createStaticBonus(GameContext gameContext, byte symbol, int points) {
        return new Bonus(gameContext, symbol, points, null);
    }

    private final byte symbol;
    private final int points;
    private long ticksRemaining;
    private BonusState state;

    private final Pulse moveAnimation;
    private RouteBasedSteering steering;

    private Bonus(GameContext gameContext, byte symbol, int points, Pulse moveAnimation) {
        super(gameContext);
        this.symbol = symbol;
        this.points = points;
        this.moveAnimation = moveAnimation;
        reset();
        canTeleport = false; // override default value
        ticksRemaining = 0;
        state = BonusState.INACTIVE;
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, countdown=%d, state=%s, animation=%s}".formatted(symbol, points, ticksRemaining, state, moveAnimation);
    }

    @Override
    public String name() {
        return "%sBonus_symbol=%s_points=%s".formatted((moveAnimation != null ? "Moving" : "Static"), symbol, points);
    }

    @Override
    public boolean canReverse() {
        return false;
    }

    @Override
    public boolean canAccessTile(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        // Portal tiles are the only tiles outside the world map that can be accessed
        if (!level.isTileInsideWorld(tile)) {
            return level.isTileInPortalSpace(tile);
        }
        if (level.house().isPresent() && level.house().get().isTileInHouseArea(tile)) {
            return false;
        }
        return !level.isTileBlocked(tile);
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
        if (moveAnimation != null) {
            moveAnimation.stop();
            setSpeed(0);
        }
        hide();
        state = BonusState.INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    public void setEdibleTicks(long edibleTicks) {
        if (moveAnimation != null) {
            moveAnimation.restart();
            setSpeed(0.5f); // how fast in the original game?
            setTargetTile(null);
        }
        ticksRemaining = edibleTicks;
        show();
        state = BonusState.EDIBLE;
        Logger.trace("Bonus edible: {}", this);
    }

    public void setEaten(long eatenDurationTicks) {
        if (moveAnimation != null) {
            moveAnimation.stop();
        }
        ticksRemaining = eatenDurationTicks;
        state = BonusState.EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    public void update() {
        switch (state) {
            case INACTIVE -> {}
            case EDIBLE -> {
                if (moveAnimation != null) {
                    updateMovingBonusEdibleState();
                } else {
                    waitForExpiration();
                }
            }
            case EATEN -> waitForExpiration();
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }

    // waits for countdown to reach 0 then expires
    private void waitForExpiration() {
        if (ticksRemaining == 0) {
            setInactive();
            gameContext.theGameEventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
        } else if (ticksRemaining != TickTimer.INDEFINITE) {
            --ticksRemaining;
        }
    }

    // moves, when end of route is reached, expires
    private void updateMovingBonusEdibleState() {
        gameContext.optGameLevel().ifPresent(gameLevel -> {
            steering.steer(this, gameLevel);
            if (steering.isComplete()) {
                setInactive();
                gameContext.theGameEventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
            } else {
                navigateTowardsTarget(gameLevel);
                tryMoving(gameLevel);
                moveAnimation.tick();
            }
        });
    }

    public void setRoute(List<Waypoint> route, boolean leftToRight) {
        requireNonNull(route);
        var mutableRoute = new ArrayList<>(route);
        placeAtTile(mutableRoute.getFirst().tile());
        setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        mutableRoute.removeFirst();
        steering = new RouteBasedSteering(mutableRoute);
    }

    public float elongationY() {
        if (moveAnimation == null || !moveAnimation.isRunning()) {
            return 0;
        }
        return moveAnimation.isOn() ? -3f : 3f;
    }
}