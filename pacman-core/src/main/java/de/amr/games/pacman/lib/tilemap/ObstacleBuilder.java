/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TerrainTiles.isDoubleWall;
import static java.util.function.Predicate.not;

/**
 * Analyzes a terrain tile map and creates the obstacle list. An obstacle is in essence a
 * list of vectors defining the contour path of the obstacle. The path starts at the NW corner
 * of the obstacle and moves in counter-clockwise order around it.
 */
public class ObstacleBuilder {

    // Public API
    public static Set<Obstacle> buildObstacles(TileMap terrain, List<Vector2i> tilesWithErrors) {
        return new ObstacleBuilder(terrain).buildObstacles(tilesWithErrors);
    }

    static class Cursor {
        private Vector2i prevTile, currentTile;
        
        Cursor(Vector2i tile) {
            prevTile = null;
            currentTile = tile;
        }
        
        boolean points(Direction dir) {
            return prevTile.plus(dir.vector()).equals(currentTile);
        }

        void move(Direction dir) {
            prevTile = currentTile;
            currentTile = currentTile.plus(dir.vector());
        }
    }

    // Corners are represented by "diagonal" vectors
    private static final Vector2i SEG_CORNER_NW_UP   = vec_2i(HTS, -HTS);
    private static final Vector2i SEG_CORNER_NW_DOWN = SEG_CORNER_NW_UP.inverse();
    private static final Vector2i SEG_CORNER_SW_UP   = vec_2i(-HTS, -HTS);
    private static final Vector2i SEG_CORNER_SW_DOWN = SEG_CORNER_SW_UP.inverse();
    private static final Vector2i SEG_CORNER_SE_UP   = vec_2i(HTS, -HTS);
    private static final Vector2i SEG_CORNER_SE_DOWN = SEG_CORNER_SE_UP.inverse();
    private static final Vector2i SEG_CORNER_NE_UP   = vec_2i(-HTS, -HTS);
    private static final Vector2i SEG_CORNER_NE_DOWN = SEG_CORNER_NE_UP.inverse();

    private final TileMap terrain;
    private final BitSet exploredTiles = new BitSet();
    private Cursor cursor;

    private ObstacleBuilder(TileMap terrain) {
        this.terrain = terrain;
    }

    private boolean isExplored(Vector2i tile) {
        return exploredTiles.get(terrain.index(tile));
    }

    private void setExplored(Vector2i tile) {
        exploredTiles.set(terrain.index(tile));
    }

    private Set<Obstacle> buildObstacles(List<Vector2i> tilesWithErrors) {
        Logger.debug("Find obstacles in map ID={} size={}x{}", terrain.hashCode(), terrain.numRows(), terrain.numCols());

        tilesWithErrors.clear();
        exploredTiles.clear();

        Set<Obstacle> obstacles = new HashSet<>();

        // Note: order of detection matters! Otherwise, when searching for closed
        // obstacles first, each failed attempt must set its visited tile set to unvisited!
        terrain.tiles()
            .filter(not(this::isExplored))
            .filter(tile -> tile.x() == 0 || tile.x() == terrain.numCols() - 1)
            .map(borderTile -> buildOpenObstacle(borderTile, borderTile.x() == 0, tilesWithErrors))
            .filter(Objects::nonNull)
            .forEach(obstacles::add);

        terrain.tiles()
            .filter(not(this::isExplored))
            .filter(tile ->
                terrain.get(tile) == TerrainTiles.CORNER_NW ||
                terrain.get(tile) == TerrainTiles.DCORNER_ANGULAR_NW) // house top-left corner
            .map(cornerNW -> buildClosedObstacle(cornerNW, tilesWithErrors))
            .forEach(obstacles::add);

        Logger.debug("Found {} obstacles", obstacles.size());

        return optimize(obstacles);
    }

    private Obstacle buildClosedObstacle(Vector2i cornerNW, List<Vector2i> tilesWithErrors) {
        Vector2i startPoint = cornerNW.scaled(TS).plus(TS, HTS);
        byte startTileContent = terrain.get(cornerNW);
        Obstacle obstacle = new Obstacle(startPoint);
        obstacle.addSegment(SEG_CORNER_NW_DOWN, true, startTileContent);
        cursor = new Cursor(cornerNW);
        cursor.move(DOWN);
        setExplored(cornerNW);
        buildRestOfObstacle(obstacle, cornerNW, true, tilesWithErrors);
        if (obstacle.isClosed()) {
            Logger.debug("Closed obstacle, top-left tile={}, map ID={}:", cornerNW, terrain.hashCode());
            Logger.debug(obstacle);
        }
        return obstacle;
    }

    private Obstacle buildOpenObstacle(Vector2i startTile, boolean startsAtLeftBorder, List<Vector2i> tilesWithErrors) {
        Vector2i startPoint = startTile.scaled(TS).plus(startsAtLeftBorder ? 0 : TS, HTS);
        byte startTileContent = terrain.get(startTile);
        var obstacle = new Obstacle(startPoint);
        cursor = new Cursor(startTile);
        if (startTileContent == TerrainTiles.WALL_H) {
            Direction startDir = startsAtLeftBorder ? RIGHT : LEFT;
            obstacle.addSegment(scaledVector(startDir, TS), true, TerrainTiles.WALL_H);
            cursor.move(startDir);
        }
        else if (startsAtLeftBorder && startTileContent == TerrainTiles.CORNER_SE) {
            obstacle.addSegment(SEG_CORNER_SE_UP, true, TerrainTiles.CORNER_SE);
            cursor.move(UP);
        }
        else if (startsAtLeftBorder && startTileContent == TerrainTiles.CORNER_NE) {
            obstacle.addSegment(SEG_CORNER_NE_DOWN, false, TerrainTiles.CORNER_NE);
            cursor.move(DOWN);
        }
        else if (!startsAtLeftBorder && startTileContent == TerrainTiles.CORNER_SW) {
            obstacle.addSegment(SEG_CORNER_SW_UP, false, TerrainTiles.CORNER_SW);
            cursor.move(UP);
        }
        else if (!startsAtLeftBorder && startTileContent == TerrainTiles.CORNER_NW) {
            obstacle.addSegment(SEG_CORNER_NW_DOWN, true, TerrainTiles.CORNER_NW);
            cursor.move(DOWN);
        }
        else {
            return null;
        }
        setExplored(startTile);
        buildRestOfObstacle(obstacle, startTile, obstacle.segment(0).ccw(), tilesWithErrors);
        Logger.debug("Open obstacle, start tile={}, segment count={}, map ID={}:",
            startTile, obstacle.segments().size(), terrain.hashCode());
        Logger.debug(obstacle);
        return obstacle;
    }

    private void errorAtCurrentTile(List<Vector2i> tilesWithErrors) {
        Logger.debug("Did not expect content {} at tile {}", terrain.get(cursor.currentTile), cursor.currentTile);
        tilesWithErrors.add(cursor.currentTile);
    }

    private void buildRestOfObstacle(Obstacle obstacle, Vector2i startTile, boolean ccw, List<Vector2i> tilesWithErrors) {
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            if (isExplored(cursor.currentTile)) {
                break;
            }
            setExplored(cursor.currentTile);
            byte tileContent = terrain.get(cursor.currentTile);

            boolean doubleWall = isDoubleWall(tileContent);
            // check if obstacle tiles have same wall strength, ignore doors
            if (tileContent != TerrainTiles.DOOR &&
                (obstacle.hasDoubleWalls() && !doubleWall || !obstacle.hasDoubleWalls() && doubleWall)) {
                errorAtCurrentTile(tilesWithErrors);
                break;
            }

            switch (tileContent) {

                case TerrainTiles.WALL_V -> {
                    if (cursor.points(DOWN)) {
                        obstacle.addSegment(scaledVector(DOWN, TS), ccw, tileContent);
                        cursor.move(DOWN);
                    } else if (cursor.points(UP)) {
                        obstacle.addSegment(scaledVector(UP, TS), ccw, tileContent);
                        cursor.move(UP);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case TerrainTiles.WALL_H, TerrainTiles.DOOR -> {
                    if (cursor.points(RIGHT)) {
                        obstacle.addSegment(scaledVector(RIGHT, TS), ccw, tileContent);
                        cursor.move(RIGHT);
                    } else if (cursor.points(LEFT)) {
                        obstacle.addSegment(scaledVector(LEFT, TS), ccw, tileContent);
                        cursor.move(LEFT);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case TerrainTiles.CORNER_SW, TerrainTiles.DCORNER_ANGULAR_SW -> {
                    if (cursor.points(DOWN)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SW_DOWN, ccw, tileContent);
                        cursor.move(RIGHT);
                    } else if (cursor.points(LEFT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SW_UP, ccw, tileContent);
                        cursor.move(UP);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case TerrainTiles.CORNER_SE, TerrainTiles.DCORNER_ANGULAR_SE -> {
                    if (cursor.points(DOWN)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SE_DOWN, ccw, tileContent);
                        cursor.move(LEFT);
                    }
                    else if (cursor.points(RIGHT)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SE_UP, ccw, tileContent);
                        cursor.move(UP);
                    }
                    else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case TerrainTiles.CORNER_NE, TerrainTiles.DCORNER_ANGULAR_NE -> {
                    if (cursor.points(UP)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_NE_UP, ccw, tileContent);
                        cursor.move(LEFT);
                    }
                    else if (cursor.points(RIGHT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NE_DOWN, ccw, tileContent);
                        cursor.move(DOWN);
                    }
                    else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case TerrainTiles.CORNER_NW, TerrainTiles.DCORNER_ANGULAR_NW -> {
                    if (cursor.points(UP)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NW_UP, ccw, tileContent);
                        cursor.move(RIGHT);
                    }
                    else if (cursor.points(LEFT)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_NW_DOWN, ccw, tileContent);
                        cursor.move(DOWN);
                    }
                    else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                default -> errorAtCurrentTile(tilesWithErrors);
            }

            if (cursor.currentTile.equals(startTile) || terrain.outOfBounds(cursor.currentTile)) {
                break;
            }
        }
    }

    private Vector2i scaledVector(Direction dir, int length) {
        return dir.vector().scaled(length);
    }

    //TODO simplify
    private Set<Obstacle> optimize(Set<Obstacle> obstacles) {
        var optimizedObstacles = new HashSet<Obstacle>();
        for (Obstacle obstacle : obstacles) {
            Obstacle optimized = new Obstacle(obstacle.startPoint());
            optimizedObstacles.add(optimized);
            boolean merging = false;
            Vector2i mergedVector = null;
            boolean mergedCCW = false;
            byte mergedMapContent = -1;
            for (int i = 0; i < obstacle.numSegments(); ++i) {
                ObstacleSegment segment = obstacle.segment(i);
                if (segment.isStraightLine()) {
                    if (merging) { // continue merging
                        mergedVector = mergedVector.plus(segment.vector());
                    } else { // start merging
                        mergedVector = segment.vector();
                        mergedCCW = segment.ccw();
                        mergedMapContent = segment.encoding();
                        merging = true;
                    }
                }
                else {
                    if (merging) {
                        optimized.addSegment(mergedVector, mergedCCW, mergedMapContent);
                        merging = false;
                    }
                    optimized.addSegment(segment.vector(), segment.ccw(), segment.encoding());
                }
            }
            if (merging) {
                optimized.addSegment(mergedVector, mergedCCW, mergedMapContent);
            }
        }
        return optimizedObstacles;
    }
}