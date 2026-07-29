/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

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
        final WorldNavigationSystem navigator = gameContext.systems().navigator();
        final WorldNavigation worldNavigation = actor.assertComponent(WorldNavigation.class);

        final GameLevel level = gameContext.assertLevel();

        if (targetIndex == route.size()) {
            routeTraversed = true;
        }
        else if (worldNavigation.optTargetTile().isEmpty()) {
            worldNavigation.setTargetTile(route.get(targetIndex));
        }
        else if (WorldNavigationSystem.computeTile(actor).equals(route.get(targetIndex))) {
            selectNextTargetTile(navigator, level, actor);
        }
        else {
            navigator.navigateTowardsTarget(actor, level);
        }
    }

    private void selectNextTargetTile(WorldNavigationSystem navigator, GameLevel level, Actor actor) {
        final WorldNavigation navigation = actor.assertComponent(WorldNavigation.class);
        ++targetIndex;
        if (targetIndex < route.size()) {
            //TODO Use system method instead
            navigation.setTargetTile(route.get(targetIndex));

            // The next line is important!
            navigator.navigateTowardsTarget(actor, level);
        }
    }
}