/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;

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
    public void steer(Actor actor, GameContext gameContext) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (worldMovement.optTargetTile().isEmpty()) {
            worldMovement.setTargetTile(route.get(targetIndex));
        }
        else if (WorldMovementSystem.computeTile(actor).equals(route.get(targetIndex))) {
            selectNextTargetTile(gameContext, actor);
        }
        else {
            worldMovementSystem.navigateTowardsTarget(actor, gameContext);
        }
    }

    private void selectNextTargetTile(GameContext gameContext, Actor actor) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        ++targetIndex;
        if (targetIndex < route.size()) {
            worldMovement.setTargetTile(route.get(targetIndex));
            // The next line is important!
            worldMovementSystem.navigateTowardsTarget(actor, gameContext);
        }
    }
}