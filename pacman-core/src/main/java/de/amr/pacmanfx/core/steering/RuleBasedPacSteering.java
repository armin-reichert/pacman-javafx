/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.steering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.FoodLayer;
import de.amr.pacmanfx.core.model.world.WorldMap;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pac-Man steering based on a set of rules.
 *
 * @author Armin Reichert
 */
public class RuleBasedPacSteering implements Steering {

    private static class CollectedData {

        static final int MAX_GHOST_AHEAD_DETECTION_DIST = 3; // tiles
        static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
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
    public void steer(Actor actor, GameContext gameContext) {
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (worldMovement.info.moved && !worldMovement.isNewTileEntered()) {
            return;
        }
        var data = collectData(gameContext);
        if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
            Logger.trace("\n{}", data);
        }
        takeAction(gameContext, data);
    }

    private CollectedData collectData(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final Vector2i pacTile = WorldMovementSystem.computeTile(pac);
        
        var data = new CollectedData();

        final Ghost hunterAhead = findHuntingGhostAhead(gameContext); // Where is Hunter?
        if (hunterAhead != null) {
            final Vector2i tile = WorldMovementSystem.computeTile(hunterAhead);
            data.hunterAhead = hunterAhead;
            data.hunterAheadDistance = pacTile.manhattanDist(tile);
        }
        
        final Ghost hunterBehind = findHuntingGhostBehind(gameContext, pac);
        if (hunterBehind != null) {
            final Vector2i tile = WorldMovementSystem.computeTile(hunterBehind);
            data.hunterBehind = hunterBehind;
            data.hunterBehindDistance = pacTile.manhattanDist(tile);
        }

        data.frightenedGhosts = level.ghostsInState(GhostState.FRIGHTENED)
            .filter(ghost -> WorldMovementSystem.computeTile(ghost).manhattanDist(pacTile) <= CollectedData.MAX_GHOST_CHASE_DIST)
            .collect(Collectors.toList());

        data.frightenedGhostsDistance = data.frightenedGhosts.stream()
            .map(ghost -> (float) WorldMovementSystem.computeTile(ghost).manhattanDist(pacTile)).collect(Collectors.toList());

        return data;
    }

    private void takeAction(GameContext gameContext, CollectedData data) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final Vector2i pacTile = WorldMovementSystem.computeTile(pac);
        final WorldMovement worldMovement = pac.worldMovement();
        
        if (data.hunterAhead != null) {
            Direction escapeDir;
            if (data.hunterBehind != null) {
                escapeDir = findEscapeDirectionExcluding(gameContext, EnumSet.of(worldMovement.moveDir(), worldMovement.moveDir().opposite()));
                Logger.trace("Detected ghost {} behind, escape direction is {}", data.hunterAhead.name(), escapeDir);
            } else {
                escapeDir = findEscapeDirectionExcluding(gameContext, EnumSet.of(worldMovement.moveDir()));
                Logger.trace("Detected ghost {} ahead, escape direction is {}", data.hunterAhead.name(), escapeDir);
            }
            if (escapeDir != null) {
                worldMovementSystem.setWishDir(pac, escapeDir);
            }
            return;
        }

        // when not escaping ghost, keep move direction at least until next intersection
        if (worldMovement.info.moved && !level.worldMap().terrainLayer().isIntersection(pacTile))
            return;

        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPowerSystem;
        if (!data.frightenedGhosts.isEmpty()
            && pacPowerSystem.powerTicksRemaining(pac) >= GameConstants.SIMULATION_FPS) {
            final Ghost prey = data.frightenedGhosts.getFirst();
            final Vector2i preyTile = WorldMovementSystem.computeTile(prey);
            Logger.trace("Detected frightened ghost {} {} tiles away", prey.name(), preyTile.manhattanDist(pacTile));
            worldMovement.setTargetTile(preyTile);
        } 
        else if (isEdibleBonusNearPac(gameContext, pac)) {
            Logger.trace("Active bonus detected, get it!");
            level.optBonus().ifPresent(bonus -> worldMovement.setTargetTile(
                WorldMap.computeTileAt(bonus.position().x, bonus.position().y)));
        } 
        else {
            worldMovement.setTargetTile(findTileFarthestFromGhosts(gameContext, pac, findNearestFoodTiles(gameContext)));
        }
        worldMovement.optTargetTile().ifPresent(_ -> {
            worldMovementSystem.navigateTowardsTarget(pac, gameContext);
            Logger.trace("Navigated towards {}, moveDir={} wishDir={}",
                worldMovement.targetTile(), worldMovement.moveDir(), worldMovement.wishDir());
        });
    }

    private boolean isEdibleBonusNearPac(GameContext gameContext, Pac pac) {
        final GameLevel level = gameContext.assertLevel();
        final Vector2i pacTile = WorldMovementSystem.computeTile(pac);

        if (level.optBonus().isPresent()) {
            final Bonus bonus = level.optBonus().get();
            final Vector2i bonusTile = WorldMovementSystem.computeTile(bonus);
            return bonus.state() == BonusState.EDIBLE
                && bonusTile.manhattanDist(pacTile) <= CollectedData.MAX_BONUS_HARVEST_DIST;
        }
        return false;
    }

    private Ghost findHuntingGhostAhead(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        final WorldMovement worldMovement = pac.worldMovement();
        final WorldMovementPolicy worldMovementPolicy = pac.assertComponent(WorldMovementPolicy.class);

        final Vector2i pacManTile = WorldMovementSystem.computeTile(pac);

        boolean energizerFound = false;
        FoodLayer foodLayer = level.worldMap().foodLayer();
        for (int i = 1; i <= CollectedData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
            Vector2i ahead = pacManTile.plus(worldMovement.moveDir().vector().scaled(i));
            if (!worldMovementPolicy.canAccessTile(gameContext, pac, ahead)) {
                break;
            }
            if (foodLayer.isEnergizerTile(ahead) && !foodLayer.hasEatenFoodAtTile(ahead)) {
                energizerFound = true;
            }
            final Vector2i aheadLeft = ahead.plus(worldMovement.moveDir().nextCounterClockwise().vector());
            final Vector2i aheadRight = ahead.plus(worldMovement.moveDir().nextClockwise().vector());
            final List<Ghost> huntingGhosts = level.ghostsInState(GhostState.HUNTING_PAC).toList();
            for (var ghost : huntingGhosts) {
                final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
                if (ghostTile.equals(ahead) || ghostTile.equals(aheadLeft) || ghostTile.equals(aheadRight)) {
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

    private Ghost findHuntingGhostBehind(GameContext gameContext, Pac pac) {
        final WorldMovement worldMovement = pac.worldMovement();
        final WorldMovementPolicy worldMovementPolicy = pac.assertComponent(WorldMovementPolicy.class);

        final GameLevel level = gameContext.assertLevel();
        final Vector2i pacManTile = WorldMovementSystem.computeTile(pac);

        for (int i = 1; i <= CollectedData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
            var behind = pacManTile.plus(worldMovement.moveDir().opposite().vector().scaled(i));
            if (!worldMovementPolicy.canAccessTile(gameContext, pac, behind)) {
                break;
            }
            Iterable<Ghost> huntingGhosts = level.ghostsInState(GhostState.HUNTING_PAC)::iterator;
            for (Ghost ghost : huntingGhosts) {
                final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
                if (ghostTile.equals(behind)) {
                    return ghost;
                }
            }
        }
        return null;
    }

    private Direction findEscapeDirectionExcluding(GameContext gameContext, Collection<Direction> forbidden) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        final WorldMovementPolicy worldMovementPolicy = pac.assertComponent(WorldMovementPolicy.class);

        final Vector2i pacTile = WorldMovementSystem.computeTile(pac);
        final List<Direction> escapes = new ArrayList<>(4);
        for (Direction dir : Direction.shuffled()) {
            if (forbidden.contains(dir)) {
                continue;
            }
            Vector2i neighbor = pacTile.plus(dir.vector());
            if (worldMovementPolicy.canAccessTile(gameContext, pac, neighbor)) {
                escapes.add(dir);
            }
        }
        for (Direction escape : escapes) {
            Vector2i escapeTile = pacTile.plus(escape.vector());
            if (level.worldMap().terrainLayer().isTunnel(escapeTile)) {
                return escape;
            }
        }
        return escapes.isEmpty() ? null : escapes.getFirst();
    }

    private List<Vector2i> findNearestFoodTiles(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final WorldMap worldMap = level.worldMap();
        final FoodLayer foodLayer = worldMap.foodLayer();
        final Pac pac = level.entities().pac();
        final Vector2i pacManTile = WorldMovementSystem.computeTile(pac);
        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPowerSystem;
        final long powerTicksRemaining = pacPowerSystem.powerTicksRemaining(pac);
        final boolean enoughTimeLeft = powerTicksRemaining > 2L * GameConstants.SIMULATION_FPS;
        final List<Vector2i> foodTiles = new ArrayList<>();

        long time = System.nanoTime();

        float minDist = Float.MAX_VALUE;
        for (int x = 0; x < worldMap.numCols(); ++x) {
            for (int y = 0; y < worldMap.numRows(); ++y) {
                final Vector2i tile = new Vector2i(x, y);
                if (!foodLayer.isFoodTile(tile) || foodLayer.hasEatenFoodAtTile(tile)) {
                    continue;
                }
                if (foodLayer.isEnergizerTile(tile)
                    && enoughTimeLeft
                    && foodLayer.remainingFoodCount() > 1) {
                    continue;
                }
                float dist = pacManTile.manhattanDist(tile);
                if (dist < minDist) {
                    minDist = dist;
                    foodTiles.clear();
                    foodTiles.add(tile);
                }
                else if (dist == minDist) {
                    foodTiles.add(tile);
                }
            }
        }

        time = System.nanoTime() - time;
        Logger.trace("Nearest food tiles from Pac-Man location {}: (time {} millis)", pacManTile, time / 1_000_000f);

        return foodTiles;
    }

    private Vector2i findTileFarthestFromGhosts(GameContext gameContext, Pac pac, List<Vector2i> tiles) {
        Vector2i farthestTile = null;
        float maxDist = -1;
        for (Vector2i tile : tiles) {
            float dist = minDistanceFromGhosts(gameContext, pac);
            if (dist > maxDist) {
                maxDist = dist;
                farthestTile = tile;
            }
        }
        return farthestTile;
    }

    private float minDistanceFromGhosts(GameContext gameContext, Pac pac) {
        final GameLevel level = gameContext.assertLevel();
        final Vector2i pacTile = WorldMovementSystem.computeTile(pac);

        return (float) level.entities().ghosts().stream().map(WorldMovementSystem::computeTile)
            .mapToDouble(pacTile::manhattanDist)
            .min()
            .orElse(Float.MAX_VALUE);
    }
}