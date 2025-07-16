/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;

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
    public void steer(MovingActor movingActor, GameLevel level) {
        if (movingActor.moveInfo().moved && !movingActor.isNewTileEntered()) {
            return;
        }
        var data = collectData(game);
        if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
            Logger.trace("\n{}", data);
        }
        takeAction(game, data);
    }

    private CollectedData collectData(GameModel game) {
        GameLevel level = game.level().orElseThrow();
        var data = new CollectedData();
        var pac = level.pac();
        Ghost hunterAhead = findHuntingGhostAhead(level); // Where is Hunter?
        if (hunterAhead != null) {
            data.hunterAhead = hunterAhead;
            data.hunterAheadDistance = pac.tile().manhattanDist(hunterAhead.tile());
        }
        Ghost hunterBehind = findHuntingGhostBehind(pac);
        if (hunterBehind != null) {
            data.hunterBehind = hunterBehind;
            data.hunterBehindDistance = pac.tile().manhattanDist(hunterBehind.tile());
        }
        data.frightenedGhosts = level.ghosts(GhostState.FRIGHTENED)
            .filter(ghost -> ghost.tile().manhattanDist(pac.tile()) <= CollectedData.MAX_GHOST_CHASE_DIST)
            .collect(Collectors.toList());
        data.frightenedGhostsDistance = data.frightenedGhosts.stream()
            .map(ghost -> (float)ghost.tile().manhattanDist(pac.tile())).collect(Collectors.toList());

        return data;
    }

    private void takeAction(GameModel game, CollectedData data) {
        GameLevel level = game.level().orElseThrow();
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
        if (pac.moveInfo().moved && !level.isIntersection(pac.tile()))
            return;

        if (!data.frightenedGhosts.isEmpty() && level.pac().powerTimer().remainingTicks() >= Globals.NUM_TICKS_PER_SEC) {
            Ghost prey = data.frightenedGhosts.getFirst();
            Logger.trace("Detected frightened ghost {} {} tiles away", prey.name(),
                prey.tile().manhattanDist(pac.tile()));
            pac.setTargetTile(prey.tile());
        } else if (isEdibleBonusNearPac(game, pac)) {
            Logger.trace("Active bonus detected, get it!");
            level.bonus().ifPresent(bonus -> pac.setTargetTile(tileAt(bonus.position())));
        } else {
            pac.setTargetTile(findTileFarthestFromGhosts(pac, findNearestFoodTiles(level)));
        }
        pac.optTargetTile().ifPresent(target -> {
            pac.navigateTowardsTarget(level);
            Logger.trace("Navigated towards {}, moveDir={} wishDir={}", pac.targetTile(), pac.moveDir(), pac.wishDir());
        });
    }

    private boolean isEdibleBonusNearPac(GameModel game, Pac pac) {
        GameLevel level = game.level().orElseThrow();
        if (level.bonus().isPresent()) {
            var bonus = level.bonus().get();
            var tile = tileAt(bonus.position());
            return bonus.state() == BonusState.EDIBLE
                && tile.manhattanDist(pac.tile()) <= CollectedData.MAX_BONUS_HARVEST_DIST;
        }
        return false;
    }

    private Ghost findHuntingGhostAhead(GameLevel level) {
        var pac = level.pac();
        Vector2i pacManTile = pac.tile();
        boolean energizerFound = false;
        for (int i = 1; i <= CollectedData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
            Vector2i ahead = pacManTile.plus(pac.moveDir().vector().scaled(i));
            if (!pac.canAccessTile(level, ahead)) {
                break;
            }
            if (level.isEnergizerPosition(ahead) && !level.tileContainsEatenFood(ahead)) {
                energizerFound = true;
            }
            var aheadLeft = ahead.plus(pac.moveDir().nextCounterClockwise().vector());
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

    private Ghost findHuntingGhostBehind(Pac pac) {
        GameLevel level = game.level().orElseThrow();
        var pacManTile = pac.tile();
        for (int i = 1; i <= CollectedData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
            var behind = pacManTile.plus(pac.moveDir().opposite().vector().scaled(i));
            if (!pac.canAccessTile(level, behind)) {
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
            if (pac.canAccessTile(level, neighbor)) {
                escapes.add(dir);
            }
        }
        for (Direction escape : escapes) {
            Vector2i escapeTile = pacManTile.plus(escape.vector());
            if (level.isTunnel(escapeTile)) {
                return escape;
            }
        }
        return escapes.isEmpty() ? null : escapes.getFirst();
    }

    private List<Vector2i> findNearestFoodTiles(GameLevel level) {
        long time = System.nanoTime();
        var pac = level.pac();
        List<Vector2i> foodTiles = new ArrayList<>();
        Vector2i pacManTile = pac.tile();
        float minDist = Float.MAX_VALUE;
        for (int x = 0; x < level.worldMap().numCols(); ++x) {
            for (int y = 0; y < level.worldMap().numRows(); ++y) {
                Vector2i tile = new Vector2i(x, y);
                if (!level.isFoodPosition(tile) || level.tileContainsEatenFood(tile)) {
                    continue;
                }
                if (level.isEnergizerPosition(tile) && level.pac().powerTimer().remainingTicks() > 2 * 60
                    && level.uneatenFoodCount() > 1) {
                    continue;
                }
                float dist = pacManTile.manhattanDist(tile);
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
            Logger.trace("\t{} ({} tiles away from Pac-Man, {} tiles away from ghosts)", t, t.manhattanDist(pacManTile),
                minDistanceFromGhosts(pac));
        }
        return foodTiles;
    }

    private Vector2i findTileFarthestFromGhosts(Pac pac, List<Vector2i> tiles) {
        Vector2i farthestTile = null;
        float maxDist = -1;
        for (Vector2i tile : tiles) {
            float dist = minDistanceFromGhosts(pac);
            if (dist > maxDist) {
                maxDist = dist;
                farthestTile = tile;
            }
        }
        return farthestTile;
    }

    private float minDistanceFromGhosts(Pac pac) {
        GameLevel level = game.level().orElseThrow();
        return (float) level.ghosts().map(Ghost::tile)
            .mapToDouble(pac.tile()::manhattanDist)
            .min().orElse(Float.MAX_VALUE);
    }
}