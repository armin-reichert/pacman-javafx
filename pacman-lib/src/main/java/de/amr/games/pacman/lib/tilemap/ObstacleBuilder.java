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
import static de.amr.games.pacman.lib.tilemap.Tiles.isDoubleWall;

public class ObstacleBuilder {

    static class Cursor {
        private Vector2i prevTile, currentTile;
        
        Cursor(Vector2i prevTile, Vector2i currentTile) {
            this.prevTile = prevTile;
            this.currentTile = currentTile;
        }
        
        boolean isMoving(Direction dir) {
            return prevTile.plus(dir.vector()).equals(currentTile);
        }

        void move(Direction dir) {
            prevTile = currentTile;
            currentTile = currentTile.plus(dir.vector());
        }
    }
    
    private static final Vector2f SEG_CORNER_NW_UP   = v2f(HTS, -HTS);
    private static final Vector2f SEG_CORNER_NW_DOWN = SEG_CORNER_NW_UP.inverse();
    private static final Vector2f SEG_CORNER_SW_UP   = v2f(-HTS, -HTS);
    private static final Vector2f SEG_CORNER_SW_DOWN = SEG_CORNER_SW_UP.inverse();
    private static final Vector2f SEG_CORNER_SE_UP   = v2f(HTS, -HTS);
    private static final Vector2f SEG_CORNER_SE_DOWN = SEG_CORNER_SE_UP.inverse();
    private static final Vector2f SEG_CORNER_NE_UP   = v2f(-HTS, -HTS);
    private static final Vector2f SEG_CORNER_NE_DOWN = SEG_CORNER_NE_UP.inverse();

    private final TileMap terrain;
    private final Set<Vector2i> exploredTiles = new HashSet<>();
    private Cursor cursor;

    public ObstacleBuilder(TileMap terrain) {
        this.terrain = terrain;
    }

    public List<Obstacle> buildObstacles(List<Vector2i> tilesWithErrors) {
        tilesWithErrors.clear();
        List<Obstacle> obstacles = new ArrayList<>();

        Logger.info("Find obstacles in map ID={} size={}x{}", terrain.hashCode(), terrain.numRows(), terrain.numCols());
        // Note: order of detection matters! Otherwise, when searching for closed
        // obstacles first, each failed attempt must set its visited tile set to unvisited!
        terrain.tiles()
            .filter(tile -> tile.x() == 0 || tile.x() == terrain.numCols() - 1)
            .filter(Predicate.not(exploredTiles::contains))
            .map(tile -> buildOpenObstacle(tile, tile.x() == 0, tilesWithErrors))
            .filter(Objects::nonNull)
            .forEach(obstacles::add);

        terrain.tiles()
            .filter(tile ->
                terrain.get(tile) == Tiles.CORNER_NW ||
                terrain.get(tile) == Tiles.DCORNER_NW ||
                terrain.get(tile) == Tiles.DCORNER_ANGULAR_NW) // house top-left corner
            .filter(Predicate.not(exploredTiles::contains))
            .map(cornerNW -> buildClosedObstacle(cornerNW, tilesWithErrors))
            .forEach(obstacles::add);

        Logger.info("Found {} obstacles", obstacles.size());
        obstacles = optimize(obstacles);
        Logger.info("Optimized {} obstacles", obstacles.size());
        return obstacles;
    }

    private Obstacle buildClosedObstacle(Vector2i cornerNW, List<Vector2i> tilesWithErrors) {
        Vector2f startPoint = cornerNW.scaled((float)TS).plus(TS, HTS);
        byte startTileContent = terrain.get(cornerNW);
        boolean doubleWalls = startTileContent == Tiles.DCORNER_NW || startTileContent == Tiles.DCORNER_ANGULAR_NW;
        Obstacle obstacle = new Obstacle(startPoint, doubleWalls);
        obstacle.addSegment(SEG_CORNER_NW_DOWN, true, startTileContent);

        cursor = new Cursor(null, cornerNW);
        cursor.move(DOWN);

        buildRestOfObstacle(obstacle, cornerNW, true, tilesWithErrors);

        if (obstacle.isClosed()) {
            Logger.debug("Closed obstacle, top-left tile={}, map ID={}:", cornerNW, terrain.hashCode());
            Logger.debug(obstacle);
        } else {
            Logger.error("Could not build closed obstacle, top-left tile={}, map ID={}", cornerNW, terrain.hashCode());
        }
        return obstacle;
    }

    private Obstacle buildOpenObstacle(Vector2i startTile, boolean startsAtLeftBorder, List<Vector2i> tilesWithErrors) {
        cursor = new Cursor(null, startTile);
        Vector2f startPoint = startTile.scaled((float) TS).plus(startsAtLeftBorder ? 0 : TS, HTS);
        byte startTileContent = terrain.get(startTile);
        boolean doubleWall = isDoubleWall(startTileContent);
        var obstacle = new Obstacle(startPoint, doubleWall);
        if (startTileContent == Tiles.DWALL_H) {
            Direction initialDir = startsAtLeftBorder ? RIGHT : LEFT;
            obstacle.addSegment(arrow(initialDir, TS), true, Tiles.DWALL_H);
            cursor.move(initialDir);
        }
        else if (startsAtLeftBorder && startTileContent == Tiles.DCORNER_SE) {
            obstacle.addSegment(SEG_CORNER_SE_UP, true, Tiles.DCORNER_SE);
            cursor.move(UP);
        }
        else if (startsAtLeftBorder && startTileContent == Tiles.DCORNER_NE) {
            obstacle.addSegment(SEG_CORNER_NE_DOWN, false, Tiles.DCORNER_NE);
            cursor.move(DOWN);
        }
        else if (!startsAtLeftBorder && startTileContent == Tiles.DCORNER_SW) {
            obstacle.addSegment(SEG_CORNER_SW_UP, false, Tiles.DCORNER_SW);
            cursor.move(UP);
        }
        else if (!startsAtLeftBorder && startTileContent == Tiles.DCORNER_NW) {
            obstacle.addSegment(SEG_CORNER_NW_DOWN, true, Tiles.DCORNER_NW);
            cursor.move(DOWN);
        }
        else {
            return null;
        }

        buildRestOfObstacle(obstacle, startTile, true, tilesWithErrors);
        Logger.debug("Open obstacle, start tile={}, segment count={}, map ID={}:",
            startTile, obstacle.segments().size(), terrain.hashCode());
        Logger.debug(obstacle);
        return obstacle;
    }

    private void errorAtCurrentTile(List<Vector2i> tilesWithErrors) {
        Logger.error("Did not expect content {} at tile {}", terrain.get(cursor.currentTile), cursor.currentTile);
        tilesWithErrors.add(cursor.currentTile);
    }

    private void buildRestOfObstacle(Obstacle obstacle, Vector2i startTile, boolean ccw, List<Vector2i> tilesWithErrors) {
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            if (exploredTiles.contains(cursor.currentTile)) {
                break;
            }
            exploredTiles.add(cursor.currentTile);
            byte tileContent = terrain.get(cursor.currentTile);
            boolean doubleWall = isDoubleWall(tileContent);
            if (tileContent != Tiles.DOOR &&
                (obstacle.hasDoubleWalls() && !doubleWall || !obstacle.hasDoubleWalls() && doubleWall)) {
                errorAtCurrentTile(tilesWithErrors);
                break;
            }

            switch (tileContent) {

                case Tiles.WALL_V, Tiles.DWALL_V -> {
                    if (cursor.isMoving(DOWN)) {
                        obstacle.addSegment(arrow(DOWN, TS), ccw, tileContent);
                        cursor.move(DOWN);
                    } else if (cursor.isMoving(UP)) {
                        obstacle.addSegment(arrow(UP, TS), ccw, tileContent);
                        cursor.move(UP);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case Tiles.WALL_H, Tiles.DWALL_H, Tiles.DOOR -> {
                    if (cursor.isMoving(RIGHT)) {
                        obstacle.addSegment(arrow(RIGHT, TS), ccw, tileContent);
                        cursor.move(RIGHT);
                    } else if (cursor.isMoving(LEFT)) {
                        obstacle.addSegment(arrow(LEFT, TS), ccw, tileContent);
                        cursor.move(LEFT);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case Tiles.CORNER_SW, Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> {
                    if (cursor.isMoving(DOWN)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SW_DOWN, ccw, tileContent);
                        cursor.move(RIGHT);
                    } else if (cursor.isMoving(LEFT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SW_UP, ccw, tileContent);
                        cursor.move(UP);
                    } else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case Tiles.CORNER_SE, Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> {
                    if (cursor.isMoving(DOWN)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_SE_DOWN, ccw, tileContent);
                        cursor.move(LEFT);
                    }
                    else if (cursor.isMoving(RIGHT)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_SE_UP, ccw, tileContent);
                        cursor.move(UP);
                    }
                    else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case Tiles.CORNER_NE, Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> {
                    if (cursor.isMoving(UP)) {
                        ccw = true;
                        obstacle.addSegment(SEG_CORNER_NE_UP, ccw, tileContent);
                        cursor.move(LEFT);
                    }
                    else if (cursor.isMoving(RIGHT)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NE_DOWN, ccw, tileContent);
                        cursor.move(DOWN);
                    }
                    else {
                        errorAtCurrentTile(tilesWithErrors);
                    }
                }

                case Tiles.CORNER_NW, Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> {
                    if (cursor.isMoving(UP)) {
                        ccw = false;
                        obstacle.addSegment(SEG_CORNER_NW_UP, ccw, tileContent);
                        cursor.move(RIGHT);
                    }
                    else if (cursor.isMoving(LEFT)) {
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

    private Vector2f arrow(Direction dir, float length) {
        return dir.vector().scaled(length);
    }

    private List<Obstacle> optimize(List<Obstacle> obstacles) {
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
        return optimizedObstacles;
    }
}