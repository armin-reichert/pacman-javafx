/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Vector2i;
import org.tinylog.Logger;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.lib.worldmap.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.lib.worldmap.FoodTile.PELLET;

public class FoodLayer extends WorldMapLayer {

    public static boolean isValidFoodCode(byte code) {
        return Stream.of(FoodTile.values()).anyMatch(tile -> tile.$ == code);
    }

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
        if (tileContainsFood(tile)) {
            eatenFoodBits.set(indexInRowWiseOrder(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAllPellets() {
        tiles().filter(this::tileContainsFood).filter(tile -> !energizerTiles.contains(tile)).forEach(this::registerFoodEatenAt);
    }

    public void eatAllFood() {
        tiles().filter(this::tileContainsFood).forEach(this::registerFoodEatenAt);
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !outOfBounds(tile) && get(tile) != FoodTile.EMPTY.$;
    }

    public Set<Vector2i> energizerPositions() { return Collections.unmodifiableSet(energizerTiles); }

    public boolean isEnergizerPosition(Vector2i tile) {
        return energizerTiles.contains(tile);
    }

    public boolean tileContainsFood(Vector2i tile) {
        return isFoodPosition(tile) && !tileContainsEatenFood(tile);
    }

    public boolean tileContainsEatenFood(Vector2i tile) {
        return eatenFoodBits.get(indexInRowWiseOrder(tile));
    }
}