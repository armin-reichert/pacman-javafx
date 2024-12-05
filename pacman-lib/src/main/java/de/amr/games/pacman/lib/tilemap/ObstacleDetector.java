/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2f;

public class ObstacleDetector {

    private static final float DX = 4.0f;
    private static final float DY = 4.0f;

    private static final Vector2f SEG_CORNER_NW_UP   = v2f(DX,-DY);
    private static final Vector2f SEG_CORNER_NW_DOWN = SEG_CORNER_NW_UP.inverse();
    private static final Vector2f SEG_CORNER_SW_UP   = v2f(-DX,-DY);
    private static final Vector2f SEG_CORNER_SW_DOWN = SEG_CORNER_SW_UP.inverse();
    private static final Vector2f SEG_CORNER_SE_UP   = v2f(DX,-DY);
    private static final Vector2f SEG_CORNER_SE_DOWN = SEG_CORNER_SE_UP.inverse();
    private static final Vector2f SEG_CORNER_NE_UP   = v2f(-DX,-DY);
    private static final Vector2f SEG_CORNER_NE_DOWN = SEG_CORNER_NE_UP.inverse();

    private final TileMap terrain;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Set<Vector2i> exploredTiles = new HashSet<>();

    private Vector2i predecessorTile;
    private Vector2i cursorTile;

    public ObstacleDetector(TileMap terrain) {
        this.terrain = terrain;
    }

    public List<Obstacle> detectObstacles() {
        detectObstaclesInside();
        return obstacles;
    }

    private void detectObstaclesInside() {
        terrain.tiles(Tiles.CORNER_NW)
            .filter(Predicate.not(exploredTiles::contains))
            .map(this::detectClosedObstacle)
            .forEach(obstacles::add);
    }

    private void buildObstacle(Obstacle obstacle, Vector2i startTile, boolean ccw) {
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            if (exploredTiles.contains(cursorTile)) {
                break;
            }
            exploredTiles.add(cursorTile);
            switch (terrain.get(cursorTile)) {

                case Tiles.WALL_V -> {
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

                case Tiles.WALL_H -> {
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

                case Tiles.CORNER_SW -> {
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

                case Tiles.CORNER_SE -> {
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

                case Tiles.CORNER_NE -> {
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

                case Tiles.CORNER_NW -> {
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

    private Obstacle detectClosedObstacle(Vector2i cornerNW) {
        // start polygon at right edge of start tile
        Vector2f startPoint = cornerNW.scaled((float)TS).plus(TS, TS-DY);
        Obstacle obstacle = new Obstacle(startPoint);
        predecessorTile = null;
        cursorTile = cornerNW;
        obstacle.addSegment(SEG_CORNER_NW_DOWN, true, terrain.get(cursorTile));
        moveCursor(DOWN);
        buildObstacle(obstacle, cornerNW, true);
        if (obstacle.isClosed()) {
            Logger.info("Found closed obstacle, top-left tile={}, map ID={}:", cornerNW, terrain.hashCode());
            Logger.info(obstacle);
        } else {
            Logger.error("Could not identify closed obstacle, top-left tile={}, map ID={}", cornerNW, terrain.hashCode());
        }
        return obstacle;
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
}