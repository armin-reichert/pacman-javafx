/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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

public class FoodLayer extends WorldMapLayer {

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private BitSet eatenFoodBits;
    private int totalFoodCount;
    private int uneatenFoodCount;
    private Set<Vector2i> energizerTiles;

    public FoodLayer(int numRows, int numCols) {
        super(numRows, numCols);
        initFood();
    }

    public FoodLayer(WorldMapLayer layer) {
        super(layer);
        initFood();
    }

    public void initFood() {
        energizerTiles = tilesContaining(ENERGIZER.$).collect(Collectors.toSet());
        eatenFoodBits = new BitSet(numCols() * numRows());
        totalFoodCount = (int) tilesContaining(PELLET.$).count() + energizerTiles.size();
        uneatenFoodCount = totalFoodCount;
    }

    public int totalFoodCount() {
        return totalFoodCount;
    }

    public int uneatenFoodCount() {
        return uneatenFoodCount;
    }

    public int eatenFoodCount() {
        return totalFoodCount - uneatenFoodCount;
    }

    public void registerFoodEatenAt(Vector2i tile) {
        if (hasFoodAtTile(tile)) {
            eatenFoodBits.set(indexInRowWiseOrder(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatPellets() {
        tiles().filter(this::hasFoodAtTile).filter(not(this::isEnergizerTile)).forEach(this::registerFoodEatenAt);
    }

    public void eatAll() {
        tiles().filter(this::hasFoodAtTile).forEach(this::registerFoodEatenAt);
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