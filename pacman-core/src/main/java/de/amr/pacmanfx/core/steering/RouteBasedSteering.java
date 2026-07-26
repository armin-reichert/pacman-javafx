/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.core.model.level.GameLevel;

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
    public void steer(Actor actor, GameLevel level) {
        final WorldMovement mazeMovement = actor.assertComponent(WorldMovement.class);

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (mazeMovement.optTargetTile().isEmpty()) {
            mazeMovement.setTargetTile(route.get(targetIndex));
        }
        else if (GameContext.SYSTEMS.worldMovementSystem.computeTile(actor).equals(route.get(targetIndex))) {
            selectNextTargetTile(level, actor);
        }
        else {
            GameContext.SYSTEMS.worldMovementSystem.navigateTowardsTarget(actor, level);
        }
    }

    private void selectNextTargetTile(GameLevel level, Actor actor) {
        final WorldMovement mazeMovement = actor.assertComponent(WorldMovement.class);

        ++targetIndex;
        if (targetIndex < route.size()) {
            mazeMovement.setTargetTile(route.get(targetIndex));
            // The next line is important!
            GameContext.SYSTEMS.worldMovementSystem.navigateTowardsTarget(actor, level);
        }
    }
}