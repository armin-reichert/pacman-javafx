/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.WorldMovingActor;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteBasedSteering implements Steering {

    private final List<Waypoint> route;
    private int targetIndex;
    private boolean complete;

    public RouteBasedSteering(List<Waypoint> route) {
        this.route = requireNonNull(route);
        init();
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public void init() {
        targetIndex = 0;
        complete = false;
    }

    @Override
    public void steer(WorldMovingActor movingActor, GameLevel level) {
        movingActor.navigateTowardsTarget(level);
        if (targetIndex == route.size()) {
            complete = true;
        } else if (movingActor.targetTile().isEmpty()) {
            movingActor.setTargetTile(currentTarget().tile());
            movingActor.navigateTowardsTarget(level);
            Logger.trace("New target tile for {}={}s", movingActor.name(), movingActor.targetTile().get());
        } else if (movingActor.tile().equals(currentTarget().tile())) {
            nextTarget(level, movingActor);
            Logger.trace("New target tile for {}={}", movingActor.name(), movingActor.targetTile().get());
        }
    }

    private void nextTarget(GameLevel level, WorldMovingActor movingActor) {
        ++targetIndex;
        if (targetIndex < route.size()) {
            movingActor.setTargetTile(currentTarget().tile());
            movingActor.navigateTowardsTarget(level);
        }
    }

    private Waypoint currentTarget() {
        return route.get(targetIndex);
    }
}