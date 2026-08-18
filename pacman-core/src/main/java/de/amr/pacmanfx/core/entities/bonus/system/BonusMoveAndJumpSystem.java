/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BonusMoveAndJumpSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy worldMovementPolicy;

    public BonusMoveAndJumpSystem(WorldNavigationSystem navigator, WorldMovementPolicy worldMovementPolicy) {
        this.navigator = requireNonNull(navigator);
        this.worldMovementPolicy = requireNonNull(worldMovementPolicy);
    }

    public void update(GameLevel level, Bonus bonus, MovementSystem motor) {
        requireNonNull(level);
        requireNonNull(bonus);
        requireNonNull(motor);

        switch (bonus.state().bonusState()) {
            case INACTIVE -> {
            }

            case EDIBLE -> {
                wanderMaze(motor, level, bonus);
                jump(bonus);
            }

            case EATEN -> {}
        }
    }

    public void setBonusInactive(
        Bonus bonus,
        BonusMoveAndJumpSystem moveAndJumpSystem,
        WorldNavigationSystem worldNavigationSystem)
    {
        if (bonus.optMovement().isPresent()) {
            worldNavigationSystem.setSpeed(bonus, 0);
        }
        bonus.optMoveAndJump().ifPresent(moveAndJumpSystem::reset);
    }

    public void reset(BonusMoveAndJumpComp animation) {
        requireNonNull(animation);
        animation.pulse().reset();
    }

    public void start(BonusMoveAndJumpComp animation) {
        requireNonNull(animation);
        animation.pulse().restart();
    }

    public void stop(BonusMoveAndJumpComp animation) {
        requireNonNull(animation);
        animation.pulse().stop();
    }

    public void setRoute(Bonus bonus, List<Vector2i> waypoints, boolean fromLeftToRight) {
        requireNonNull(bonus);
        requireNonNull(waypoints);

        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }

        final var route = new ArrayList<>(waypoints);
        final Direction initialDir = fromLeftToRight ? Direction.RIGHT : Direction.LEFT;
        navigator.placeAtTile(bonus, route.removeFirst());
        navigator.setMoveDir(bonus, initialDir);
        navigator.setWishDir(bonus, initialDir);

        final var steering = new RouteGuidedSteering(navigator, worldMovementPolicy, route);
        bonus.reqComp(BonusMoveAndJumpComp.class).setRouteNavigation(steering);
    }

    private void wanderMaze(MovementSystem motor, GameLevel level, Bonus bonus) {
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);

        moveAndJump.routeNavigation().steer(bonus, level);

        final Vector2i tile = bonus.pos().tile();
        final boolean exitPortalReached = moveAndJump.routeNavigation().isRouteTraversed()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        if (!exitPortalReached) {
            navigator.navigateTowardsTarget(bonus, level, worldMovementPolicy);
            navigator.tryMovingOrTeleporting(motor, bonus, level, worldMovementPolicy);
        }
        moveAndJump.setTargetReached(exitPortalReached);
    }

    private void jump(Bonus bonus) {
        final WorldNavigationComp worldNavigation = bonus.reqComp(WorldNavigationComp.class);
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);

        final Pulse pulse = moveAndJump.pulse();
        pulse.triggerPulse();
        if (pulse.pulseTriggered()) {
            float jumpDelta = worldNavigation.moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = pulse.state() == Pulse.State.ON ? -jumpDelta : jumpDelta;
            bonus.pos().setY(bonus.pos().y() + dy);
        }
    }
}
