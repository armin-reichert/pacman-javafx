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
import de.amr.pacmanfx.core.entities.bonus.comp.BonusRouteInfo;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;
import org.tinylog.Logger;

import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

public class BonusMoveAndJumpSystem {

    private final WorldNavigationSystem navigationSystem;
    private final WorldMovementPolicy movementPolicy;

    public BonusMoveAndJumpSystem(WorldNavigationSystem navigationSystem, WorldMovementPolicy movementPolicy) {
        this.navigationSystem = requireNonNull(navigationSystem);
        this.movementPolicy = requireNonNull(movementPolicy);
    }

    public void setBonusInactive(Bonus bonus) {
        requireNonNull(bonus);
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);
        navigationSystem.setMoveDirSpeed(bonus, 0);
        moveAndJump.jumpPulse().reset();
    }

    public void startWandering(Bonus bonus, BonusRouteInfo routeInfo, float speed) {
        requireNonNull(bonus);
        requireNonNull(routeInfo);
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);
        setRoute(bonus, routeInfo);
        navigationSystem.clearTargetTile(bonus);
        navigationSystem.setMoveDirSpeed(bonus, speed);
        moveAndJump.jumpPulse().restart();
    }

    public void update(GameLevel level, Bonus bonus, MovementSystem motor) {
        if (bonus.hasComp(BonusMoveAndJumpComp.class)) {
            wander(level, bonus, motor);
            jump(bonus);
        }
    }

    private void wander(GameLevel level, Bonus bonus, MovementSystem motor) {
        requireNonNull(level);
        requireNonNull(bonus);
        requireNonNull(motor);

        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);

        moveAndJump.routeNavigation().steer(bonus, level);

        final Vector2i tile = bonus.pos().tile();
        final boolean exitPortalReached = moveAndJump.routeNavigation().isRouteTraversed()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        if (!exitPortalReached) {
            navigationSystem.navigateTowardsTarget(bonus, level, movementPolicy);
            navigationSystem.tryMovingOrTeleporting(level, bonus, motor, movementPolicy);
        }
        moveAndJump.setTargetReached(exitPortalReached);
    }

    private void jump(Bonus bonus) {
        requireNonNull(bonus);

        final WorldNavigationComp worldNavigation = bonus.reqComp(WorldNavigationComp.class);
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);

        final Pulse pulse = moveAndJump.jumpPulse();
        pulse.triggerPulse();
        if (pulse.isTriggered()) {
            float jumpDelta = worldNavigation.moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = pulse.state() == Pulse.State.ON ? -jumpDelta : jumpDelta;
            bonus.pos().setY(bonus.pos().y() + dy);
        }
    }

    private void setRoute(Bonus bonus, BonusRouteInfo routeInfo) {
        if (routeInfo.waypoints().isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }

        final Direction initialDir = routeInfo.leftToRight() ? Direction.RIGHT : Direction.LEFT;
        navigationSystem.setMoveDir(bonus, initialDir);
        navigationSystem.setWishDir(bonus, initialDir);

        final var waypoints = new ArrayList<>(routeInfo.waypoints());
        navigationSystem.placeAtTile(bonus, waypoints.removeFirst());

        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);
        moveAndJump.setRouteNavigation(new RouteGuidedSteering(navigationSystem, movementPolicy, waypoints));
    }
}
