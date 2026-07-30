/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.component.BonusJumpAnimation;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.steering.RouteGuidedActorSteering;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BonusJumpAnimationSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy worldMovementPolicy;

    public BonusJumpAnimationSystem(WorldNavigationSystem navigator, WorldMovementPolicy worldMovementPolicy) {
        this.navigator = requireNonNull(navigator);
        this.worldMovementPolicy = requireNonNull(worldMovementPolicy);
    }

    public void update(GameLevel level, Bonus bonus) {
        requireNonNull(level);
        requireNonNull(bonus);

        wanderMaze(level, bonus);
        jump(bonus);
    }

    public void reset(Bonus bonus) {
        requireNonNull(bonus);

        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);
        animation.pulse().reset();
    }

    public void start(Bonus bonus) {
        requireNonNull(bonus);

        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);
        animation.pulse().restart();
    }

    public void stop(Bonus bonus) {
        requireNonNull(bonus);

        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);
        animation.pulse().stop();
    }

    public void setMazeRoute(Bonus bonus, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(bonus);
        requireNonNull(waypoints);

        if (waypoints.isEmpty()) {
            Logger.error("Bonus route must not be empty");
            return;
        }

        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);

        final var route = new ArrayList<>(waypoints);
        final Vector2i first = route.removeFirst();

        navigator.placeAtTile(bonus, first);
        navigator.setMoveDir(bonus, leftToRight ? Direction.RIGHT : Direction.LEFT);
        navigator.setWishDir(bonus, leftToRight ? Direction.RIGHT : Direction.LEFT);

        animation.setRouteNavigation(new RouteGuidedActorSteering<>(navigator, worldMovementPolicy, route));
    }

    private void wanderMaze(GameLevel level, Bonus bonus) {
        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);
        animation.routeNavigation().steer(bonus, level);
        final Vector2i tile = WorldNavigationSystem.computeTile(bonus);
        boolean mazeExitReached = animation.routeNavigation().isRouteTraversed() || level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (!mazeExitReached) {
            navigator.navigateTowardsTarget(bonus, level, worldMovementPolicy);
            navigator.tryMovingOrTeleporting(bonus, level, worldMovementPolicy);
        }
        animation.setTargetReached(mazeExitReached);
    }

    private void jump(Bonus bonus) {
        final WorldNavigation navigation = bonus.assertComponent(WorldNavigation.class);
        final BonusJumpAnimation animation = bonus.assertComponent(BonusJumpAnimation.class);
        animation.pulse().triggerPulse();
        if (animation.pulse().pulseTriggered()) {
            float pixels = navigation.moveDir().isVertical() ? 3.0f : 2.0f;
            float dy = animation.pulse().state() == Pulse.State.ON ? -pixels : pixels;
            bonus.position().y += dy;
        }
    }
}
