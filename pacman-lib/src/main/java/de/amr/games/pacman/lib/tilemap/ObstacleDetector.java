package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.graph.Dir;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

public class ObstacleDetector {

    private Set<Vector2i> explored = new HashSet<>();
    private TileMap terrain;
    private List<Obstacle> obstacles = new ArrayList<>();

    private List<Vector2i> obstacleTiles = new ArrayList<>();
    private Vector2i currentTile;

    public ObstacleDetector(TileMap terrain) {
        this.terrain = terrain;
    }

    public List<Obstacle> detectObstacles() {
        detectObstaclesInside();
        return obstacles;
    }

    private void detectObstaclesInside() {
        terrain.tiles(Tiles.CORNER_NW)
            .filter(Predicate.not(explored::contains))
            .forEach(topLeftCornerTile -> obstacles.add(detectObstacle(topLeftCornerTile)));
    }

    private Obstacle detectObstacle(Vector2i topLeftCornerTile) {
        Logger.info("Detect obstacle with top-left corner tile {}. Map {}", topLeftCornerTile, terrain);
        obstacleTiles.clear();
        obstacleTiles.add(topLeftCornerTile);
        currentTile = topLeftCornerTile;
        move(DOWN);

        Vector2f startPoint = topLeftCornerTile.scaled(TS).plus(TS, HTS).toVector2f();
        Obstacle obstacle = new Obstacle(startPoint);
        obstacle.addSegment(v2f(-HTS, HTS));
        int bailout = 0;
        while (bailout < 1000) {
            ++bailout;
            byte content = terrain.get(currentTile);
            if (explored.contains(currentTile)) {
                break;
            }
            explored.add(currentTile);
            switch (content) {
                case Tiles.WALL_V -> {
                    if (isGoing(DOWN)) {
                        obstacle.addSegment(oneTile(DOWN));
                        move(DOWN);
                    } else if (isGoing(UP)) {
                        obstacle.addSegment(oneTile(UP));
                        move(UP);
                    } else {
                        //error
                    }
                }
                case Tiles.WALL_H -> {
                    if (isGoing(RIGHT)) {
                        obstacle.addSegment(oneTile(RIGHT));
                        move(RIGHT);
                    } else if (isGoing(LEFT)) {
                        obstacle.addSegment(oneTile(LEFT));
                        move(LEFT);
                    } else {
                        //error
                    }
                }
                case Tiles.CORNER_SW -> {
                    if (isGoing(DOWN)) {
                        if (isGoing(LEFT)) {
                            obstacle.addSegment(v2f(HTS, HTS));
                            move(RIGHT);
                        } else {
                            obstacle.addSegment(v2f(HTS, HTS));
                            move(RIGHT);
                        }
                    } else if (isGoing(LEFT)) {
                        obstacle.addSegment(v2f(-HTS, -HTS));
                        move(UP);
                    } else {
                        //error
                    }
                }
                case Tiles.CORNER_SE -> {
                    if (isGoing(RIGHT)) {
                        if (isGoing(DOWN)) {
                            obstacle.addSegment(v2f(-HTS, HTS));
                            move(LEFT);
                        } else {
                            obstacle.addSegment(v2f(HTS, -HTS));
                            move(UP);
                        }
                    } else if (isGoing(DOWN)) {
                        obstacle.addSegment(v2f(-HTS, HTS));
                        move(LEFT);
                    } else {
                        //error
                    }
                }
                case Tiles.CORNER_NE -> {
                    if (isGoing(RIGHT)) {
                        if (isGoing(UP)) {
                            obstacle.addSegment(v2f(-HTS, -HTS));
                            move(LEFT);
                        } else {
                            obstacle.addSegment(v2f(HTS, HTS));
                            move(DOWN);
                        }
                    } else if (isGoing(UP)) {
                        obstacle.addSegment(v2f(-HTS, -HTS));
                        move(LEFT);
                    } else {
                        //error
                    }
                }
                case Tiles.CORNER_NW -> {
                    if (isGoing(LEFT)) {
                        if (isGoing(UP)) {
                            obstacle.addSegment(v2f(HTS, -HTS));
                            move(RIGHT);
                        } else {
                            obstacle.addSegment(v2f(-HTS, HTS));
                            move(DOWN);
                        }
                    } else if (isGoing(UP)) {
                        obstacle.addSegment(v2f(HTS, -HTS));
                        move(RIGHT);
                    } else {
                        //error
                    }
                }
            }
            if (currentTile.equals(topLeftCornerTile)) {
                break;
            }
        }
        return obstacle;
    }

    private boolean isGoing(Direction dir) {
        return obstacleTiles.getLast().plus(dir.vector()).equals(currentTile);
    }

    private void move(Direction dir) {
        obstacleTiles.add(currentTile);
        currentTile = currentTile.plus(dir.vector());
    }

    private Vector2f oneTile(Direction dir) {
        return dir.vector().scaled((float) TS);
    }
}
