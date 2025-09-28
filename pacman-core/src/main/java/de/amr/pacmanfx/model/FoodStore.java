/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import org.tinylog.Logger;

import java.util.BitSet;
import java.util.Set;

import static de.amr.pacmanfx.lib.worldmap.FoodTile.PELLET;
import static java.util.Objects.requireNonNull;

public class FoodStore {

    private final WorldMapLayer foodLayer;
    private final Set<Vector2i> energizerTiles;

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    public FoodStore(WorldMapLayer foodLayer, Set<Vector2i> energizerTiles) {
        this.foodLayer = requireNonNull(foodLayer);
        this.energizerTiles = requireNonNull(energizerTiles);
        totalFoodCount = (int) foodLayer.tilesContaining(PELLET.$).count() + energizerTiles.size();
        uneatenFoodCount = totalFoodCount;
        eatenFoodBits = new BitSet(foodLayer.numCols() * foodLayer.numRows());
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
        if (tileContainsFood(tile)) {
            eatenFoodBits.set(foodLayer.indexInRowWiseOrder(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAllPellets() {
        foodLayer.tiles().filter(this::tileContainsFood).filter(tile -> !energizerTiles.contains(tile)).forEach(this::registerFoodEatenAt);
    }

    public void eatAllFood() {
        foodLayer.tiles().filter(this::tileContainsFood).forEach(this::registerFoodEatenAt);
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !foodLayer.outOfBounds(tile) && foodLayer.get(tile) != FoodTile.EMPTY.$;
    }

    public boolean tileContainsFood(Vector2i tile) {
        return isFoodPosition(tile) && !tileContainsEatenFood(tile);
    }

    public boolean tileContainsEatenFood(Vector2i tile) {
        return eatenFoodBits.get(foodLayer.indexInRowWiseOrder(tile));
    }
}