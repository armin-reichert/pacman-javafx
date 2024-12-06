/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

public class ObstacleBuilder {

    private static final Vector2f SEG_CORNER_NW_UP   = v2f(HTS, -HTS);
    private static final Vector2f SEG_CORNER_NW_DOWN = SEG_CORNER_NW_UP.inverse();
    private static final Vector2f SEG_CORNER_SW_UP   = v2f(-HTS, -HTS);
    private static final Vector2f SEG_CORNER_SW_DOWN = SEG_CORNER_SW_UP.inverse();
    private static final Vector2f SEG_CORNER_SE_UP   = v2f(HTS, -HTS);
    private static final Vector2f SEG_CORNER_SE_DOWN = SEG_CORNER_SE_UP.inverse();
    private static final Vector2f SEG_CORNER_NE_UP   = v2f(-HTS, -HTS);
    private static final Vector2f SEG_CORNER_NE_DOWN = SEG_CORNER_NE_UP.inverse();

    private final TileMap terrain;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Set<Vector2i> exploredTiles = new HashSet<>();

    private Vector2i predecessorTile;
    private Vector2i cursorTile;

    public ObstacleBuilder(TileMap terrain) {
        this.terrain = terrain;
    }

    public List<Obstacle> buildObstacles() {
        Logger.info("Find obstacles in map ID={} size={}x{}", terrain.hashCode(), terrain.numRows(), terrain.numCols());
        // Note: order of detection matters! Otherwise, when searching for closed
        // obstacles first, each failed attempt must set its visited tile set to unvisited!
        terrain.tiles()
            .filter(tile -> tile.x() == 0 || tile.x() == terrain.numCols() - 1)
            .filter(Predicate.not(exploredTiles::contains))
            .map(tile -> buildOpenObstacle(tile, tile.x() == 0))
            .filter(Objects::nonNull)
            .forEach(obstacles::add);

        terrain.tiles()
            .filter(tile ->
                terrain.get(tile) == Tiles.CORNER_NW ||
                terrain.get(tile) == Tiles.DCORNER_NW ||
                terrain.get(tile) == Tiles.DCORNER_ANGULAR_NW) // house top-left corner
            .filter(Predicate.not(exploredTiles::contains))
            .map(this::buildClosedObstacle)
            .forEach(obstacles::add);

        Logger.info("Found {} obstacles", obstacles.size());
        optimizeObstacles();
        Logger.info("Optimized {} obstacles", obstacles.size());
        return obstacles;
    }

    private Obstacle buildClosedObstacle(Vector2i cornerNW) {
        predecessorTile = null;
        cursorTile = cornerNW;

        Vector2f startPoint = cornerNW.scaled((float)TS).plus(TS, HTS);
        byte startTileContent = terrain.get(cornerNW);
        boolean doubleWalls = startTileContent == Tiles.DCORNER_NW ||
            startTileContent == Tiles.DCORNER_ANGULAR_NW;
        Obstacle obstacle = new Obstacle(startPoint, doubleWalls);
        obstacle.addSegment(SEG_CORNER_NW_DOWN, true, startTileContent);
        moveCursor(DOWN);

        buildRestOfObstacle(obstacle, cornerNW, true);

        if (obstacle.isClosed()) {
            Logger.debug("Found closed obstacle, top-left tile={}, map ID={}:", cornerNW, terrain.hashCode());
            Logger.debug(obstacle);
        } else {
            Logger.error("Could not identify closed obstacle, top-left tile={}, map ID={}", cornerNW, terrain.hashCode());
        }
        return obstacle;
    }

    private Obstacle buildOpenObstacle(Vector2i startTile, boolean startsAtLeftBorder) {
        predecessorTile = null;
        cursorTile = startTile;

        Vector2f startPoint = startTile.scaled((float) TS).plus(startsAtLeftBorder ? 0 : TS, HTS);
        byte startTileContent = terrain.get(startTile);
        boolean doubleWall = Tiles.isDoubleWall(startTileContent);
        var obstacle = new Obstacle(startPoint, doubleWall);
        if (startTileContent == Tiles.DWALL_H) {
            Direction initialDir = startsAtLeftBorder ? RIGHT : LEFT;
            obstacle.addSegment(oneTile(initialDir), true, Tiles.DWALL_H);
            moveCursor(initialDir);
        }
        else if (startsAtLeftBorder && startTileContent == Tiles.DCORNER_SE) {
            obstacle.addSegment(SEG_CORNER_SE_UP, true, Tiles.DCORNER_SE);
            moveCursor(UP);
        }
        else if (startsAtLeftBorder && startTileContent == Tiles.DCORNER_NE) {
            obstacle.addSegment(SEG_CORNER_NE_DOWN, false, Tiles.DCORNER_NE);
            moveCursor(DOWN);
        }
        else if (!startsAtLeftBorder && startTileContent == Tiles.DCORNER_SW) {
            obstacle.addSegment(SEG_CORNER_SW_UP, false, Tiles.DCORNER_SW);
            moveCursor(UP);
        }
        else if (!startsAtLeftBorder && startTileContent == Tiles.DCORNER_NW) {
            obstacle.addSegment(SEG_CORNER_NW_DOWN, true, Tiles.DCORNER_NW);
            moveCursor(DOWN);
        }
        else {
            return null;
        }

        buildRestOfObstacle(obstacle, startTile, true);
        Logger.debug("Found open obstacle, start tile={}, segment count={}, map ID={}:",
            startTile, obstacle.segments().size(), terrain.hashCode());
        Logger.info(obstacle);
        return obstacle;
    }

    private void buildRestOfObstacle(Obstacle obstacle, Vector2i startTile, boolean ccw) {
        int bailout = 0;
        while (bailout < 2000) {
            ++bailout;
            if (exploredTiles.contains(cursorTile)) {
                break;
            }
            exploredTiles.add(cursorTile);
            switch (terrain.get(cursorTile)) {

                case Tiles.WALL_V, Tiles.DWALL_V -> {
                    if (isGoing(DOWN)) {
                        obstacle.addSegment(oneTile(DOWN), ccw, terrain.get(cursorTile));
                        moveCursor(DOWN);
                    } else if (isGoing(UP)) {
                        obstacle.addSegment(oneTile(UP), ccw, terrain.get(cursorTile));
                        moveCursor(UP);
                    } else {
                        //error
                    }
                }

                case Tiles.WALL_H, Tiles.DWALL_H, Tiles.DOOR -> {
                    if (isGoing(RIGHT)) {
                        obstacle.addSegment(oneTile(RIGHT), ccw, terrain.get(cursorTile));
                        moveCursor(RIGHT);
                    } else if (isGoing(LEFT)) {
                        obstacle.addSegment(oneTile(LEFT), ccw, terrain.get(cursorTile));
                        moveCursor(LEFT);
                    } else {
                        //error
                    }
                }

                case Tiles.CORNER_SW, Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> {
                    if (isGoing(DOWN)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SW_DOWN, ccw, terrain.get(cursorTile));
                        moveCursor(RIGHT);
                    } else if (isGoing(LEFT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SW_UP, ccw, terrain.get(cursorTile));
                        moveCursor(UP);
                    } else {
                        //error
                    }
                }

                case Tiles.CORNER_SE, Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> {
                    if (isGoing(DOWN)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SE_DOWN, ccw, terrain.get(cursorTile));
                        moveCursor(LEFT);
                    }
                    else if (isGoing(RIGHT)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SE_UP, ccw, terrain.get(cursorTile));
                        moveCursor(UP);
                    }
                    else {
                        //error
                    }
                }

                case Tiles.CORNER_NE, Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> {
                    if (isGoing(UP)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_NE_UP, ccw, terrain.get(cursorTile));
                        moveCursor(LEFT);
                    }
                    else if (isGoing(RIGHT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NE_DOWN, ccw, terrain.get(cursorTile));
                        moveCursor(DOWN);
                    }
                    else {
                        //error
                    }
                }

                case Tiles.CORNER_NW, Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> {
                    if (isGoing(UP)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NW_UP, ccw, terrain.get(cursorTile));
                        moveCursor(RIGHT);
                    }
                    else if (isGoing(LEFT)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_NW_DOWN, ccw, terrain.get(cursorTile));
                        moveCursor(DOWN);
                    }
                    else {
                        //error
                    }
                }
            }

            if (cursorTile.equals(startTile) || terrain.outOfBounds(cursorTile)) {
                break;
            }
        }
    }

    private boolean isGoing(Direction dir) {
        return predecessorTile.plus(dir.vector()).equals(cursorTile);
    }

    private void moveCursor(Direction dir) {
        predecessorTile = cursorTile;
        cursorTile = cursorTile.plus(dir.vector());
    }

    private Vector2f oneTile(Direction dir) {
        return dir.vector().scaled((float) TS);
    }

    private void optimizeObstacles() {
        List<Obstacle> optimizedObstacles = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            Obstacle optimized = new Obstacle(obstacle.startPoint(), obstacle.hasDoubleWalls());
            optimizedObstacles.add(optimized);
            boolean merging = false;
            Vector2f mergedVector = null;
            boolean mergedCCW = false;
            byte mergedMapContent = -1;
            for (int i = 0; i < obstacle.numSegments(); ++i) {
                Obstacle.Segment segment = obstacle.segment(i);
                if (segment.isStraightLine()) {
                    if (merging) { // continue merging
                        mergedVector = mergedVector.plus(segment.vector());
                    } else { // start merging
                        mergedVector = segment.vector();
                        mergedCCW = segment.ccw();
                        mergedMapContent = segment.mapContent();
                        merging = true;
                    }
                }
                else {
                    if (merging) {
                        optimized.addSegment(mergedVector, mergedCCW, mergedMapContent);
                        merging = false;
                    }
                    optimized.addSegment(segment.vector(), segment.ccw(), segment.mapContent());
                }
            }
            if (merging) {
                optimized.addSegment(mergedVector, mergedCCW, mergedMapContent);
            }
        }
        obstacles.clear();
        obstacles.addAll(optimizedObstacles);
    }
}