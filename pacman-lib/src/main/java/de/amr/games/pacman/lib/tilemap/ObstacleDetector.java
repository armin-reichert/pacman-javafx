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

    private final List<Vector2i> obstacleTiles = new ArrayList<>();
    private Vector2i cursor;

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
            .map(this::detectObstacle)
            .forEach(obstacles::add);
    }

    private Obstacle detectObstacle(Vector2i cornerNW) {
        Logger.info("Detect obstacle with top-left corner {}, map ID={}", cornerNW, terrain.hashCode());
        obstacleTiles.clear();
        obstacleTiles.add(cornerNW);
        cursor = cornerNW;
        Direction orientation = LEFT; // LEFT = counter clock-wise, RIGHT = clockwise

        // start polygon at right edge of start tile
        Vector2f startPoint = cornerNW.scaled((float)TS).plus(TS, TS-DY);
        Obstacle obstacle = new Obstacle(startPoint);
        obstacle.addSegment(SEG_CORNER_NW_DOWN, orientation, terrain.get(cursor));

        moveCursor(DOWN);
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            if (exploredTiles.contains(cursor)) {
                break;
            }
            exploredTiles.add(cursor);
            switch (terrain.get(cursor)) {

                case Tiles.WALL_V -> {
                    if (isGoing(DOWN)) {
                        obstacle.addSegment(oneTile(DOWN), orientation, terrain.get(cursor));
                        moveCursor(DOWN);
                    } else if (isGoing(UP)) {
                        obstacle.addSegment(oneTile(UP), orientation, terrain.get(cursor));
                        moveCursor(UP);
                    } else {
                        //error
                    }
                }

                case Tiles.WALL_H -> {
                    if (isGoing(RIGHT)) {
                        obstacle.addSegment(oneTile(RIGHT), orientation, terrain.get(cursor));
                        moveCursor(RIGHT);
                    } else if (isGoing(LEFT)) {
                        obstacle.addSegment(oneTile(LEFT), orientation, terrain.get(cursor));
                        moveCursor(LEFT);
                    } else {
                        //error
                    }
                }

                case Tiles.CORNER_SW -> {
                    if (isGoing(DOWN)) {
                        orientation = LEFT;
                        obstacle.addSegment(SEG_CORNER_SW_DOWN, orientation, terrain.get(cursor));
                        moveCursor(RIGHT);
                    } else if (isGoing(LEFT)) {
                        orientation = RIGHT;
                        obstacle.addSegment(SEG_CORNER_SW_UP, orientation, terrain.get(cursor));
                        moveCursor(UP);
                    } else {
                        //error
                    }
                }

                case Tiles.CORNER_SE -> {
                    if (isGoing(DOWN)) {
                        orientation = RIGHT;
                        obstacle.addSegment(SEG_CORNER_SE_DOWN, orientation, terrain.get(cursor));
                        moveCursor(LEFT);
                    }
                    else if (isGoing(RIGHT)) {
                        orientation = LEFT;
                        obstacle.addSegment(SEG_CORNER_SE_UP, orientation, terrain.get(cursor));
                        moveCursor(UP);
                    }
                    else {
                        //error
                    }
                }

                case Tiles.CORNER_NE -> {
                    if (isGoing(UP)) {
                        orientation = LEFT;
                        obstacle.addSegment(SEG_CORNER_NE_UP, orientation, terrain.get(cursor));
                        moveCursor(LEFT);
                    }
                    else if (isGoing(RIGHT)) {
                        orientation = RIGHT;
                        obstacle.addSegment(SEG_CORNER_NE_DOWN, orientation, terrain.get(cursor));
                        moveCursor(DOWN);
                    }
                    else {
                        //error
                    }
                }

                case Tiles.CORNER_NW -> {
                    if (isGoing(UP)) {
                        orientation = RIGHT;
                        obstacle.addSegment(SEG_CORNER_NW_UP, orientation, terrain.get(cursor));
                        moveCursor(RIGHT);
                    }
                    else if (isGoing(LEFT)) {
                        orientation = LEFT;
                        obstacle.addSegment(SEG_CORNER_NW_DOWN, orientation, terrain.get(cursor));
                        moveCursor(DOWN);
                    }
                    else {
                        //error
                    }
                }
            }

            if (cursor.equals(cornerNW) || terrain.outOfBounds(cursor)) {
                break;
            }
        }

        Logger.info("{}{}", obstacle.isClosed()? "Closed " : "", obstacle);
        return obstacle;
    }

    private boolean isGoing(Direction dir) {
        return obstacleTiles.getLast().plus(dir.vector()).equals(cursor);
    }

    private void moveCursor(Direction dir) {
        obstacleTiles.add(cursor);
        cursor = cursor.plus(dir.vector());
    }

    private Vector2f oneTile(Direction dir) {
        return dir.vector().scaled((float) TS);
    }
}
