/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package experiments.mazemap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.graph.DirMap;
import de.amr.pacmanfx.lib.graph.GridGraph;

import java.util.*;
import java.util.stream.IntStream;

public interface MazeGenerators {

    // Wilson

    static void createMazeByWilsonAlgorithm(GridGraph grid) {
        List<Integer> vertices = new ArrayList<>(IntStream.range(0, grid.numVertices()).boxed().toList());
        Collections.shuffle(vertices);
        BitSet visited = new BitSet();
        visited.set(vertices.getFirst());
        DirMap lastWalkDir = new DirMap();
        for (int vertex : vertices) {
            loopErasedRandomWalk(grid, vertex, lastWalkDir, visited);
        }
    }

    private static void loopErasedRandomWalk(GridGraph grid, int start, DirMap lastWalkDir, BitSet visited) {
        // random walk until a tree vertex is touched
        int vertex = start;
        while (!visited.get(vertex)) {
            Direction walkDir = Direction.random();
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                lastWalkDir.set(vertex, walkDir);
                vertex = neighbor;
            }
        }
        // add the (loop-erased) random walk to the tree
        vertex = start;
        while (!visited.get(vertex)) {
            Direction walkDir = lastWalkDir.get(vertex);
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                visited.set(vertex);
                grid.connect(vertex, walkDir);
                vertex = neighbor;
            }
        }
    }

    // Recursive-Division

    static void createMazeByRecursiveDivision(GridGraph grid) {
        for (int row = 0; row < grid.numRows(); ++row) {
            for (int col = 0; col < grid.numCols(); ++col) {
                int vertex = grid.vertex(row, col);
                if (row > 0) {
                    grid.connect(vertex, Direction.UP);
                }
                if (col > 0) {
                    grid.connect(vertex, Direction.LEFT);
                }
            }
        }
        divide(grid, new Random(), 0, 0, grid.numCols(), grid.numRows());
    }

    private static void divide(GridGraph grid, Random rnd, int x0, int y0, int w, int h) {
        if (w <= 1 && h <= 1) {
            return;
        }
        if (w < h || (w == h && rnd.nextBoolean())) {
            // Build "horizontal wall" at random y from [y0 + 1, y0 + h - 1], keep random door
            int y = y0 + 1 + rnd.nextInt(h - 1);
            int door = x0 + rnd.nextInt(w);
            for (int x = x0; x < x0 + w; ++x) {
                if (x != door) {
                    grid.disconnect(grid.vertex(y - 1, x), Direction.DOWN);
                }
            }
            divide(grid, rnd, x0, y0, w, y - y0);
            divide(grid, rnd, x0, y, w, h - (y - y0));
        } else {
            // Build "vertical wall" at random x from [x0 + 1, x0 + w - 1], keep random door
            int x = x0 + 1 + rnd.nextInt(w - 1);
            int door = y0 + rnd.nextInt(h);
            for (int y = y0; y < y0 + h; ++y) {
                if (y != door) {
                    grid.disconnect(grid.vertex(y, x - 1), Direction.RIGHT);
                }
            }
            divide(grid, rnd, x0, y0, x - x0, h);
            divide(grid, rnd, x, y0, w - (x - x0), h);
        }
    }
}