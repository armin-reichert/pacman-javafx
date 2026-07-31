/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.comp.bonus.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.steering.RouteGuidedActorSteering;
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

    public void update(GameLevel level, Bonus bonus) {
        requireNonNull(level);
        requireNonNull(bonus);

        wanderMaze(level, bonus);
        jump(bonus);
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

        final var steering = new RouteGuidedActorSteering<Bonus>(navigator, worldMovementPolicy, route);
        bonus.requireComponent(BonusMoveAndJumpComp.class).setRouteNavigation(steering);
    }

    private void wanderMaze(GameLevel level, Bonus bonus) {
        final BonusMoveAndJumpComp moveAndJumpComp = bonus.requireComponent(BonusMoveAndJumpComp.class);
        moveAndJumpComp.routeNavigation().steer(bonus, level);
        final Vector2i tile = WorldNavigationSystem.computeTile(bonus);
        boolean exitPortalReached = moveAndJumpComp.routeNavigation().isRouteTraversed()
            || level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (!exitPortalReached) {
            navigator.navigateTowardsTarget(bonus, level, worldMovementPolicy);
            navigator.tryMovingOrTeleporting(bonus, level, worldMovementPolicy);
        }
        moveAndJumpComp.setTargetReached(exitPortalReached);
    }

    private void jump(Bonus bonus) {
        final WorldNavigationComp navigationComp   = bonus.requireComponent(WorldNavigationComp.class);
        final BonusMoveAndJumpComp moveAndJumpComp = bonus.requireComponent(BonusMoveAndJumpComp.class);
        moveAndJumpComp.pulse().triggerPulse();
        if (moveAndJumpComp.pulse().pulseTriggered()) {
            float jumpDelta = navigationComp.moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = moveAndJumpComp.pulse().state() == Pulse.State.ON ? -jumpDelta : jumpDelta;
            bonus.position().y += dy;
        }
    }
}
