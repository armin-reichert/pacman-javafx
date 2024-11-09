/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Data describing paths/obstacles in terrain maps.
 */
public class TerrainData {
    final BitSet exploredSet = new BitSet();
    List<TileMapPath> singleStrokePaths = new ArrayList<>();
    List<TileMapPath> doubleStrokePaths = new ArrayList<>();
    List<TileMapPath> fillerPaths = new ArrayList<>();
    List<Vector2i> topConcavityEntries = new ArrayList<>();
    List<Vector2i> bottomConcavityEntries = new ArrayList<>();
    List<Vector2i> leftConcavityEntries = new ArrayList<>();
    List<Vector2i> rightConcavityEntries = new ArrayList<>();

    TerrainData() {}

    TerrainData(TerrainData other) {
        singleStrokePaths = new ArrayList<>(other.singleStrokePaths);
        doubleStrokePaths = new ArrayList<>(other.doubleStrokePaths);
        fillerPaths = new ArrayList<>(other.fillerPaths);
        topConcavityEntries = new ArrayList<>(other.topConcavityEntries);
        bottomConcavityEntries = new ArrayList<>(other.bottomConcavityEntries);
        leftConcavityEntries = new ArrayList<>(other.bottomConcavityEntries);
        rightConcavityEntries = new ArrayList<>(other.bottomConcavityEntries);
    }

    boolean isExplored(TileMap terrain, Vector2i tile) {
        return exploredSet.get(terrain.index(tile));
    }

    void setExplored(TileMap terrain, Vector2i tile) {
        exploredSet.set(terrain.index(tile));
    }

    void clearExploredSet() {
        exploredSet.clear();
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
}