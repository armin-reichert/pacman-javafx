/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfo;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.ArcadeHouseRenderer;
import de.amr.pacmanfx.uilib.rendering.FoodMapRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.rendering.TerrainMapVectorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.ui.api.GameUI_Config.PROPERTY_COLOR_MAP;
import static java.util.function.Predicate.not;

/**
 * Draws wall and obstacle paths generated at runtime from the 2D tile map data.
 */
public class GenericMapRenderer extends BaseCanvasRenderer {

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

        backgroundColorProperty().addListener((py, ov, newColor) -> updateColors(newColor));
        updateColors(backgroundColor());
    }

    private void updateColors(Color backgroundColor) {
        blinkingOnColors = new TerrainMapColorScheme(backgroundColor, backgroundColor, Color.WHITE, backgroundColor);
        blinkingOffColors = new TerrainMapColorScheme(backgroundColor, Color.WHITE, backgroundColor, backgroundColor);
        TerrainMapColorScheme oldColorScheme = terrainRenderer.colorScheme();
        TerrainMapColorScheme newColorScheme = new TerrainMapColorScheme(
            backgroundColor, oldColorScheme.wallFillColor(), oldColorScheme.wallStrokeColor(), oldColorScheme.doorColor()
        );
        terrainRenderer.setColorScheme(newColorScheme);
    }

    public void drawMaze(GameLevel gameLevel, RenderInfo info) {
        WorldMap worldMap = gameLevel.worldMap();
        if (info.getBoolean(CommonRenderInfo.MAZE_BRIGHT)) {
            terrainRenderer.setColorScheme(info.getBoolean(CommonRenderInfo.MAZE_BLINKING) ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.draw(worldMap, worldMap.obstacles());
        }
        else {
            TerrainMapColorScheme colorScheme = info.get("terrainMapColorScheme", TerrainMapColorScheme.class);
            terrainRenderer.setColorScheme(colorScheme);
            terrainRenderer.draw(worldMap, worldMap.obstacles());

            gameLevel.house().ifPresent(house -> {
                houseRenderer.setColorScheme(colorScheme);
                houseRenderer.drawHouse(house.minTile(), house.sizeInTiles(),
                    terrainRenderer.borderWallFullWidth(),terrainRenderer.borderWallInnerWidth());
            });

            // this is set by the map selector
            Map<String, String> colorMap = gameLevel.worldMap().getConfigValue(PROPERTY_COLOR_MAP);

            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            gameLevel.tiles()
                .filter(gameLevel::tileContainsFood)
                .filter(not(gameLevel::isEnergizerPosition))
                .forEach(foodRenderer::drawPellet);

            if (info.getBoolean(CommonRenderInfo.MAZE_BLINKING)) {
                foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
                gameLevel.energizerPositions().stream()
                    .filter(gameLevel::tileContainsFood)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
    }
}