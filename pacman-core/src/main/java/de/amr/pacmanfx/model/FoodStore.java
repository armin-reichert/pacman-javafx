/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import org.tinylog.Logger;

import java.util.BitSet;
import java.util.Set;

import static de.amr.pacmanfx.lib.worldmap.FoodTile.PELLET;

public class FoodStore {

    private final WorldMap worldMap;
    private final Set<Vector2i> energizerTiles;

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    public FoodStore(WorldMap worldMap, Set<Vector2i> energizerTiles) {
        this.worldMap = worldMap;
        this.energizerTiles = energizerTiles;
        totalFoodCount = (int) worldMap.layer(LayerID.FOOD).tilesContaining(PELLET.$).count() + energizerTiles.size();
        uneatenFoodCount = totalFoodCount;
        eatenFoodBits = new BitSet(worldMap.numCols() * worldMap.numRows());
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
            eatenFoodBits.set(worldMap.indexInRowWiseOrder(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAllPellets() {
        worldMap.tiles().filter(this::tileContainsFood).filter(tile -> !energizerTiles.contains(tile)).forEach(this::registerFoodEatenAt);
    }

    public void eatAllFood() {
        worldMap.tiles().filter(this::tileContainsFood).forEach(this::registerFoodEatenAt);
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !worldMap.outOfWorld(tile) && worldMap.content(LayerID.FOOD, tile) != FoodTile.EMPTY.$;
    }

    public boolean tileContainsFood(Vector2i tile) {
        return isFoodPosition(tile) && !tileContainsEatenFood(tile);
    }

    public boolean tileContainsEatenFood(Vector2i tile) {
        return eatenFoodBits.get(worldMap.indexInRowWiseOrder(tile));
    }
}
