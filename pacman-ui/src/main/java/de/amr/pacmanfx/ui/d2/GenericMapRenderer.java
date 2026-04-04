/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static java.util.function.Predicate.not;

/**
 * Vector-based renderer for maze terrain, food, and ghost house.
 * Used by XXL and other dynamic-map variants via delegation.
 */
public class GenericMapRenderer extends BaseRenderer {

    public enum RenderInfoKey {TERRAIN_MAP_COLORING}

    private final TerrainMapVectorRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private TerrainMapColoring blinkingOnMapColoring;
    private TerrainMapColoring blinkingOffMapColoring;

    public GenericMapRenderer(Canvas canvas) {
        super(canvas);

        terrainRenderer = new TerrainMapVectorRenderer(canvas);
        terrainRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        terrainRenderer.scalingProperty().bind(scalingProperty());

        foodRenderer = new FoodMapRenderer(canvas);
        foodRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        foodRenderer.scalingProperty().bind(scalingProperty());

        houseRenderer = new ArcadeHouseRenderer(canvas);
        houseRenderer.scalingProperty().bind(scalingProperty());

        backgroundColorProperty().addListener((_, _, newColor) -> updateColors(newColor));
        updateColors(backgroundColor());
    }

    private void updateColors(Color backgroundColor) {
        blinkingOnMapColoring = new TerrainMapColoring(backgroundColor, backgroundColor, Color.WHITE, backgroundColor);
        blinkingOffMapColoring = new TerrainMapColoring(backgroundColor, Color.WHITE, backgroundColor, backgroundColor);
        final TerrainMapColoring oldColoring = terrainRenderer.colorScheme();
        final TerrainMapColoring newColoring = new TerrainMapColoring(
            backgroundColor, oldColoring.wallFillColor(), oldColoring.wallStrokeColor(), oldColoring.doorColor()
        );
        terrainRenderer.setMapColoring(newColoring);
    }

    public void drawMap(GameLevel gameLevel, RenderInfo info) {
        final WorldMap worldMap = gameLevel.worldMap();
        if (info.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
            terrainRenderer.setMapColoring(info.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE) ? blinkingOnMapColoring : blinkingOffMapColoring);
            terrainRenderer.draw(worldMap);
        }
        else {
            final TerrainMapColoring mapColoring = info.get(RenderInfoKey.TERRAIN_MAP_COLORING, TerrainMapColoring.class);
            terrainRenderer.setMapColoring(mapColoring);
            terrainRenderer.draw(worldMap);

            worldMap.terrainLayer().optHouse().ifPresent(house -> {
                houseRenderer.setMapColoring(mapColoring);
                houseRenderer.drawHouse(house.minTile(), house.sizeInTiles(),
                    terrainRenderer.borderWallFullWidth(),terrainRenderer.borderWallInnerWidth());
            });

            // Color scheme is set by the map selector
            final FoodLayer foodLayer = worldMap.foodLayer();
            final WorldMapColorScheme foodColorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
            final Color pelletColor = Color.valueOf(foodColorScheme.pellet());
            foodRenderer.setPelletColor(pelletColor);
            foodLayer.tiles()
                .filter(foodLayer::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .forEach(foodRenderer::drawPellet);

            if (info.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE)) {
                foodRenderer.setEnergizerColor(pelletColor);
                foodLayer.energizerTiles().stream()
                    .filter(foodLayer::hasFoodAtTile)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
    }
}