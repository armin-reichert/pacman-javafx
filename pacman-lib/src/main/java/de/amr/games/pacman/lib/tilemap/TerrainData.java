/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static java.util.function.Predicate.not;

/**
 * Computes the contour paths created by terrain tiles which are then used by the renderer. Outer paths like the
 * maze border are double-stroked, inner paths like normal obstacles are single-stroked. The ghost house is
 * double-stroked and uses angular corner tiles.
 */
public class TerrainData {

    private List<TileMapPath> singleStrokePaths = new ArrayList<>();
    private List<TileMapPath> doubleStrokePaths = new ArrayList<>();
    private List<TileMapPath> fillerPaths = new ArrayList<>();
    private List<Vector2i> topConcavityEntries = new ArrayList<>();
    private List<Vector2i> bottomConcavityEntries = new ArrayList<>();
    private List<Vector2i> leftConcavityEntries = new ArrayList<>();
    private List<Vector2i> rightConcavityEntries = new ArrayList<>();


    // New data structure
    private List<Obstacle> obstacles = new ArrayList<>();

    public List<Obstacle> obstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    private TerrainData() {}

    public TerrainData copy() {
        var copy = new TerrainData();
        copy.singleStrokePaths = new ArrayList<>(singleStrokePaths);
        copy.doubleStrokePaths = new ArrayList<>(doubleStrokePaths);
        copy.fillerPaths = new ArrayList<>(fillerPaths);
        copy.topConcavityEntries = new ArrayList<>(topConcavityEntries);
        copy.bottomConcavityEntries = new ArrayList<>(bottomConcavityEntries);
        copy.leftConcavityEntries = new ArrayList<>(leftConcavityEntries);
        copy.rightConcavityEntries = new ArrayList<>(rightConcavityEntries);
        return copy;
    }

    private boolean isExplored(BitSet exploredSet, TileMap terrain, Vector2i tile) {
        return exploredSet.get(terrain.index(tile));
    }

    private void setExplored(BitSet exploredSet, TileMap terrain, Vector2i tile) {
        exploredSet.set(terrain.index(tile));
    }

    public Stream<TileMapPath> singleStrokePaths() {
        return singleStrokePaths.stream();
    }

    public Stream<TileMapPath> doubleStrokePaths() {
        return doubleStrokePaths.stream();
    }

    public Stream<TileMapPath> fillerPaths() {
        return fillerPaths.stream();
    }

    public Stream<Vector2i> topConcavityEntries() {
        return topConcavityEntries.stream();
    }

    public Stream<Vector2i> bottomConcavityEntries() {
        return bottomConcavityEntries.stream();
    }

    public Stream<Vector2i> leftConcavityEntries() {
        return leftConcavityEntries.stream();
    }

    public Stream<Vector2i> rightConcavityEntries() {
        return rightConcavityEntries.stream();
    }

    public TerrainData(TileMap terrain) {

        // new
        obstacles = new ObstacleDetector(terrain).detectObstacles();


        // old

        BitSet exploredSet = new BitSet();

        int firstCol = 0, lastCol = terrain.numCols() - 1;
        Direction startDir;
        // Paths starting at left maze border leading inside maze
        for (int row = 0; row < terrain.numRows(); ++row) {
            byte firstColContent = terrain.get(row, firstCol);
            startDir = switch (firstColContent) {
                case Tiles.DWALL_H -> RIGHT;
                case Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> UP; // tunnel entry, path continues upwards
                case Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> RIGHT; // ??? why not down?
                default -> null; // should never happen
            };
            if (startDir != null) {
                addDoubleStrokePath(exploredSet, terrain, new Vector2i(firstCol, row), startDir);
            }
        }
        // Paths starting at right maze border leading inside maze
        for (int row = 0; row < terrain.numRows(); ++row) {
            byte lastColContent = terrain.get(row, lastCol);
            startDir = switch (lastColContent) {
                case Tiles.DWALL_H -> LEFT;
                case Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> UP; // tunnel entry, path continues upwards
                case Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> DOWN; // tunnel entry, path continues downwards
                default -> null; // should never happen
            };
            if (startDir != null) {
                addDoubleStrokePath(exploredSet, terrain, new Vector2i(lastCol, row), startDir);
            }
        }

        // Other closed double-stroke paths
        for (int row = 0; row < terrain.numRows(); ++row) {
            for (int col = 0; col < terrain.numCols(); ++col) {
                if (terrain.get(row, col) == Tiles.DWALL_V) {
                    addDoubleStrokePath(exploredSet, terrain, new Vector2i(col, row), DOWN);
                }
            }
        }

        // find ghost house, doors are included as walls!
        terrain.tiles(Tiles.DCORNER_ANGULAR_NW)
                .filter(not(tile -> isExplored(exploredSet, terrain, tile)))
                .filter(tile -> tile.x() > firstCol && tile.x() < lastCol)
                .map(corner -> computePath(exploredSet, terrain, corner, LEFT))
                .forEach(doubleStrokePaths::add);

        // add paths for obstacles inside maze, start with top-left corner of obstacle
        terrain.tiles(Tiles.CORNER_NW).filter(not(tile -> isExplored(exploredSet, terrain, tile)))
                .map(corner -> computePath(exploredSet, terrain, corner, LEFT))
                .forEach(singleStrokePaths::add);


        // create filler paths for concavities from maze border inside maze
        exploredSet.clear();

        {
            int topRow = 3; // TODO
            for (int col = 0; col < terrain.numCols() - 1; ++col) {
                if (terrain.get(topRow, col) == Tiles.DCORNER_NE && terrain.get(topRow, col + 1) == Tiles.DCORNER_NW) {
                    topConcavityEntries.add(new Vector2i(col, topRow));
                    Logger.debug("Found concavity entry at top row {} col {}", topRow, col);
                    Vector2i pathStartTile = new Vector2i(col, topRow + 1);
                    TileMapPath path = computePath(exploredSet, terrain, pathStartTile, DOWN,
                            tile -> terrain.outOfBounds(tile) || tile.equals(pathStartTile.plus(1, -1)));
                    path.add(UP.vector());
                    path.add(LEFT.vector());
                    path.add(DOWN.vector());
                    fillerPaths.add(path);
                }
            }
        }

        {
            int bottomRow = terrain.numRows() - 3; // TODO make this more general
            for (int col = 0; col < terrain.numCols() - 1; ++col) {
                if (terrain.get(bottomRow, col) == Tiles.DCORNER_SE && terrain.get(bottomRow, col + 1) == Tiles.DCORNER_SW) {
                    bottomConcavityEntries.add(new Vector2i(col, bottomRow));
                    Logger.debug("Found concavity entry at bottom row {} col {}", bottomRow, col);
                    Vector2i pathStartTile = new Vector2i(col, bottomRow - 1);
                    TileMapPath path = computePath(exploredSet, terrain, pathStartTile, UP,
                            tile -> terrain.outOfBounds(tile) || tile.equals(pathStartTile.plus(1, 1)));
                    path.add(LEFT.vector());
                    fillerPaths.add(path);
                }
            }
        }

        {
            int leftBorderCol = 0;
            for (int row = 0; row < terrain.numRows() - 1; ++row) {
                boolean roundedEntry = false; // get(row, leftBorderCol) == Tiles.DCORNER_SW && get(row + 1, leftBorderCol) == Tiles.DCORNER_NW;
                boolean straightEntry = terrain.get(row, leftBorderCol) == Tiles.DWALL_H && terrain.get(row + 1, leftBorderCol) == Tiles.DWALL_H;
                if (roundedEntry || straightEntry) {
                    leftConcavityEntries.add(new Vector2i(leftBorderCol, row));
                    Logger.debug("Found concavity entry at left border at row {}", row);
                    Vector2i pathStartTile = new Vector2i(roundedEntry ? 1 : 0, row);
                    TileMapPath path = computePath(exploredSet, terrain, pathStartTile, RIGHT,
                            tile -> tile.equals(pathStartTile.plus(roundedEntry ? 0 : -1, 1)));
                    fillerPaths.add(path);
                }
            }
        }

        {
            int rightBorderCol = terrain.numCols() - 1;
            for (int row = 0; row < terrain.numRows() - 1; ++row) {
                boolean roundedEntry = false; // get(row, rightBorderCol) == Tiles.DCORNER_SE && get(row + 1, rightBorderCol) == Tiles.DCORNER_NE;
                boolean straightEntry = terrain.get(row, rightBorderCol) == Tiles.DWALL_H && terrain.get(row + 1, rightBorderCol) == Tiles.DWALL_H;
                if (roundedEntry || straightEntry) {
                    rightConcavityEntries.add(new Vector2i(rightBorderCol, row));
                    Logger.debug("Found concavity entry at right border at row {}", row);
                    Vector2i pathStartTile = new Vector2i(rightBorderCol - (roundedEntry ? 1 : 0), row);
                    TileMapPath path = computePath(exploredSet, terrain, pathStartTile, LEFT,
                            tile -> tile.equals(pathStartTile.plus(roundedEntry ? 0 : 1, 1)));
                    fillerPaths.add(path);
                }
            }
        }

        exploredSet.clear();
        Logger.debug("Paths computed, {} single wall paths, {} double wall paths, {} filler paths",
                singleStrokePaths.size(), doubleStrokePaths.size(), fillerPaths.size());
    }

    // How the move direction changes when "traversing" a tile
    private Direction exitDirection(Direction incomingDir, byte tileType) {
        return switch (tileType) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW, Tiles.DCORNER_ANGULAR_NW -> incomingDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE, Tiles.DCORNER_ANGULAR_NE -> incomingDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE, Tiles.DCORNER_ANGULAR_SE -> incomingDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW, Tiles.DCORNER_ANGULAR_SW -> incomingDir == DOWN  ? RIGHT : UP;
            default -> incomingDir;
        };
    }

    private TileMapPath computePath(BitSet exploredSet, TileMap terrain,
        Vector2i startTile, Direction startDir, Predicate<Vector2i> stopCondition) {
        if (terrain.outOfBounds(startTile)) {
            throw new IllegalArgumentException("Start tile of path must be inside map");
        }
        TileMapPath path = new TileMapPath(startTile);
        var tile = startTile;
        var dir = startDir;
        while (true) {
            setExplored(exploredSet, terrain, tile);
            dir = exitDirection(dir, terrain.get(tile));
            tile = tile.plus(dir.vector());
            if (stopCondition.test(tile)) {
                break;
            }
            if (isExplored(exploredSet, terrain, tile)) {
                path.add(dir.vector());
                break;
            }
            path.add(dir.vector());
        }
        return path;
    }

    private TileMapPath computePath(BitSet exploredSet, TileMap terrain, Vector2i startTile, Direction startDir) {
        return computePath(exploredSet, terrain, startTile, startDir, terrain::outOfBounds);
    }

    private void addDoubleStrokePath(BitSet exploredSet, TileMap terrain, Vector2i startTile, Direction startDir) {
        if (!isExplored(exploredSet, terrain, startTile)) {
            doubleStrokePaths.add(computePath(exploredSet, terrain, startTile, startDir));
        }
    }
}