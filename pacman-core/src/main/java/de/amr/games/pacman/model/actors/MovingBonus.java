/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.GameLevel;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 *
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 *
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

    private final Pulse jumpAnimation = new Pulse(10, false);
    private RouteBasedSteering steering;
    private final byte symbol;
    private final int points;
    private long countdown;
    private byte state;

    public MovingBonus(byte symbol, int points) {
        super("MovingBonus-" + symbol);
        this.symbol = symbol;
        this.points = points;
        reset();
        canTeleport = false; // override default from Creature
        countdown = 0;
        state = Bonus.STATE_INACTIVE;
    }

    @Override
    public boolean canReverse() {
        return false;
    }

    @Override
    public MovingBonus entity() {
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
    public byte state() {
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
        jumpAnimation.stop();
        setSpeed(0);
        hide();
        state = Bonus.STATE_INACTIVE;
        Logger.trace("Bonus gets inactive: {}", this);
    }

    @Override
    public void setEdible(long ticks) {
        jumpAnimation.restart();
        setSpeed(0.5f); // how fast in the original game?
        setTargetTile(null);
        show();
        countdown = ticks;
        state = Bonus.STATE_EDIBLE;
        Logger.trace("Bonus gets edible: {}", this);
    }

    @Override
    public void setEaten(long ticks) {
        jumpAnimation.stop();
        countdown = ticks;
        state = Bonus.STATE_EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    public void setRoute(List<NavPoint> route, boolean leftToRight) {
        centerOverTile(route.getFirst().tile());
        setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        route.removeFirst();
        steering = new RouteBasedSteering(this, route);
    }

    public float dy() {
        if (!jumpAnimation.isRunning()) {
            return 0;
        }
        return jumpAnimation.on() ? -3f : 3f;
    }

    @Override
    public void update(GameLevel level) {
        switch (state) {
            case STATE_INACTIVE -> {}
            case STATE_EDIBLE -> {
                steering.steer(level);
                if (steering.isComplete()) {
                    Logger.trace("Moving bonus reached target: {}", this);
                    setInactive();
                    publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
                } else {
                    navigateTowardsTarget();
                    tryMoving();
                    jumpAnimation.tick();
                }
            }
            case STATE_EATEN -> {
                if (countdown == 0) {
                    setInactive();
                    Logger.trace("Bonus expired: {}", this);
                    publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }
}