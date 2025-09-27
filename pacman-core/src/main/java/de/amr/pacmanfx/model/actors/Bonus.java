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

    private final byte symbol;
    private final int points;
    private long ticksRemaining;
    private BonusState state;

    private final Pulse jumpAnimation;
    private RouteBasedSteering steering;

    public Bonus(byte symbol, int points, Pulse jumpAnimation) {
        this.symbol = symbol;
        this.points = points;
        this.jumpAnimation = jumpAnimation;
        reset();
        canTeleport = false; // override default value
        ticksRemaining = 0;
        state = BonusState.INACTIVE;
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
            .formatted(symbol, points, ticksRemaining, state, jumpAnimation);
    }

    @Override
    public String name() {
        return "%sBonus_symbol=%s_points=%s".formatted((jumpAnimation != null ? "Moving" : "Static"), symbol, points);
    }

    @Override
    public boolean canTurnBack() {
        return false;
    }

    @Override
    public boolean canAccessTile(GameLevel gameLevel, Vector2i tile) {
        requireNonNull(gameLevel);
        requireNonNull(tile);
        // Portal tiles are the only tiles outside the world map that can be accessed
        if (gameLevel.worldMap().outOfWorld(tile)) {
            return gameLevel.isTileInPortalSpace(tile);
        }
        if (gameLevel.optHouse().isPresent() && gameLevel.optHouse().get().isTileInHouseArea(tile)) {
            return false;
        }
        return !gameLevel.isTileBlocked(tile);
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
        if (jumpAnimation != null) {
            jumpAnimation.stop();
            setSpeed(0);
        }
        hide();
        state = BonusState.INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    /**
     * @param ticks number of ticks the bonus stays edible
     */
    public void setEdible(long ticks) {
        if (jumpAnimation != null) {
            jumpAnimation.restart();
            setSpeed(0.5f); // how fast in the original game?
            setTargetTile(null);
        }
        ticksRemaining = ticks;
        show();
        state = BonusState.EDIBLE;
        Logger.trace("Bonus edible: {}", this);
    }

    /**
     * @param ticks number of ticks the eaten bonus is displayed as the number of points won
     */
    public void setEaten(long ticks) {
        if (jumpAnimation != null) {
            jumpAnimation.stop();
        }
        ticksRemaining = ticks;
        state = BonusState.EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    @Override
    public void tick(GameContext gameContext) {
        switch (state) {
            case EDIBLE -> {
                countDownTimer();
                boolean leftWorld = jumpAnimation != null && tumbleEventuallyLeaveThroughPortal(gameContext.gameLevel());
                if (leftWorld || ticksRemaining == 0) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case EATEN -> {
                countDownTimer();
                if (ticksRemaining == 0) {
                    setInactive();
                    gameContext.eventManager().publishEvent(GameEventType.BONUS_EXPIRED, tile());
                }
            }
            case INACTIVE -> {}
        }
    }

    private void countDownTimer() {
        if (ticksRemaining > 0 && ticksRemaining != TickTimer.INDEFINITE) {
            --ticksRemaining;
        }
    }

    // Moves through level, when end of route is reached, expires.
    private boolean tumbleEventuallyLeaveThroughPortal(GameLevel gameLevel) {
        steering.steer(this, gameLevel);
        if (steering.isComplete()) {
            return true;
        }
        navigateTowardsTarget(gameLevel);
        moveThroughThisCruelWorld(gameLevel);
        jumpAnimation.tick();
        return false;
    }

    //TODO check in emulator what's really going on
    public float jumpHeight() {
        if (jumpAnimation == null || !jumpAnimation.isRunning()) {
            return 0;
        }
        return jumpAnimation.isOn() ? -3f : 3f;
    }
}