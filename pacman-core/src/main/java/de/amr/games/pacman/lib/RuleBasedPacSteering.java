/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.World;
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
public class RuleBasedPacSteering implements Steering {

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


    private final GameModel game;

    public RuleBasedPacSteering(GameModel game) {
        this.game = game;
    }

    @Override
    public void steer(Creature creature, World world) {
        if (creature.lastMove().moved && !creature.isNewTileEntered()) {
            return;
        }
        var data = collectData(game);
        if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
            Logger.trace("\n{}", data);
        }
        takeAction(game, data);
    }

    private CollectedData collectData(GameModel game) {
        var data = new CollectedData();
        var pac = game.pac();
        Ghost hunterAhead = findHuntingGhostAhead(); // Where is Hunter?
        if (hunterAhead != null) {
            data.hunterAhead = hunterAhead;
            data.hunterAheadDistance = pac.tile().manhattanDistance(hunterAhead.tile());
        }
        Ghost hunterBehind = findHuntingGhostBehind();
        if (hunterBehind != null) {
            data.hunterBehind = hunterBehind;
            data.hunterBehindDistance = pac.tile().manhattanDistance(hunterBehind.tile());
        }
        data.frightenedGhosts = game.ghosts(GhostState.FRIGHTENED)
            .filter(ghost -> ghost.tile().manhattanDistance(pac.tile()) <= CollectedData.MAX_GHOST_CHASE_DIST)
            .collect(Collectors.toList());
        data.frightenedGhostsDistance = data.frightenedGhosts.stream()
            .map(ghost -> ghost.tile().manhattanDistance(pac.tile())).collect(Collectors.toList());

        return data;
    }

    private void takeAction(GameModel game, CollectedData data) {
        var pac = game.pac();
        if (data.hunterAhead != null) {
            Direction escapeDir;
            if (data.hunterBehind != null) {
                escapeDir = findEscapeDirectionExcluding(EnumSet.of(pac.moveDir(), pac.moveDir().opposite()));
                Logger.trace("Detected ghost {} behind, escape direction is {}", data.hunterAhead.name(), escapeDir);
            } else {
                escapeDir = findEscapeDirectionExcluding(EnumSet.of(pac.moveDir()));
                Logger.trace("Detected ghost {} ahead, escape direction is {}", data.hunterAhead.name(), escapeDir);
            }
            if (escapeDir != null) {
                pac.setWishDir(escapeDir);
            }
            return;
        }

        // when not escaping ghost, keep move direction at least until next intersection
        if (pac.lastMove().moved && !game.world().isIntersection(pac.tile()))
            return;

        if (!data.frightenedGhosts.isEmpty() && game.powerTimer().remaining() >= GameModel.FPS) {
            Ghost prey = data.frightenedGhosts.getFirst();
            Logger.trace("Detected frightened ghost {} {} tiles away", prey.name(),
                prey.tile().manhattanDistance(pac.tile()));
            pac.setTargetTile(prey.tile());
        } else if (isEdibleBonusNearPac(game, pac)) {
            Logger.trace("Active bonus detected, get it!");
            game.bonus().ifPresent(bonus -> pac.setTargetTile(tileAt(bonus.entity().position())));
        } else {
            pac.setTargetTile(findTileFarthestFromGhosts(findNearestFoodTiles()));
        }
        pac.targetTile().ifPresent(target -> {
            pac.navigateTowardsTarget();
            Logger.trace("Navigated towards {}, moveDir={} wishDir={}", pac.targetTile(), pac.moveDir(), pac.wishDir());
        });
    }

    private boolean isEdibleBonusNearPac(GameModel game, Pac pac) {
        if (game.bonus().isPresent()) {
            var bonus = game.bonus().get();
            var tile = tileAt(bonus.entity().position());
            return bonus.state() == Bonus.STATE_EDIBLE
                && tile.manhattanDistance(pac.tile()) <= CollectedData.MAX_BONUS_HARVEST_DIST;
        }
        return false;
    }

    private Ghost findHuntingGhostAhead() {
        var pac = game.pac();
        Vector2i pacManTile = pac.tile();
        boolean energizerFound = false;
        for (int i = 1; i <= CollectedData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
            Vector2i ahead = pacManTile.plus(pac.moveDir().vector().scaled(i));
            if (!pac.canAccessTile(ahead)) {
                break;
            }
            if (game.world().isEnergizerTile(ahead) && !game.world().hasEatenFoodAt(ahead)) {
                energizerFound = true;
            }
            var aheadLeft = ahead.plus(pac.moveDir().nextAntiClockwise().vector());
            var aheadRight = ahead.plus(pac.moveDir().nextClockwise().vector());
            Iterable<Ghost> huntingGhosts = game.ghosts(GhostState.HUNTING_PAC)::iterator;
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

    private Ghost findHuntingGhostBehind() {
        var pac = game.pac();
        var pacManTile = pac.tile();
        for (int i = 1; i <= CollectedData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
            var behind = pacManTile.plus(pac.moveDir().opposite().vector().scaled(i));
            if (!pac.canAccessTile(behind)) {
                break;
            }
            Iterable<Ghost> huntingGhosts = game.ghosts(GhostState.HUNTING_PAC)::iterator;
            for (Ghost ghost : huntingGhosts) {
                if (ghost.tile().equals(behind)) {
                    return ghost;
                }
            }
        }
        return null;
    }

    private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
        var pac = game.pac();
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
            if (game.world().isTunnel(escapeTile)) {
                return escape;
            }
        }
        return escapes.isEmpty() ? null : escapes.getFirst();
    }

    private List<Vector2i> findNearestFoodTiles() {
        long time = System.nanoTime();
        var pac = game.pac();
        List<Vector2i> foodTiles = new ArrayList<>();
        Vector2i pacManTile = pac.tile();
        float minDist = Float.MAX_VALUE;
        for (int x = 0; x < game.world().numCols(); ++x) {
            for (int y = 0; y < game.world().numRows(); ++y) {
                Vector2i tile = new Vector2i(x, y);
                if (!game.world().isFoodTile(tile) || game.world().hasEatenFoodAt(tile)) {
                    continue;
                }
                if (game.world().isEnergizerTile(tile) && game.powerTimer().remaining() > 2 * 60
                    && game.world().uneatenFoodCount() > 1) {
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
                minDistanceFromGhosts());
        }
        return foodTiles;
    }

    private Vector2i findTileFarthestFromGhosts(List<Vector2i> tiles) {
        Vector2i farthestTile = null;
        float maxDist = -1;
        for (Vector2i tile : tiles) {
            float dist = minDistanceFromGhosts();
            if (dist > maxDist) {
                maxDist = dist;
                farthestTile = tile;
            }
        }
        return farthestTile;
    }

    private float minDistanceFromGhosts() {
        return (float) game.ghosts().map(Ghost::tile)
            .mapToDouble(game.pac().tile()::manhattanDistance)
            .min().orElse(Float.MAX_VALUE);
    }
}