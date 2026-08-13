package de.amr.pacmanfx.core.model.world.map;

import de.amr.basics.math.Vector2i;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.pacmanfx.core.model.world.map.FoodTile.PELLET;
import static java.util.function.Predicate.not;

public class FoodState {

    private final FoodLayer foodLayer;
    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private final int totalFoodCount;
    private int remainingFoodCount;

    public FoodState(FoodLayer foodLayer) {
        this.foodLayer = foodLayer;
        eatenFoodBits = new BitSet(foodLayer.numCols() * foodLayer.numRows());
        totalFoodCount = (int) foodLayer.tilesContaining(PELLET.$).count() + foodLayer.energizerTiles().size();
        remainingFoodCount = totalFoodCount;
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
            eatenFoodBits.set(foodLayer.indexInRowWiseOrder(tile));
            --remainingFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAll() {
        foodLayer.tiles().filter(this::hasFoodAtTile).forEach(this::markFoodEatenAt);
    }

    public void eatPellets() {
        foodLayer.tiles().filter(this::hasFoodAtTile).filter(not(foodLayer::isEnergizerTile)).forEach(this::markFoodEatenAt);
    }

    public boolean hasFoodAtTile(Vector2i tile) {
        return foodLayer.isFoodTile(tile) && !hasEatenFoodAtTile(tile);
    }

    public boolean hasEatenFoodAtTile(Vector2i tile) {
        return !foodLayer.outOfBounds(tile) && eatenFoodBits.get(foodLayer.indexInRowWiseOrder(tile));
    }


}
