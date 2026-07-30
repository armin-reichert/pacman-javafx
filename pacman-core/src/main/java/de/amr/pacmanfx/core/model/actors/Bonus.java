/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.BonusMoveAndJumpAnimationComponent;
import de.amr.pacmanfx.core.model.component.bonus.BonusState;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.world.WorldNavigationComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends GameEntity implements UpdatableEntity {

    private final TickTimer timer;
    private final int symbolCode;
    private final int points;

    private boolean edibleStateExpired;
    private BonusState state;

    public Bonus(int symbolCode, int points) {
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.timer = new TickTimer("Bonus-Timer");
        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);

        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(WorldNavigationComponent.class, new WorldNavigationComponent());

        // To add support for animated maze walking, the following component has to be added
        //setComponent(BonusJumpAnimation.class, new BonusJumpAnimation());

        reset();

        worldMovement().setCanTeleport(false); // override default value (true)
    }

    @Override
    public void reset() {
        super.reset();
        edibleStateExpired = false;
        state = BonusState.INACTIVE;
    }

    @Override
    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();

        timer.doTick();

        switch (state) {
            case EDIBLE -> {
                if (supportsMoveAndJumpAnimation()) {
                    optComponent(BonusMoveAndJumpAnimationComponent.class).ifPresent(animation -> {
                        sys.bonusJumpAnimation().update(level, this);
                        edibleStateExpired = animation.targetReached() || timer.hasExpired();
                    });
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    edibleStateExpired = timer.hasExpired();
                }
                if (edibleStateExpired) {
                    setInactive(sys);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case EATEN -> {
                if (timer.hasExpired()) {
                    setInactive(sys);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(this));
                }
            }
            case INACTIVE -> {}
        }
    }

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s}".formatted(
            symbolCode, points, timer.remainingTicks(), state);
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }

    public WorldNavigationComponent worldMovement() {
        return requireComponent(WorldNavigationComponent.class);
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

    public void setInactive(GameSystems sys) {
        state = BonusState.INACTIVE;
        timer.restartIndefinitely();
        hide();

        //TODO reconsider this:
        sys.navigator().setSpeed(this, 0);

        if (supportsMoveAndJumpAnimation()) {
            sys.bonusJumpAnimation().reset(this);
        }
    }

    public void showEdibleForSeconds(float seconds) {
        state = BonusState.EDIBLE;
        timer.restartSeconds(seconds);
        show();
    }

    public void showEdibleAndStartWandering(GameContext gameContext, float speed) {
        final GameSystems sys = gameContext.systems();

        state = BonusState.EDIBLE;
        timer.restartIndefinitely();
        show();

        //TODO reconsider this:
        sys.navigator().setSpeed(this, speed);

        //TODO use system method:
        worldMovement().setTargetTile(null);

        if (supportsMoveAndJumpAnimation()) {
            sys.bonusJumpAnimation().start(this);
        }
    }

    public void setMazeRoute(GameContext gameContext, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(gameContext);

        if (supportsMoveAndJumpAnimation()) {
            final GameSystems sys = gameContext.systems();
            sys.bonusJumpAnimation().setMazeRoute(this, waypoints, leftToRight);
        }
        else {
            Logger.error("Cannot set bonus route: No bonus animation support!");
        }
    }

    public void showEatenForSeconds(GameContext gameContext, float seconds) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();

        state = BonusState.EATEN;
        timer.restartSeconds(seconds);
        show();

        //TODO reconsider this:
        sys.navigator().setSpeed(this, 0);

        if (supportsMoveAndJumpAnimation()) {
            sys.bonusJumpAnimation().stop(this);
        }
    }

    private boolean supportsMoveAndJumpAnimation() {
        return hasComponent(BonusMoveAndJumpAnimationComponent.class);
    }

}