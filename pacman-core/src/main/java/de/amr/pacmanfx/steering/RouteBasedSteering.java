/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.level.GameLevel;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteBasedSteering implements Steering {

    private final List<Vector2i> route;
    private int targetIndex;
    private boolean routeTraversed;

    public RouteBasedSteering(List<Vector2i> route) {
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
    public void steer(MovingActor actor, GameLevel level) {
        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (actor.optTargetTile().isEmpty()) {
            actor.setTargetTile(route.get(targetIndex));
        }
        else if (actor.computeTile().equals(route.get(targetIndex))) {
            selectNextTargetTile(level, actor);
        }
        else {
            actor.navigateTowardsTarget(level);
        }
    }

    private void selectNextTargetTile(GameLevel level, MovingActor actor) {
        ++targetIndex;
        if (targetIndex < route.size()) {
            actor.setTargetTile(route.get(targetIndex));
            // The next line is important!
            actor.navigateTowardsTarget(level);
        }
    }
}