/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static java.util.function.Predicate.not;

/**
 * Analyzes a terrain tile map and creates the obstacle list. An obstacle is stored as a
 * list of vectors defining the contour path of the obstacle.
 *
 * <p>The path starts at the minimum coordinate tile (NW arc) and moves in counter-clockwise order
 * around the obstacle.
 */
public class ObstacleBuilder {

    // Public API
    public static Set<Obstacle> buildObstacles(WorldMap worldMap, List<Vector2i> tilesWithErrors) {
        return new ObstacleBuilder(worldMap).buildObstacles(tilesWithErrors);
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

    // Arcs are represented by "diagonal" vectors
    private static final Vector2i SEG_ARC_NW_UP   = Vector2i.of(HTS, -HTS);
    private static final Vector2i SEG_ARC_NW_DOWN = SEG_ARC_NW_UP.inverse();
    private static final Vector2i SEG_ARC_SW_UP   = Vector2i.of(-HTS, -HTS);
    private static final Vector2i SEG_ARC_SW_DOWN = SEG_ARC_SW_UP.inverse();
    private static final Vector2i SEG_ARC_SE_UP   = Vector2i.of(HTS, -HTS);
    private static final Vector2i SEG_ARC_SE_DOWN = SEG_ARC_SE_UP.inverse();
    private static final Vector2i SEG_ARC_NE_UP   = Vector2i.of(-HTS, -HTS);
    private static final Vector2i SEG_ARC_NE_DOWN = SEG_ARC_NE_UP.inverse();

    private final WorldMap worldMap;
    private final BitSet exploredTiles = new BitSet();
    private Cursor cursor;

    private ObstacleBuilder(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    private boolean isTileExplored(Vector2i tile) {
        return exploredTiles.get(worldMap.indexInRowWiseOrder(tile));
    }

    private void setExplored(Vector2i tile) {
        exploredTiles.set(worldMap.indexInRowWiseOrder(tile));
    }

    private Set<Obstacle> buildObstacles(List<Vector2i> tilesWithErrors) {
        Logger.debug("Find obstacles in map ID={} size={}x{}", worldMap.hashCode(), worldMap.numRows(), worldMap.numCols());

        tilesWithErrors.clear();
        exploredTiles.clear();

        Set<Obstacle> obstacles = new HashSet<>();

        // Note: order of detection matters! Otherwise, when searching for closed
        // obstacles first, each failed attempt must set its visited tile set to unvisited!
        worldMap.tiles()
            .filter(not(this::isTileExplored))
            .filter(tile -> tile.x() == 0 || tile.x() == worldMap.numCols() - 1)
            .map(tile -> buildBorderObstacle(tile, tile.x() == 0, tilesWithErrors))
            .filter(Objects::nonNull)
            .forEach(obstacles::add);

        worldMap.tiles()
            .filter(not(this::isTileExplored))
            .filter(tile ->
                    worldMap.content(LayerID.TERRAIN, tile) == ARC_NW.$ ||
                    worldMap.content(LayerID.TERRAIN, tile) == DARC_NW.$) // house top-left corner
            .map(cornerNW -> buildInsideObstacle(cornerNW, tilesWithErrors))
            .forEach(obstacles::add);

        Logger.debug("Found {} obstacles", obstacles.size());

        return optimize(obstacles);
    }

    private Obstacle buildInsideObstacle(Vector2i cornerNW, List<Vector2i> tilesWithErrors) {
        Vector2i startPoint = cornerNW.scaled(TS).plus(TS, HTS);
        byte startTileContent = worldMap.content(LayerID.TERRAIN, cornerNW);
        Obstacle obstacle = new Obstacle(startPoint);
        obstacle.addSegment(SEG_ARC_NW_DOWN, true, startTileContent);
        cursor = new Cursor(cornerNW);
        cursor.move(Direction.DOWN);
        setExplored(cornerNW);
        buildRestOfObstacle(obstacle, cornerNW, true, tilesWithErrors);
        if (obstacle.isClosed()) {
            Logger.debug("Closed obstacle, top-left tile={}, map ID={}:", cornerNW, worldMap.hashCode());
            Logger.debug(obstacle);
        }
        return obstacle;
    }

    private Obstacle buildBorderObstacle(Vector2i startTile, boolean startsAtLeftBorder, List<Vector2i> tilesWithErrors) {
        Vector2i startPoint = startTile.scaled(TS).plus(startsAtLeftBorder ? 0 : TS, HTS);
        byte startTileContent = worldMap.content(LayerID.TERRAIN, startTile);
        var obstacle = new Obstacle(startPoint);
        cursor = new Cursor(startTile);
        if (startTileContent == WALL_H.$) {
            Direction startDir = startsAtLeftBorder ? Direction.RIGHT : Direction.LEFT;
            obstacle.addSegment(startDir.vector().scaled(TS), true, WALL_H.$);
            cursor.move(startDir);
        }
        else if (startsAtLeftBorder && startTileContent == ARC_SE.$) {
            obstacle.addSegment(SEG_ARC_SE_UP, true, ARC_SE.$);
            cursor.move(Direction.UP);
        }
        else if (startsAtLeftBorder && startTileContent == ARC_NE.$) {
            obstacle.addSegment(SEG_ARC_NE_DOWN, false, ARC_NE.$);
            cursor.move(Direction.DOWN);
        }
        else if (!startsAtLeftBorder && startTileContent == ARC_SW.$) {
            obstacle.addSegment(SEG_ARC_SW_UP, false, ARC_SW.$);
            cursor.move(Direction.UP);
        }
        else if (!startsAtLeftBorder && startTileContent == ARC_NW.$) {
            obstacle.addSegment(SEG_ARC_NW_DOWN, true, ARC_NW.$);
            cursor.move(Direction.DOWN);
        }
        else {
            return null; //TODO check this
        }
        setExplored(startTile);
        buildRestOfObstacle(obstacle, startTile, obstacle.segment(0).ccw(), tilesWithErrors);
        Logger.debug("Open obstacle, start tile={}, segment count={}, map ID={}:",
            startTile, obstacle.segments().size(), worldMap.hashCode());
        Logger.debug(obstacle);
        return obstacle;
    }

    private void errorAtCurrentTile(List<Vector2i> tilesWithErrors) {
        Logger.debug("Did not expect content {} at tile {}",
                worldMap.content(LayerID.TERRAIN, cursor.currentTile), cursor.currentTile);
        tilesWithErrors.add(cursor.currentTile);
    }

    private void buildRestOfObstacle(Obstacle obstacle, Vector2i startTile, boolean counterClockwise, List<Vector2i> tilesWithErrors) {
        boolean ccw = counterClockwise;
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            if (isTileExplored(cursor.currentTile)) {
                break;
            }
            setExplored(cursor.currentTile);
            byte tileContent = worldMap.content(LayerID.TERRAIN, cursor.currentTile);
            if (tileContent == WALL_V.$) {
                if (cursor.points(Direction.DOWN)) {
                    obstacle.addSegment(Direction.DOWN.vector().scaled(TS), ccw, tileContent);
                    cursor.move(Direction.DOWN);
                } else if (cursor.points(Direction.UP)) {
                    obstacle.addSegment(Direction.UP.vector().scaled(TS), ccw, tileContent);
                    cursor.move(Direction.UP);
                } else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else if (tileContent == WALL_H.$ || tileContent == DOOR.$) {
                if (cursor.points(Direction.RIGHT)) {
                    obstacle.addSegment(Direction.RIGHT.vector().scaled(TS), ccw, tileContent);
                    cursor.move(Direction.RIGHT);
                } else if (cursor.points(Direction.LEFT)) {
                    obstacle.addSegment(Direction.LEFT.vector().scaled(TS), ccw, tileContent);
                    cursor.move(Direction.LEFT);
                } else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else if (tileContent == ARC_SW.$ || tileContent == DARC_SW.$) {
                if (cursor.points(Direction.DOWN)) {
                    ccw = true;
                    obstacle.addSegment(SEG_ARC_SW_DOWN, ccw, tileContent);
                    cursor.move(Direction.RIGHT);
                } else if (cursor.points(Direction.LEFT)) {
                    ccw = false;
                    obstacle.addSegment(SEG_ARC_SW_UP, ccw, tileContent);
                    cursor.move(Direction.UP);
                } else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else if (tileContent == ARC_SE.$ || tileContent == DARC_SE.$) {
                if (cursor.points(Direction.DOWN)) {
                    ccw = false;
                    obstacle.addSegment(SEG_ARC_SE_DOWN, ccw, tileContent);
                    cursor.move(Direction.LEFT);
                }
                else if (cursor.points(Direction.RIGHT)) {
                    ccw = true;
                    obstacle.addSegment(SEG_ARC_SE_UP, ccw, tileContent);
                    cursor.move(Direction.UP);
                }
                else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else if (tileContent == ARC_NE.$ || tileContent == DARC_NE.$) {
                if (cursor.points(Direction.UP)) {
                    ccw = true;
                    obstacle.addSegment(SEG_ARC_NE_UP, ccw, tileContent);
                    cursor.move(Direction.LEFT);
                }
                else if (cursor.points(Direction.RIGHT)) {
                    ccw = false;
                    obstacle.addSegment(SEG_ARC_NE_DOWN, ccw, tileContent);
                    cursor.move(Direction.DOWN);
                }
                else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else if (tileContent == ARC_NW.$ || tileContent == DARC_NW.$) {
                if (cursor.points(Direction.UP)) {
                    ccw = false;
                    obstacle.addSegment(SEG_ARC_NW_UP, ccw, tileContent);
                    cursor.move(Direction.RIGHT);
                }
                else if (cursor.points(Direction.LEFT)) {
                    ccw = true;
                    obstacle.addSegment(SEG_ARC_NW_DOWN, ccw, tileContent);
                    cursor.move(Direction.DOWN);
                }
                else {
                    errorAtCurrentTile(tilesWithErrors);
                }
            }
            else {
                errorAtCurrentTile(tilesWithErrors);
            }

            if (cursor.currentTile.equals(startTile) || worldMap.outOfWorld(cursor.currentTile)) {
                break;
            }
        }
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