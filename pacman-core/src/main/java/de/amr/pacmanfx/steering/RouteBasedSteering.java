/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Creature;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Steers a creature to follow a given route.
 *
 * @author Armin Reichert
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
    public void steer(Creature creature, GameLevel level) {
        creature.navigateTowardsTarget();
        if (targetIndex == route.size()) {
            complete = true;
        } else if (creature.targetTile().isEmpty()) {
            creature.setTargetTile(currentTarget().tile());
            creature.navigateTowardsTarget();
            Logger.trace("New target tile for {}={}s", creature.name(), creature.targetTile().get());
        } else if (creature.tile().equals(currentTarget().tile())) {
            nextTarget(creature);
            Logger.trace("New target tile for {}={}", creature.name(), creature.targetTile().get());
        }
    }

    private void nextTarget(Creature creature) {
        ++targetIndex;
        if (targetIndex < route.size()) {
            creature.setTargetTile(currentTarget().tile());
            creature.navigateTowardsTarget();
        }
    }

    private Waypoint currentTarget() {
        return route.get(targetIndex);
    }
}