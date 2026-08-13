/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.world.map;

import de.amr.basics.math.Vector2i;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.core.model.world.map.FoodTile.ENERGIZER;

public final class FoodLayer extends WorldMapLayer {

    private final Set<Vector2i> energizerTiles;

    public FoodLayer(int numRows, int numCols) {
        super(numRows, numCols);
        energizerTiles = tilesContaining(ENERGIZER.$).collect(Collectors.toSet());
    }

    public FoodLayer(FoodLayer layer) {
        super(layer);
        energizerTiles = tilesContaining(ENERGIZER.$).collect(Collectors.toSet());
    }

    public Set<Vector2i> energizerTiles() { return Collections.unmodifiableSet(energizerTiles); }

    public boolean isEnergizerTile(Vector2i tile) {
        return !outOfBounds(tile) && energizerTiles.contains(tile);
    }

    public boolean isFoodTile(Vector2i tile) {
        return !outOfBounds(tile) && content(tile) != FoodTile.EMPTY.$;
    }
}