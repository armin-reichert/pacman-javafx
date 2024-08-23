/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.lib.graph.GridGraph;
import de.amr.games.pacman.lib.graph.GridGraphImpl;
import org.tinylog.Logger;

import java.io.File;
import java.util.Random;

public class MazeMapGenerator {

    private static final byte FREE = Tiles.EMPTY;
    private static final byte BLOCKED = Tiles.WALL;

    private static final int EMPTY_ROWS_ABOVE = 3, EMPTY_ROWS_BELOW = 2;

    public WorldMap createMazeMap(int numRows, int numCols) {
        // create maze graph
        GridGraph graph = new GridGraphImpl(numRows, numCols);
        MazeGenerators.createMazeByRecursiveDivision(graph);

        // create map from maze (3 times larger!)
        var map = new WorldMap(3 * numRows + EMPTY_ROWS_ABOVE + EMPTY_ROWS_BELOW, 3 * numCols);
        var terrain = map.terrain();
        for (int v = 0; v < graph.numVertices(); ++v) {
            // center of 3x3 "macro" cell
            int row = 3 * graph.row(v) + 1 + EMPTY_ROWS_ABOVE, col = 3 * graph.col(v) + 1;
            if (row < 3 || row > 3 * numRows + EMPTY_ROWS_BELOW) {
                continue;
            }
            set(terrain, row - 1, col - 1, BLOCKED);
            set(terrain, row - 1, col, graph.connected(v, Dir.N) ? FREE : BLOCKED);
            set(terrain, row - 1, col + 1, BLOCKED);
            set(terrain, row, col - 1, graph.connected(v, Dir.W) ? FREE : BLOCKED);
            set(terrain, row, col, FREE);
            set(terrain, row, col + 1, graph.connected(v, Dir.E) ? FREE : BLOCKED);
            set(terrain, row + 1, col - 1, BLOCKED);
            set(terrain, row + 1, col, graph.connected(v, Dir.S) ? FREE : BLOCKED);
            set(terrain, row + 1, col + 1, BLOCKED);
        }
        computeWallContour(map, new Vector2i(0, EMPTY_ROWS_ABOVE));
        addFood(map);
        terrain.setProperty("color_wall_fill", "#993300");
        return map;
    }

    private Vector2i current;
    private Vector2i predecessor;
    private Direction moveDir;

    private boolean inRange(Vector2i tile, TileMap map) {
        return 0 <= tile.x() && tile.x() < map.numCols() && 0 <= tile.y() && tile.y() <= map.numRows();
    }

    private void computeWallContour(WorldMap map, Vector2i startTile) {
        TileMap terrain = map.terrain();
        predecessor = null;
        current = startTile;
        set(terrain, current, Tiles.CORNER_NW);
        moveDir = Direction.RIGHT;
        move();
        while (inRange(current, terrain)) {
            Vector2i tileAtRightHand = current.plus(moveDir.nextClockwise().vector());
            if (terrain.get(tileAtRightHand) == FREE) {
                Vector2i tileAhead = current.plus(moveDir.vector());
                if (terrain.get(tileAhead) == BLOCKED) {
                    set(terrain, current, moveDir.isHorizontal() ? Tiles.WALL_H : Tiles.WALL_V);
                } else {
                    byte corner = switch (moveDir) {
                        case LEFT  -> Tiles.CORNER_NW;
                        case RIGHT -> Tiles.CORNER_SE;
                        case UP    -> Tiles.CORNER_NE;
                        case DOWN  -> Tiles.CORNER_SW;
                    };
                    set(terrain, current, corner);
                    turnCounterclockwise();
                }
            } else {
                byte corner = switch (moveDir) {
                    case RIGHT -> Tiles.CORNER_NE;
                    case DOWN  -> Tiles.CORNER_SE;
                    case LEFT  -> Tiles.CORNER_SW;
                    case UP    -> Tiles.CORNER_NW;
                };
                set(terrain, current, corner);
                turnClockwise();
            }
            move();
        }
        set(terrain, predecessor, Tiles.WALL_V); //TODO correct?
    }

    private void turnClockwise() {
        moveDir = moveDir.nextClockwise();
    }

    private void turnCounterclockwise() {
        moveDir = moveDir.nextCounterClockwise();
    }

    private void addFood(WorldMap map) {
        TileMap terrainMap = map.terrain();
        TileMap foodMap = map.food();
        for (int row = EMPTY_ROWS_ABOVE; row < foodMap.numRows() - EMPTY_ROWS_BELOW; ++row) {
            for (int col = 0; col < foodMap.numCols(); ++col) {
                if (terrainMap.get(row, col) == Tiles.EMPTY && new Random().nextInt(100) < 40) {
                    foodMap.set(row, col, Tiles.PELLET);
                }
            }
        }
    }

    private void move() {
        predecessor = current;
        current = current.plus(moveDir.vector());
    }

    private void set(TileMap map, Vector2i tile, byte value) {
        set(map, tile.y(), tile.x(), value);
    }

    private void set(TileMap map, int row, int col, byte value) {
        if (row < 0 || row >= map.numRows()) {
            return;
        }
        if (col < 0 || col >= map.numCols()) {
            return;
        }
        map.set(row, col, value);
        Logger.debug("x={} y={}: {} move {}", col, row, value, moveDir);
    }

    public static void main(String[] args)  {
        MazeMapGenerator mg = new MazeMapGenerator();
        //WorldMap mazeMap = new WorldMap(new File("maze.world"));
        for (int i = 0; i < 5; ++i) {
            try {
                WorldMap mazeMap = mg.createMazeMap(10, 10);
                File file = new File("maze_%d.world".formatted(i));
                mazeMap.save(file);
            } catch (Exception x) {
                Logger.error(x);
            }
        }
    }
}