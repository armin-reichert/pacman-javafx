/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.mapeditor.FoodMapRenderer;
import de.amr.games.pacman.mapeditor.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.mapeditor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class VectorGameWorldRenderer {

    public final DoubleProperty scalingPy = new SimpleDoubleProperty(1);

    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    public VectorGameWorldRenderer() {
        terrainRenderer.scalingPy.bind(scalingPy);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    public void draw(GraphicsContext g, GameWorld world, boolean flashMode, boolean highlighted) {
        TileMap terrain = world.map().terrain();
        Color wallStrokeColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_STROKE, Color.WHITE);
        Color wallFillColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_FILL, Color.GREEN);
        Color doorColor = getColorFromMap(terrain, PROPERTY_COLOR_DOOR, Color.YELLOW);
        if (flashMode) {
            terrainRenderer.setWallStrokeColor(highlighted ? Color.WHITE : Color.BLACK);
            terrainRenderer.setWallFillColor(highlighted   ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(g, terrain);
        }
        else {
            terrainRenderer.setWallStrokeColor(wallStrokeColor);
            terrainRenderer.setWallFillColor(wallFillColor);
            terrainRenderer.setDoorColor(doorColor);
            terrainRenderer.drawMap(g, terrain);
            Color foodColor = getColorFromMap(world.map().food(), PROPERTY_COLOR_FOOD, Color.ORANGE);
            foodRenderer.setPelletColor(foodColor);
            foodRenderer.setEnergizerColor(foodColor);
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition)).forEach(tile -> foodRenderer.drawPellet(g, tile));
            if (highlighted) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
            }
        }
    }
}