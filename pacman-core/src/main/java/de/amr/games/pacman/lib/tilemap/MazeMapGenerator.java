/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.lib.graph.GridGraph;
import de.amr.games.pacman.lib.graph.GridGraphImpl;
import de.amr.games.pacman.model.WorldMapProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.Random;

public class MazeMapGenerator {

    private static final byte FREE = TerrainTiles.EMPTY;
    private static final byte BLOCKED = TerrainTiles.WALL_H;

    private static final int EMPTY_ROWS_ABOVE = 3, EMPTY_ROWS_BELOW = 2;

    public WorldMap createMazeMap(int numRows, int numCols) {
        // create maze graph
        GridGraph graph = new GridGraphImpl(numRows, numCols);
        MazeGenerators.createMazeByRecursiveDivision(graph);

        // create map from maze (3 times larger!)
        var map = new WorldMap(3 * numRows + EMPTY_ROWS_ABOVE + EMPTY_ROWS_BELOW, 3 * numCols);
        for (int v = 0; v < graph.numVertices(); ++v) {
            // center of 3x3 "macro" cell
            int row = 3 * graph.row(v) + 1 + EMPTY_ROWS_ABOVE, col = 3 * graph.col(v) + 1;
            if (row < 3 || row > 3 * numRows + EMPTY_ROWS_BELOW) {
                continue;
            }
            set(map, LayerID.TERRAIN, row - 1, col - 1, BLOCKED);
            set(map, LayerID.TERRAIN, row - 1, col, graph.connected(v, Dir.N) ? FREE : BLOCKED);
            set(map, LayerID.TERRAIN, row - 1, col + 1, BLOCKED);
            set(map, LayerID.TERRAIN, row, col - 1, graph.connected(v, Dir.W) ? FREE : BLOCKED);
            set(map, LayerID.TERRAIN, row, col, FREE);
            set(map, LayerID.TERRAIN, row, col + 1, graph.connected(v, Dir.E) ? FREE : BLOCKED);
            set(map, LayerID.TERRAIN, row + 1, col - 1, BLOCKED);
            set(map, LayerID.TERRAIN, row + 1, col, graph.connected(v, Dir.S) ? FREE : BLOCKED);
            set(map, LayerID.TERRAIN, row + 1, col + 1, BLOCKED);
        }
        computeWallContour(map, new Vector2i(0, EMPTY_ROWS_ABOVE));
        return map;
    }

    private Vector2i current;
    private Vector2i predecessor;
    private Direction moveDir;

    private boolean inRange(Vector2i tile, WorldMap map) {
        return 0 <= tile.x() && tile.x() < map.numCols() && 0 <= tile.y() && tile.y() <= map.numRows();
    }

    private void computeWallContour(WorldMap map, Vector2i startTile) {
        predecessor = null;
        current = startTile;
        set(map, LayerID.TERRAIN, current, TerrainTiles.ARC_NW);
        moveDir = Direction.RIGHT;
        move();
        while (inRange(current, map)) {
            Vector2i tileAtRightHand = current.plus(moveDir.nextClockwise().vector());
            if (map.get(LayerID.TERRAIN, tileAtRightHand) == FREE) {
                Vector2i tileAhead = current.plus(moveDir.vector());
                if (map.get(LayerID.TERRAIN, tileAhead) == BLOCKED) {
                    set(map, LayerID.TERRAIN, current,
                            moveDir.isHorizontal() ? TerrainTiles.WALL_H : TerrainTiles.WALL_V);
                } else {
                    byte corner = switch (moveDir) {
                        case LEFT  -> TerrainTiles.ARC_NW;
                        case RIGHT -> TerrainTiles.ARC_SE;
                        case UP    -> TerrainTiles.ARC_NE;
                        case DOWN  -> TerrainTiles.ARC_SW;
                    };
                    set(map, LayerID.TERRAIN, current, corner);
                    turnCounterclockwise();
                }
            } else {
                byte corner = switch (moveDir) {
                    case RIGHT -> TerrainTiles.ARC_NE;
                    case DOWN  -> TerrainTiles.ARC_SE;
                    case LEFT  -> TerrainTiles.ARC_SW;
                    case UP    -> TerrainTiles.ARC_NW;
                };
                set(map, LayerID.TERRAIN, current, corner);
                turnClockwise();
            }
            move();
        }
        set(map, LayerID.TERRAIN, predecessor, TerrainTiles.WALL_V); //TODO correct?
    }

    private void turnClockwise() {
        moveDir = moveDir.nextClockwise();
    }

    private void turnCounterclockwise() {
        moveDir = moveDir.nextCounterClockwise();
    }

    private void addFood(WorldMap map) {
        for (int row = EMPTY_ROWS_ABOVE; row < map.numRows() - EMPTY_ROWS_BELOW; ++row) {
            for (int col = 0; col < map.numCols(); ++col) {
                if (map.get(LayerID.TERRAIN, row, col) == TerrainTiles.EMPTY
                        && new Random().nextInt(100) < 40) {
                    map.set(LayerID.FOOD, row, col, FoodTiles.PELLET);
                }
            }
        }
    }

    private void move() {
        predecessor = current;
        current = current.plus(moveDir.vector());
    }

    private void set(WorldMap map, LayerID layerID, Vector2i tile, byte value) {
        set(map, layerID, tile.y(), tile.x(), value);
    }

    private void set(WorldMap map, LayerID layerID, int row, int col, byte value) {
        if (row < 0 || row >= map.numRows()) {
            return;
        }
        if (col < 0 || col >= map.numCols()) {
            return;
        }
        map.set(layerID, row, col, value);
        Logger.debug("x={} y={}: {} move {}", col, row, value, moveDir);
    }

    private void setColors(WorldMap map) {
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_DOOR, "#fcb5ff");
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_FILL, "#47b7ff");
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_STROKE, "#dedeff");
        map.properties(LayerID.FOOD).put(WorldMapProperty.COLOR_FOOD, "#ffff00");
    }

    public static void main(String[] args)  {
        int numMaps = 1;
        MazeMapGenerator mg = new MazeMapGenerator();
        for (int i = 0; i < numMaps; ++i) {
            WorldMap mazeMap = mg.createMazeMap(40, 30);
            mg.setColors(mazeMap);
            File file = new File("maze_%d.world".formatted(i));
            boolean saved = mazeMap.save(file);
            if (saved) {
                Logger.info("Map file created: {}", file.getAbsolutePath());
            } else {
                Logger.error("Could not save map file {}", file.getAbsolutePath());
            }
        }
    }
}