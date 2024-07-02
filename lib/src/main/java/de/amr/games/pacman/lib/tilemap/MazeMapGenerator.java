package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.lib.graph.DirMap;
import de.amr.games.pacman.lib.graph.GridGraph;
import de.amr.games.pacman.lib.graph.GridGraphImpl;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MazeMapGenerator {

    public static void createMazeByWilson(GridGraph grid) {
        List<Integer> vertices = IntStream.range(0, grid.numVertices()).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(vertices);
        BitSet inTree = new BitSet();
        inTree.set(vertices.get(0));
        DirMap lastWalkDir = new DirMap();
        for (int vertex : vertices) {
            loopErasedRandomWalk(grid, vertex, lastWalkDir, inTree);
        }
    }

    private static void loopErasedRandomWalk(GridGraph grid, int start, DirMap lastWalkDir, BitSet inTree) {
        // random walk until a tree vertex is touched
        int vertex = start;
        while (!inTree.get(vertex)) {
            Dir walkDir = Dir.random();
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                lastWalkDir.set(vertex, walkDir);
                vertex = neighbor;
            }
        }
        // add the (loop-erased) random walk to the tree
        vertex = start;
        while (!inTree.get(vertex)) {
            Dir walkDir = lastWalkDir.get(vertex);
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                inTree.set(vertex);
                grid.connect(vertex, walkDir);
                vertex = neighbor;
            }
        }
    }

    public static WorldMap createMazeMap(int numRows, int numCols) {
        GridGraphImpl grid = new GridGraphImpl(numRows, numCols);
        createMazeByWilson(grid);
        Logger.info("Maze: {}", grid);
        var worldMap = new WorldMap(3*numRows, 3*numCols);
        var terrain = worldMap.terrain();
        for (int v = 0; v < grid.numVertices(); ++v) {
            int row = 3*grid.row(v)+1, col = 3*grid.col(v)+1;
            byte free = Tiles.EMPTY;
            byte blocked = Tiles.TUNNEL;
            set(terrain, row - 1, col - 1, blocked);
            set(terrain, row - 1, col, grid.connected(v, Dir.N) ? free : blocked);
            set(terrain, row - 1, col + 1, blocked);
            set(terrain, row, col - 1, grid.connected(v, Dir.W) ? free : blocked);
            set(terrain, row, col, free);
            set(terrain, row, col + 1, grid.connected(v, Dir.E) ? free : blocked);
            set(terrain, row + 1, col - 1, blocked);
            set(terrain, row + 1, col, grid.connected(v, Dir.S) ? free : blocked);
            set(terrain, row + 1, col + 1, blocked);
        }
        return worldMap;
    }

    private static void set(TileMap map, int row, int col, byte value) {
        if (row < 0 || row >= map.numRows()) {
            return;
        }
        if (col < 0 || col >= map.numCols()) {
            return;
        }
        map.set(row, col, value);
    }

    public static void main(String[] args)  {
        WorldMap mazeMap = createMazeMap(10, 10);
        mazeMap.save(new File("maze.world"));
    }
}
