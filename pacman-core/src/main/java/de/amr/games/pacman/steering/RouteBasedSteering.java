/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.steering;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Creature;
import org.tinylog.Logger;

import java.util.List;

/**
 * Steers a creature to follow a given route.
 *
 * @author Armin Reichert
 */
public class RouteBasedSteering implements Steering {

    private List<NavPoint> route = List.of();
    private int targetIndex;
    private boolean complete;

    public RouteBasedSteering(List<NavPoint> route) {
        Globals.assertNotNull(route);
        setRoute(route);
    }

    public void setRoute(List<NavPoint> route) {
        this.route = route;
        init();
    }

    @Override
    public void init() {
        targetIndex = 0;
        complete = false;
    }

    @Override
    public void steer(Creature creature, GameWorld world) {
        creature.navigateTowardsTarget();
        if (targetIndex == route.size()) {
            complete = true;
        } else if (creature.targetTile().isEmpty()) {
            creature.setTargetTile(currentTarget().tile());
            creature.navigateTowardsTarget();
            Logger.trace("New target tile for {}={}s", creature.name(), creature.targetTile().get());
        } else if (creature.tile().equals(currentTarget().tile())) {
            nextTarget(creature, world);
            Logger.trace("New target tile for {}={}", creature.name(), creature.targetTile().get());
        }
    }

    public boolean isComplete() {
        return complete;
    }

    private void nextTarget(Creature creature, GameWorld world) {
        ++targetIndex;
        if (targetIndex < route.size()) {
            creature.setTargetTile(currentTarget().tile());
            creature.navigateTowardsTarget();
        }
    }

    private NavPoint currentTarget() {
        return route.get(targetIndex);
    }
}