/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.event.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.BonusJumpAnimation;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.bonus.BonusJumpAnimationSystem;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

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

    private final TickTimer timer;
    private final int symbolCode;
    private final int points;

    private BonusState state;

    public Bonus(int symbolCode, int points) {
        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.timer = new TickTimer("Bonus-Timer");

        setComponent(Movement.class, new Movement());
        setComponent(WorldNavigation.class, new WorldNavigation());
        setComponent(BonusJumpAnimation.class, new BonusJumpAnimation());

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
        requireNonNull(gameContext);
        final GameSystems sys = gameContext.systems();

        state = BonusState.INACTIVE;
        timer.restartIndefinitely();
        hide();
        sys.navigator().setSpeed(this, 0);
        sys.bonusJumpAnimation().reset(this);
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
        sys.navigator().setSpeed(this, speed);
        //TODO use system method:
        worldMovement().setTargetTile(null);
        sys.bonusJumpAnimation().start(this);
    }

    public void setMazeRoute(GameContext gameContext, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();
        sys.bonusJumpAnimation().setMazeRoute(this, waypoints, leftToRight);
    }

    public void showEatenForSeconds(GameContext gameContext, float seconds) {
        requireNonNull(gameContext);

        final WorldNavigationSystem navigator = gameContext.systems().navigator();
        final BonusJumpAnimationSystem animationSystem = gameContext.systems().bonusJumpAnimation();

        state = BonusState.EATEN;
        timer.restartSeconds(seconds);
        show();
        navigator.setSpeed(this, 0);
        animationSystem.stop(this);
    }

    @Override
    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameSystems sys = gameContext.systems();
        final BonusJumpAnimation animation = assertComponent(BonusJumpAnimation.class);
        final GameLevel level = gameContext.assertLevel();

        timer.doTick();
        switch (state) {
            case EDIBLE -> {
                boolean edibleStateOver;
                if (movement().hasZeroSpeed()) {
                    edibleStateOver = timer.hasExpired();
                }
                else {
                    sys.bonusJumpAnimation().update(level, this);
                    edibleStateOver = animation.targetReached() || timer.hasExpired();
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

    @Override
    public String toString() {
        return "Bonus{symbol=%s, points=%d, ticksRemaining=%d, state=%s}"
            .formatted(symbolCode, points, timer.remainingTicks(), state);
    }
}