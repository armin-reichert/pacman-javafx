/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.MovingActor;
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
    public void steer(MovingActor movingActor, GameLevel level) {
        movingActor.navigateTowardsTarget(level);
        if (targetIndex == route.size()) {
            complete = true;
        } else if (movingActor.optTargetTile().isEmpty()) {
            movingActor.setTargetTile(currentTarget().tile());
            movingActor.navigateTowardsTarget(level);
            Logger.trace("New target tile for {}={}s", movingActor.name(), movingActor.optTargetTile().get());
        } else if (movingActor.tile().equals(currentTarget().tile())) {
            nextTarget(level, movingActor);
            Logger.trace("New target tile for {}={}", movingActor.name(), movingActor.optTargetTile().get());
        }
    }

    private void nextTarget(GameLevel level, MovingActor movingActor) {
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