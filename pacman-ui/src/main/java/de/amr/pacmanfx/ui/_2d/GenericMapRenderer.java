/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static java.util.function.Predicate.not;

/**
 * Vector-based renderer for maze terrain, food, and ghost house.
 * Used by XXL and other dynamic-map variants via delegation.
 */
public class GenericMapRenderer extends BaseRenderer {

    public enum RenderInfoKey { TERRAIN_MAP_COLOR_SCHEME }

    private final TerrainMapVectorRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

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
        blinkingOnColors = new TerrainMapColorScheme(backgroundColor, backgroundColor, Color.WHITE, backgroundColor);
        blinkingOffColors = new TerrainMapColorScheme(backgroundColor, Color.WHITE, backgroundColor, backgroundColor);
        final TerrainMapColorScheme oldColorScheme = terrainRenderer.colorScheme();
        final TerrainMapColorScheme newColorScheme = new TerrainMapColorScheme(
            backgroundColor, oldColorScheme.wallFillColor(), oldColorScheme.wallStrokeColor(), oldColorScheme.doorColor()
        );
        terrainRenderer.setColorScheme(newColorScheme);
    }

    public void drawMap(GameLevel gameLevel, RenderInfo info) {
        final WorldMap worldMap = gameLevel.worldMap();
        if (info.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
            terrainRenderer.setColorScheme(info.getBoolean(CommonRenderInfoKey.ENERGIZER_ON) ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.draw(worldMap);
        }
        else {
            final TerrainMapColorScheme terrainColorScheme = info.get(RenderInfoKey.TERRAIN_MAP_COLOR_SCHEME, TerrainMapColorScheme.class);
            terrainRenderer.setColorScheme(terrainColorScheme);
            terrainRenderer.draw(worldMap);

            worldMap.terrainLayer().optHouse().ifPresent(house -> {
                houseRenderer.setColorScheme(terrainColorScheme);
                houseRenderer.drawHouse(house.minTile(), house.sizeInTiles(),
                    terrainRenderer.borderWallFullWidth(),terrainRenderer.borderWallInnerWidth());
            });

            // Color scheme is set by the map selector
            final FoodLayer foodLayer = worldMap.foodLayer();
            final WorldMapColorScheme foodColorScheme = worldMap.getConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME);
            final Color pelletColor = Color.valueOf(foodColorScheme.pellet());
            foodRenderer.setPelletColor(pelletColor);
            foodLayer.tiles()
                .filter(foodLayer::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .forEach(foodRenderer::drawPellet);

            if (info.getBoolean(CommonRenderInfoKey.ENERGIZER_ON)) {
                foodRenderer.setEnergizerColor(pelletColor);
                foodLayer.energizerTiles().stream()
                    .filter(foodLayer::hasFoodAtTile)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
    }
}