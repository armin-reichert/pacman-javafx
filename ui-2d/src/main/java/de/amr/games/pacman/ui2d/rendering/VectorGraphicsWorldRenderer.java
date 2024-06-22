/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.mapeditor.FoodMapRenderer;
import de.amr.games.pacman.mapeditor.TerrainMapRenderer;
import de.amr.games.pacman.model.world.World;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
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
        var terrainMap = world.map().terrain();
        if (flashing) {
            terrainRenderer.setWallStrokeColor(blinkingOn ? Color.WHITE : getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, Color.WHITE));
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, Color.GREEN));
            terrainRenderer.setDoorColor(blinkingOn ? Color.BLACK : getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, Color.YELLOW));
            terrainRenderer.drawMap(g, terrainMap);
        } else {
            terrainRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, Color.WHITE));
            terrainRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, Color.GREEN));
            terrainRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, Color.YELLOW));
            terrainRenderer.drawMap(g, terrainMap);
            var foodColor = getColorFromMap(world.map().food(), PROPERTY_COLOR_FOOD, Color.ORANGE);
            foodRenderer.setPelletColor(foodColor);
            foodRenderer.setEnergizerColor(foodColor);
            world.tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerTile)).forEach(tile -> foodRenderer.drawPellet(g, tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
            }
        }
    }
}
