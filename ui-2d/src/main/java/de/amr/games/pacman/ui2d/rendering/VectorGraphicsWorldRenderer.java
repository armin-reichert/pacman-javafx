/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.mapeditor.FoodMapRenderer;
import de.amr.games.pacman.mapeditor.TerrainMapRenderer;
import de.amr.games.pacman.mapeditor.TileMapUtil;
import de.amr.games.pacman.model.world.World;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.mapeditor.TileMapUtil.getColorFromMap;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class VectorGraphicsWorldRenderer {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    public VectorGraphicsWorldRenderer() {
        terrainRenderer.scalingPy.bind(scalingPy);
        foodRenderer.scalingPy.bind(scalingPy);
    }

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
        terrainRenderer.setWallStrokeColor(hiLighted ? Color.WHITE : getColorFromMap(terrainMap, WorldMap.PROPERTY_WALL_STROKE_COLOR, Color.WHITE));
        terrainRenderer.setWallFillColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, WorldMap.PROPERTY_WALL_FILL_COLOR, Color.GREEN));
        terrainRenderer.setDoorColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, WorldMap.PROPERTY_DOOR_COLOR, Color.YELLOW));
        terrainRenderer.drawMap(g, terrainMap);
    }

    public void drawFood(GraphicsContext g, World world, boolean energizersOn) {
        var foodColor = getColorFromMap(world.map().food(), WorldMap.PROPERTY_FOOD_COLOR, Color.ORANGE);
        foodRenderer.setPelletColor(foodColor);
        foodRenderer.setEnergizerColor(foodColor);
        world.tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerTile)).forEach(tile -> foodRenderer.drawPellet(g, tile));
        if (energizersOn) {
            world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
        }
    }
}
