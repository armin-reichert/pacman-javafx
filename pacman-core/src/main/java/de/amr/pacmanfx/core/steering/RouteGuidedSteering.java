/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.level.GameLevel;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteGuidedSteering implements Steering {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy worldMovementPolicy;

    private final List<Vector2i> route;
    private int targetIndex;
    private boolean routeTraversed;

    public RouteGuidedSteering(WorldNavigationSystem navigator, WorldMovementPolicy worldMovementPolicy, List<Vector2i> route) {
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
    public void steer(GameEntity gameEntity, GameLevel level) {
        requireNonNull(gameEntity);
        requireNonNull(level);

        final WorldNavigationComp navigation = gameEntity.requireComponent(WorldNavigationComp.class);

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (navigation.optTargetTile().isEmpty()) {
            //TODO Use navigator method
            navigation.setTargetTile(route.get(targetIndex));
        }
        else if (WorldNavigationSystem.computeTile(gameEntity).equals(route.get(targetIndex))) {
            selectNextTargetTile(level, gameEntity);
        }
        else {
            navigator.navigateTowardsTarget(gameEntity, level, worldMovementPolicy);
        }
    }

    private void selectNextTargetTile(GameLevel level, GameEntity actor) {
        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);
        ++targetIndex;
        if (targetIndex < route.size()) {
            //TODO Use navigator method instead
            navigation.setTargetTile(route.get(targetIndex));
            // The next line is important!
            navigator.navigateTowardsTarget(actor, level, worldMovementPolicy);
        }
    }
}