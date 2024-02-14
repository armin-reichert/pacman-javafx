/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.amr.games.pacman.lib.Globals.tileAt;

/**
 * Pac-Man steering based on a set of rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedSteering extends Steering {

	private static class CollectedData {

		static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
		static final int MAX_GHOST_BEHIND_DETECTION_DIST = 1; // tiles
		static final int MAX_GHOST_CHASE_DIST = 10; // tiles
		static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

		Ghost hunterAhead;
		float hunterAheadDistance;
		Ghost hunterBehind;
		float hunterBehindDistance;
		List<Ghost> frightenedGhosts;
		List<Float> frightenedGhostsDistance;

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder("-- Begin autopilot info\n");
			if (hunterAhead != null) {
				s.append(String.format("Hunter ahead:  %s, distance: %.2g\n", hunterAhead.name(), hunterAheadDistance));
			} else {
				s.append("No hunter ahead\n");
			}
			if (hunterBehind != null) {
				s.append(String.format("Hunter behind: %s, distance: %.2g\n", hunterBehind.name(), hunterBehindDistance));
			} else {
				s.append("No hunter behind\n");
			}
			for (int i = 0; i < frightenedGhosts.size(); ++i) {
				Ghost ghost = frightenedGhosts.get(i);
				s.append(String.format("Prey: %s, distance: %.2g\n", ghost.name(), frightenedGhostsDistance.get(i)));
			}
			if (frightenedGhosts.isEmpty()) {
				s.append("No prey\n");
			}
			s.append("-- End autopilot info");
			return s.toString();
		}
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (guy.moved() && !guy.isNewTileEntered()) {
			return;
		}
		var data = collectData(level);
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			Logger.trace("\n{}", data);
		}
		takeAction(level, data);
	}

	private CollectedData collectData(GameLevel level) {
		var pac = level.pac();
		var data = new CollectedData();
		Ghost hunterAhead = findHuntingGhostAhead(level); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = pac.tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind(level);
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = pac.tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = level.ghosts(GhostState.FRIGHTENED)
				.filter(ghost -> ghost.tile().manhattanDistance(pac.tile()) <= CollectedData.MAX_GHOST_CHASE_DIST)
				.collect(Collectors.toList());
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(pac.tile())).collect(Collectors.toList());
		return data;
	}

	private void takeAction(GameLevel level, CollectedData data) {
		var pac = level.pac();
		if (data.hunterAhead != null) {
			Direction escapeDir;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(level, EnumSet.of(pac.moveDir(), pac.moveDir().opposite()));
				Logger.trace("Detected ghost {} behind, escape direction is {}", data.hunterAhead.name(), escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(level, EnumSet.of(pac.moveDir()));
				Logger.trace("Detected ghost {} ahead, escape direction is {}", data.hunterAhead.name(), escapeDir);
			}
			if (escapeDir != null) {
				pac.setWishDir(escapeDir);
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (pac.moved() && !level.world().isIntersection(pac.tile()))
			return;

		if (!data.frightenedGhosts.isEmpty() && pac.powerTimer().remaining() >= GameModel.FPS) {
			Ghost prey = data.frightenedGhosts.get(0);
			Logger.trace("Detected frightened ghost {} {} tiles away", prey.name(),
					prey.tile().manhattanDistance(pac.tile()));
			pac.setTargetTile(prey.tile());
		} else if (isEdibleBonusNearPac(level, pac)) {
			Logger.trace("Active bonus detected, get it!");
			level.bonus().ifPresent(bonus -> pac.setTargetTile(tileAt(bonus.entity().position())));
		} else {
			pac.setTargetTile(findTileFarthestFromGhosts(level, findNearestFoodTiles(level)));
		}
		pac.navigateTowardsTarget();
	}

	private boolean isEdibleBonusNearPac(GameLevel level, Pac pac) {
		var optBonus = level.bonus();
		if (optBonus.isPresent()) {
			var bonus = optBonus.get();
			var tile = tileAt(bonus.entity().position());
			return bonus.state() == Bonus.STATE_EDIBLE
					&& tile.manhattanDistance(pac.tile()) <= CollectedData.MAX_BONUS_HARVEST_DIST;
		}
		return false;
	}

	private Ghost findHuntingGhostAhead(GameLevel level) {
		var pac = level.pac();
		Vector2i pacManTile = pac.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= CollectedData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			Vector2i ahead = pacManTile.plus(pac.moveDir().vector().scaled(i));
			if (!pac.canAccessTile(ahead)) {
				break;
			}
			if (level.world().isEnergizerTile(ahead) && !level.world().hasEatenFoodAt(ahead)) {
				energizerFound = true;
			}
			var aheadLeft = ahead.plus(pac.moveDir().nextAntiClockwise().vector());
			var aheadRight = ahead.plus(pac.moveDir().nextClockwise().vector());
			Iterable<Ghost> huntingGhosts = level.ghosts(GhostState.HUNTING_PAC)::iterator;
			for (var ghost : huntingGhosts) {
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					if (energizerFound) {
						Logger.trace("Ignore hunting ghost ahead, energizer comes first!");
						return null;
					}
					return ghost;
				}
			}
		}
		return null;
	}

	private Ghost findHuntingGhostBehind(GameLevel level) {
		var pac = level.pac();
		var pacManTile = pac.tile();
		for (int i = 1; i <= CollectedData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			var behind = pacManTile.plus(pac.moveDir().opposite().vector().scaled(i));
			if (!pac.canAccessTile(behind)) {
				break;
			}
			Iterable<Ghost> huntingGhosts = level.ghosts(GhostState.HUNTING_PAC)::iterator;
			for (Ghost ghost : huntingGhosts) {
				if (ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(GameLevel level, Collection<Direction> forbidden) {
		var pac = level.pac();
		Vector2i pacManTile = pac.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			Vector2i neighbor = pacManTile.plus(dir.vector());
			if (pac.canAccessTile(neighbor)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			Vector2i escapeTile = pacManTile.plus(escape.vector());
			if (level.world().isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<Vector2i> findNearestFoodTiles(GameLevel level) {
		long time = System.nanoTime();
		var pac = level.pac();
		List<Vector2i> foodTiles = new ArrayList<>();
		Vector2i pacManTile = pac.tile();
		float minDist = Float.MAX_VALUE;
		for (int x = 0; x < level.world().numCols(); ++x) {
			for (int y = 0; y < level.world().numRows(); ++y) {
				Vector2i tile = new Vector2i(x, y);
				if (!level.world().isFoodTile(tile) || level.world().hasEatenFoodAt(tile)) {
					continue;
				}
				if (level.world().isEnergizerTile(tile) && pac.powerTimer().remaining() > 2 * 60
						&& level.world().uneatenFoodCount() > 1) {
					continue;
				}
				float dist = pacManTile.manhattanDistance(tile);
				if (dist < minDist) {
					minDist = dist;
					foodTiles.clear();
					foodTiles.add(tile);
				} else if (dist == minDist) {
					foodTiles.add(tile);
				}
			}
		}
		time = System.nanoTime() - time;
		Logger.trace("Nearest food tiles from Pac-Man location {}: (time {} millis)", pacManTile, time / 1_000_000f);
		for (Vector2i t : foodTiles) {
			Logger.trace("\t{} ({} tiles away from Pac-Man, {} tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(level));
		}
		return foodTiles;
	}

	private Vector2i findTileFarthestFromGhosts(GameLevel level, List<Vector2i> tiles) {
		Vector2i farthestTile = null;
		float maxDist = -1;
		for (Vector2i tile : tiles) {
			float dist = minDistanceFromGhosts(level);
			if (dist > maxDist) {
				maxDist = dist;
				farthestTile = tile;
			}
		}
		return farthestTile;
	}

	private float minDistanceFromGhosts(GameLevel level) {
		return (float) level.ghosts().map(Ghost::tile)
			.mapToDouble(level.pac().tile()::manhattanDistance)
			.min().orElse(Float.MAX_VALUE);
	}
}