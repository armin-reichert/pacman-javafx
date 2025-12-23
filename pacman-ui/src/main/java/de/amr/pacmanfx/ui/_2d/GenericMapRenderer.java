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
import javafx.scene.paint.Paint;

import static java.util.function.Predicate.not;

/**
 * Draws wall and obstacle paths generated at runtime from the 2D tile map data.
 */
public class GenericMapRenderer extends BaseRenderer {

    private final TerrainMapVectorRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

    public GenericMapRenderer(Canvas canvas) {
        super(canvas);

        terrainRenderer = new TerrainMapVectorRenderer(canvas);
        terrainRenderer.backgroundProperty().bind(backgroundProperty());
        terrainRenderer.scalingProperty().bind(scalingProperty());

        foodRenderer = new FoodMapRenderer(canvas);
        foodRenderer.backgroundProperty().bind(backgroundProperty());
        foodRenderer.scalingProperty().bind(scalingProperty());

        houseRenderer = new ArcadeHouseRenderer(canvas);
        houseRenderer.scalingProperty().bind(scalingProperty());

        backgroundProperty().addListener((_, _, newColor) -> updateColors(newColor));
        updateColors(background());
    }

    private void updateColors(Paint background) {
        if (background instanceof Color backgroundColor) {
            blinkingOnColors = new TerrainMapColorScheme(backgroundColor, backgroundColor, Color.WHITE, backgroundColor);
            blinkingOffColors = new TerrainMapColorScheme(backgroundColor, Color.WHITE, backgroundColor, backgroundColor);
            TerrainMapColorScheme oldColorScheme = terrainRenderer.colorScheme();
            TerrainMapColorScheme newColorScheme = new TerrainMapColorScheme(
                backgroundColor, oldColorScheme.wallFillColor(), oldColorScheme.wallStrokeColor(), oldColorScheme.doorColor()
            );
            terrainRenderer.setColorScheme(newColorScheme);
        }
    }

    public void drawMaze(GameLevel gameLevel, RenderInfo info) {
        WorldMap worldMap = gameLevel.worldMap();
        if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
            terrainRenderer.setColorScheme(info.getBoolean(CommonRenderInfoKey.ENERGIZER_BLINKING) ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.draw(worldMap);
        }
        else {
            TerrainMapColorScheme terrainColorScheme = info.get("terrainMapColorScheme", TerrainMapColorScheme.class);
            terrainRenderer.setColorScheme(terrainColorScheme);
            terrainRenderer.draw(worldMap);

            gameLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> {
                houseRenderer.setColorScheme(terrainColorScheme);
                houseRenderer.drawHouse(house.minTile(), house.sizeInTiles(),
                    terrainRenderer.borderWallFullWidth(),terrainRenderer.borderWallInnerWidth());
            });

            // this is set by the map selector
            WorldMapColorScheme foodColorScheme = gameLevel.worldMap().getConfigValue(GameUI_Config.CONFIG_KEY_COLOR_SCHEME);
            final Color pelletColor = Color.web(foodColorScheme.pellet());
            foodRenderer.setPelletColor(pelletColor);
            FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(foodLayer::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .forEach(foodRenderer::drawPellet);

            if (info.getBoolean(CommonRenderInfoKey.ENERGIZER_BLINKING)) {
                foodRenderer.setEnergizerColor(pelletColor);
                foodLayer.energizerTiles().stream()
                    .filter(foodLayer::hasFoodAtTile)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
    }
}