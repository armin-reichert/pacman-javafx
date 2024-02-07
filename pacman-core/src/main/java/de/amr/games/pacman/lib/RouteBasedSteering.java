/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;
import org.tinylog.Logger;

import java.util.List;

/**
 * Steering of a creature based on a route.
 * 
 * @author Armin Reichert
 */
public class RouteBasedSteering extends Steering {

	private List<NavigationPoint> route = List.of();
	private int targetIndex;
	private boolean complete;

	public RouteBasedSteering() {
	}

	public RouteBasedSteering(List<NavigationPoint> route) {
		setRoute(route);
	}

	public void setRoute(List<NavigationPoint> route) {
		this.route = route;
		init();
	}

	@Override
	public void init() {
		targetIndex = 0;
		complete = false;
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		guy.navigateTowardsTarget();
		if (targetIndex == route.size()) {
			complete = true;
		} else if (guy.targetTile().isEmpty()) {
			guy.setTargetTile(currentTarget().tile());
			guy.navigateTowardsTarget();
			Logger.trace("New target tile for {}={}s", guy.name(), guy.targetTile().get());
		} else if (guy.tile().equals(currentTarget().tile())) {
			nextTarget(guy);
			Logger.trace("New target tile for {}={}", guy.name(), guy.targetTile().get());
		}
	}

	public boolean isComplete() {
		return complete;
	}

	private void nextTarget(Creature guy) {
		++targetIndex;
		if (targetIndex < route.size()) {
			guy.setTargetTile(currentTarget().tile());
			guy.navigateTowardsTarget();
		}
	}

	private NavigationPoint currentTarget() {
		return route.get(targetIndex);
	}
}