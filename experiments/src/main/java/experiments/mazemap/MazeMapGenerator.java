/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package experiments.mazemap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.graph.GridGraph;
import de.amr.pacmanfx.lib.graph.GridGraphImpl;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MazeMapGenerator {

    private static final byte FREE = TerrainTile.EMPTY.$;
    private static final byte BLOCKED = TerrainTile.WALL_H.$;

    private static final int EMPTY_ROWS_ABOVE = 3, EMPTY_ROWS_BELOW = 2;

    public WorldMap createMazeMap(int numRows, int numCols) {
        // create maze graph
        GridGraph graph = new GridGraphImpl(numRows, numCols);
        MazeGenerators.createMazeByRecursiveDivision(graph);

        // create map from maze (3 times larger!)
        var map = new WorldMap(3 * numCols, 3 * numRows + EMPTY_ROWS_ABOVE + EMPTY_ROWS_BELOW);
        for (int v = 0; v < graph.numVertices(); ++v) {
            // center of 3x3 "macro" cell
            int row = 3 * graph.row(v) + 1 + EMPTY_ROWS_ABOVE, col = 3 * graph.col(v) + 1;
            if (row < 3 || row > 3 * numRows + EMPTY_ROWS_BELOW) {
                continue;
            }
            map.terrainLayer().setContent(row - 1, col - 1, BLOCKED);
            map.terrainLayer().setContent(row - 1, col, graph.connected(v, Direction.UP) ? FREE : BLOCKED);
            map.terrainLayer().setContent(row - 1, col + 1, BLOCKED);
            map.terrainLayer().setContent(row, col - 1, graph.connected(v, Direction.LEFT) ? FREE : BLOCKED);
            map.terrainLayer().setContent(row, col, FREE);
            map.terrainLayer().setContent(row, col + 1, graph.connected(v, Direction.RIGHT) ? FREE : BLOCKED);
            map.terrainLayer().setContent(row + 1, col - 1, BLOCKED);
            map.terrainLayer().setContent(row + 1, col, graph.connected(v, Direction.DOWN) ? FREE : BLOCKED);
            map.terrainLayer().setContent(row + 1, col + 1, BLOCKED);
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
        map.terrainLayer().setContent(current, TerrainTile.ARC_NW.$);
        moveDir = Direction.RIGHT;
        move();
        while (inRange(current, map)) {
            Vector2i tileAtRightHand = current.plus(moveDir.nextClockwise().vector());
            if (map.terrainLayer().content(tileAtRightHand) == FREE) {
                Vector2i tileAhead = current.plus(moveDir.vector());
                if (map.terrainLayer().content(tileAhead) == BLOCKED) {
                    map.terrainLayer().setContent(current,
                            moveDir.isHorizontal() ? TerrainTile.WALL_H.$
                                : TerrainTile.WALL_V.$);
                } else {
                    byte corner = switch (moveDir) {
                        case LEFT  -> TerrainTile.ARC_NW.$;
                        case RIGHT -> TerrainTile.ARC_SE.$;
                        case UP    -> TerrainTile.ARC_NE.$;
                        case DOWN  -> TerrainTile.ARC_SW.$;
                    };
                    map.terrainLayer().setContent(current, corner);
                    turnCounterclockwise();
                }
            } else {
                byte corner = switch (moveDir) {
                    case RIGHT -> TerrainTile.ARC_NE.$;
                    case DOWN  -> TerrainTile.ARC_SE.$;
                    case LEFT  -> TerrainTile.ARC_SW.$;
                    case UP    -> TerrainTile.ARC_NW.$;
                };
                map.terrainLayer().setContent(current, corner);
                turnClockwise();
            }
            move();
        }
        map.terrainLayer().setContent(predecessor, TerrainTile.WALL_V.$); //TODO correct?
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
                if (map.terrainLayer().content(row, col) == TerrainTile.EMPTY.$
                        && new Random().nextInt(100) < 40) {
                    map.foodLayer().setContent(row, col, FoodTile.PELLET.$);
                }
            }
        }
    }

    private void move() {
        predecessor = current;
        current = current.plus(moveDir.vector());
    }

    private void setColors(WorldMap map) {
        map.terrainLayer().propertyMap().put(DefaultWorldMapPropertyName.COLOR_DOOR, "#fcb5ff");
        map.terrainLayer().propertyMap().put(DefaultWorldMapPropertyName.COLOR_WALL_FILL, "#47b7ff");
        map.terrainLayer().propertyMap().put(DefaultWorldMapPropertyName.COLOR_WALL_STROKE, "#dedeff");
        map.foodLayer().propertyMap().put(DefaultWorldMapPropertyName.COLOR_FOOD, "#ffff00");
    }

    public static void main(String[] args)  {
        int numMaps = 1;
        MazeMapGenerator mg = new MazeMapGenerator();
        for (int i = 0; i < numMaps; ++i) {
            WorldMap mazeMap = mg.createMazeMap(40, 30);
            mg.setColors(mazeMap);
            File file = new File("maze_%d.world".formatted(i));
            boolean saved = saveWorldMap(mazeMap, file);
            if (saved) {
                Logger.info("Map file created: {}", file.getAbsolutePath());
            } else {
                Logger.error("Could not save map file {}", file.getAbsolutePath());
            }
        }
    }

    private static boolean saveWorldMap(WorldMap worldMap,File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(worldMap.sourceCode(false));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }
}