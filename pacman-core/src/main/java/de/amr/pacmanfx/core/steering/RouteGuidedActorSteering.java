/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteGuidedActorSteering<A extends Actor> implements Steering<A> {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy worldMovementPolicy;

    private final List<Vector2i> route;
    private int targetIndex;
    private boolean routeTraversed;

    public RouteGuidedActorSteering(WorldNavigationSystem navigator, WorldMovementPolicy worldMovementPolicy, List<Vector2i> route) {
        this.navigator = requireNonNull(navigator);
        this.worldMovementPolicy = requireNonNull(worldMovementPolicy);
        this.route = requireNonNull(route);
        init();
    }

    public boolean isRouteTraversed() {
        return routeTraversed;
    }

    @Override
    public void init() {
        targetIndex = 0;
        routeTraversed = false;
    }

    @Override
    public void steer(A actor, GameLevel level) {
        requireNonNull(actor);
        requireNonNull(level);

        final WorldNavigation navigation = actor.assertComponent(WorldNavigation.class);

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (navigation.optTargetTile().isEmpty()) {
            //TODO Use navigator method
            navigation.setTargetTile(route.get(targetIndex));
        }
        else if (WorldNavigationSystem.computeTile(actor).equals(route.get(targetIndex))) {
            selectNextTargetTile(level, actor);
        }
        else {
            navigator.navigateTowardsTarget(actor, level, worldMovementPolicy);
        }
    }

    private void selectNextTargetTile(GameLevel level, Actor actor) {
        final WorldNavigation navigation = actor.assertComponent(WorldNavigation.class);
        ++targetIndex;
        if (targetIndex < route.size()) {
            //TODO Use navigator method instead
            navigation.setTargetTile(route.get(targetIndex));
            // The next line is important!
            navigator.navigateTowardsTarget(actor, level, worldMovementPolicy);
        }
    }
}