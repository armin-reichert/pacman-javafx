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
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 *
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class MovingBonus extends MovingActor implements BonusEntity {

    private final Pulse animation = new Pulse(10, false);
    private final byte symbol;
    private final int points;
    private BonusState state;
    private long countdown;
    private RouteBasedSteering steering;

    public MovingBonus(GameContext gameContext, byte symbol, int points) {
        super(gameContext);
        this.symbol = symbol;
        this.points = points;
        reset();
        canTeleport = false; // override default value
        countdown = 0;
        state = BonusState.INACTIVE;
    }

    @Override
    public String name() {
        return "MovingBonus-symbol-" + symbol + "-points-" + points;
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

    @Override
    public MovingBonus actor() {
        return this;
    }

    @Override
    public String toString() {
        return "MovingBonus{" +
            "symbol=" + symbol +
            ", points=" + points +
            ", countdown=" + countdown +
            ", state=" + state +
            '}';
    }

    @Override
    public BonusState state() {
        return state;
    }

    @Override
    public byte symbol() {
        return symbol;
    }

    @Override
    public int points() {
        return points;
    }

    @Override
    public void setInactive() {
        animation.stop();
        setSpeed(0);
        hide();
        state = BonusState.INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    @Override
    public void setEdibleTicks(long ticks) {
        animation.restart();
        setSpeed(0.5f); // how fast in the original game?
        setTargetTile(null);
        show();
        countdown = ticks;
        state = BonusState.EDIBLE;
        Logger.trace("Bonus edible: {}", this);
    }

    @Override
    public void setEaten(long ticks) {
        animation.stop();
        countdown = ticks;
        state = BonusState.EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    @Override
    public void update() {
        switch (state) {
            case INACTIVE -> {}
            case EDIBLE -> gameContext.optGameLevel().ifPresent(gameLevel -> {
                steering.steer(this, gameLevel);
                if (steering.isComplete()) {
                    setInactive();
                    gameContext.theGameEventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                } else {
                    navigateTowardsTarget(gameLevel);
                    tryMoving(gameLevel);
                    animation.tick();
                }
            });
            case EATEN -> {
                if (countdown == 0) {
                    setInactive();
                    gameContext.theGameEventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }

    public void setRoute(List<Waypoint> route, boolean leftToRight) {
        requireNonNull(route);
        var routeCopy = new ArrayList<>(route);
        placeAtTile(routeCopy.getFirst().tile());
        setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        routeCopy.removeFirst();
        steering = new RouteBasedSteering(routeCopy);
    }

    public float elongationY() {
        if (!animation.isRunning()) {
            return 0;
        }
        return animation.isOn() ? -3f : 3f;
    }
}