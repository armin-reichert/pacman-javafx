/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.level.GameLevel;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteGuidedSteering<E extends GameEntity> implements Steering<E> {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy<E> worldMovementPolicy;

    private final List<Vector2i> route;
    private int targetIndex;
    private boolean routeTraversed;

    public RouteGuidedSteering(WorldNavigationSystem navigator, WorldMovementPolicy<E> worldMovementPolicy, List<Vector2i> route) {
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
    public void steer(E gameEntity, GameLevel level) {
        requireNonNull(gameEntity);
        requireNonNull(level);

        final WorldNavigationComp navigation = gameEntity.reqComp(WorldNavigationComp.class);

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (navigation.optTargetTile().isEmpty()) {
            //TODO Use navigator method
            navigation.setTargetTile(route.get(targetIndex));
        }
        else if (gameEntity.pos().tile().equals(route.get(targetIndex))) {
            selectNextTargetTile(level, gameEntity);
        }
        else {
            navigator.navigateActorTowardsCurrentTarget(gameEntity, level, worldMovementPolicy);
        }
    }

    private void selectNextTargetTile(GameLevel level, E actor) {
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        ++targetIndex;
        if (targetIndex < route.size()) {
            //TODO Use navigator method instead
            navigation.setTargetTile(route.get(targetIndex));
            // The next line is important!
            navigator.navigateActorTowardsCurrentTarget(actor, level, worldMovementPolicy);
        }
    }
}