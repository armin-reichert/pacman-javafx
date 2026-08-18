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

    private final WorldNavigationSystem worldNavigationSystem;
    private final WorldMovementPolicy worldMovementPolicy;

    public BonusMoveAndJumpSystem(WorldNavigationSystem navigator, WorldMovementPolicy worldMovementPolicy) {
        this.worldNavigationSystem = requireNonNull(navigator);
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

    public void setBonusInactive(Bonus bonus) {
        if (bonus.optMovement().isPresent()) {
            worldNavigationSystem.setSpeed(bonus, 0);
        }
        bonus.optMoveAndJump().ifPresent(moveAndJump -> moveAndJump.pulse().reset());
    }

    public void startWandering(Bonus bonus, BonusRouteInfo routeInfo, float speed) {
        bonus.optMoveAndJump().ifPresent(moveAndJump -> {
            setRoute(bonus, routeInfo);
            worldNavigationSystem.clearTargetTile(bonus);
            worldNavigationSystem.setSpeed(bonus, speed);
            moveAndJump.pulse().restart();
        });
    }

    private void setRoute(Bonus bonus, BonusRouteInfo routeInfo) {
        requireNonNull(bonus);
        requireNonNull(routeInfo);

        if (routeInfo.waypoints().isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }

        final var route = new ArrayList<>(routeInfo.waypoints());
        final Direction initialDir = routeInfo.leftToRight() ? Direction.RIGHT : Direction.LEFT;
        worldNavigationSystem.placeAtTile(bonus, route.removeFirst());
        worldNavigationSystem.setMoveDir(bonus, initialDir);
        worldNavigationSystem.setWishDir(bonus, initialDir);

        final var steering = new RouteGuidedSteering(worldNavigationSystem, worldMovementPolicy, route);
        bonus.reqComp(BonusMoveAndJumpComp.class).setRouteNavigation(steering);
    }

    private void wanderMaze(MovementSystem motor, GameLevel level, Bonus bonus) {
        final BonusMoveAndJumpComp moveAndJump = bonus.reqComp(BonusMoveAndJumpComp.class);

        moveAndJump.routeNavigation().steer(bonus, level);

        final Vector2i tile = bonus.pos().tile();
        final boolean exitPortalReached = moveAndJump.routeNavigation().isRouteTraversed()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        if (!exitPortalReached) {
            worldNavigationSystem.navigateTowardsTarget(bonus, level, worldMovementPolicy);
            worldNavigationSystem.tryMovingOrTeleporting(motor, bonus, level, worldMovementPolicy);
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
