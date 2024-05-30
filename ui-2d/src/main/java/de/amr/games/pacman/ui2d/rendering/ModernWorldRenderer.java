/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.tilemap.FoodMapRenderer;
import de.amr.games.pacman.ui2d.tilemap.TerrainMapRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.ui2d.tilemap.TileMapRenderer.getColorFromMap;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class ModernWorldRenderer {

    public ModernWorldRenderer(DoubleProperty scalingPy) {
        terrainRenderer.scalingPy.bind(scalingPy);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    public void draw(GraphicsContext g, World world, boolean flashing, boolean blinkingOn) {
        if (flashing) {
            drawTerrain(g, world, blinkingOn);
        } else {
            drawTerrain(g, world, false);
            drawFood(g, world, blinkingOn);
        }
    }

    public void drawTerrain(GraphicsContext g, World world, boolean hiLighted) {
        var terrainMap = world.map().terrain();
        terrainRenderer.setWallStrokeColor(hiLighted ? Color.WHITE : getColorFromMap(terrainMap, "wall_stroke_color", Color.WHITE));
        terrainRenderer.setWallFillColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, "wall_fill_color", Color.GREEN));
        terrainRenderer.setDoorColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, "door_color", Color.YELLOW));
        terrainRenderer.drawMap(g, terrainMap);
    }

    public void drawFood(GraphicsContext g, World world, boolean energizersOn) {
        var foodColor = getColorFromMap(world.map().food(), "food_color", Color.ORANGE);
        foodRenderer.setPelletColor(foodColor);
        foodRenderer.setEnergizerColor(foodColor);
        world.tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerTile)).forEach(tile -> foodRenderer.drawPellet(g, tile));
        if (energizersOn) {
            world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
        }
    }
}
