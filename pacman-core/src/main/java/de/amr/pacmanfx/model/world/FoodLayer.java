/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.model.world.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.model.world.FoodTile.PELLET;
import static java.util.function.Predicate.not;

public final class FoodLayer extends WorldMapLayer {

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private int totalFoodCount;
    private int remainingFoodCount;
    private Set<Vector2i> energizerTiles;

    public FoodLayer(int numRows, int numCols) {
        super(numRows, numCols);
        eatenFoodBits = new BitSet(numCols() * numRows());
        initFoodCount();
    }

    public FoodLayer(FoodLayer layer) {
        super(layer);
        eatenFoodBits = new BitSet(numCols() * numRows());
        initFoodCount();
    }

    public void initFoodCount() {
        energizerTiles = tilesContaining(ENERGIZER.$).collect(Collectors.toSet());
        remainingFoodCount = totalFoodCount = (int) tilesContaining(PELLET.$).count() + energizerTiles.size();
    }

    public int totalFoodCount() {
        return totalFoodCount;
    }

    public int remainingFoodCount() {
        return remainingFoodCount;
    }

    public int eatenFoodCount() {
        return totalFoodCount - remainingFoodCount;
    }

    public void markFoodEatenAt(Vector2i tile) {
        if (hasFoodAtTile(tile)) {
            eatenFoodBits.set(indexInRowWiseOrder(tile));
            --remainingFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAll() {
        tiles().filter(this::hasFoodAtTile).forEach(this::markFoodEatenAt);
    }

    public void eatPellets() {
        tiles().filter(this::hasFoodAtTile).filter(not(this::isEnergizerTile)).forEach(this::markFoodEatenAt);
    }

    public Set<Vector2i> energizerTiles() { return Collections.unmodifiableSet(energizerTiles); }

    public boolean isEnergizerTile(Vector2i tile) {
        return !outOfBounds(tile) && energizerTiles.contains(tile);
    }

    public boolean isFoodTile(Vector2i tile) {
        return !outOfBounds(tile) && content(tile) != FoodTile.EMPTY.$;
    }

    public boolean hasFoodAtTile(Vector2i tile) {
        return isFoodTile(tile) && !hasEatenFoodAtTile(tile);
    }

    public boolean hasEatenFoodAtTile(Vector2i tile) {
        return !outOfBounds(tile) && eatenFoodBits.get(indexInRowWiseOrder(tile));
    }
}