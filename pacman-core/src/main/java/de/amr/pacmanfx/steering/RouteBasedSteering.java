/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.lib.math.Vector2b;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.MovingActor;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers an actor such that it follows a given route.
 */
public class RouteBasedSteering implements Steering {

    private final List<Vector2b> route;
    private int targetIndex;
    private boolean complete;

    public RouteBasedSteering(List<Vector2b> route) {
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
    public void steer(MovingActor movingActor, GameLevel gameLevel) {
        movingActor.navigateTowardsTarget(gameLevel);
        if (targetIndex == route.size()) {
            complete = true;
        } else if (movingActor.optTargetTile().isEmpty()) {
            movingActor.setTargetTile(currentTarget().toVector2i());
            movingActor.navigateTowardsTarget(gameLevel);
            Logger.trace("New target tile for {}={}s", movingActor.name(), movingActor.targetTile());
        } else if (movingActor.tile().equals(currentTarget().toVector2i())) {
            nextTarget(gameLevel, movingActor);
            Logger.trace("New target tile for {}={}", movingActor.name(), movingActor.targetTile());
        }
    }

    private void nextTarget(GameLevel gameLevel, MovingActor movingActor) {
        ++targetIndex;
        if (targetIndex < route.size()) {
            movingActor.setTargetTile(currentTarget().toVector2i());
            movingActor.navigateTowardsTarget(gameLevel);
        }
    }

    private Vector2b currentTarget() {
        return route.get(targetIndex);
    }
}