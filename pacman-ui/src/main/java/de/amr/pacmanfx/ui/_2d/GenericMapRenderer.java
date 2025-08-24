/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static java.util.function.Predicate.not;

/**
 * Draws wall and obstacle paths generated at runtime from the 2D tile map data.
 */
public class GenericMapRenderer extends BaseCanvasRenderer {

    private final TerrainMapRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

    public GenericMapRenderer(Canvas canvas) {
        super(canvas);
        terrainRenderer = new TerrainMapRenderer(canvas);
        terrainRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        terrainRenderer.scalingProperty().bind(scalingProperty());
        foodRenderer = new FoodMapRenderer(canvas);
        foodRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        foodRenderer.scalingProperty().bind(scalingProperty());
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
        if (info.getBoolean("bright")) {
            terrainRenderer.setColorScheme(info.getBoolean("blinkingOn") ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.drawTerrain(worldMap, worldMap.obstacles());
        }
        else {
            TerrainMapColorScheme colorScheme = info.get("terrainMapColorScheme", TerrainMapColorScheme.class);
            terrainRenderer.setColorScheme(colorScheme);
            terrainRenderer.drawTerrain(worldMap, worldMap.obstacles());

            gameLevel.house().ifPresent(house -> terrainRenderer.drawHouse(house.minTile(), house.sizeInTiles()));

            // this is set by the map selector
            Map<String, String> colorMap = gameLevel.worldMap().getConfigValue("colorMap");

            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            gameLevel.tiles()
                .filter(gameLevel::tileContainsFood)
                .filter(not(gameLevel::isEnergizerPosition))
                .forEach(foodRenderer::drawPellet);

            if (info.getBoolean("blinkingOn")) {
                foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
                gameLevel.energizerPositions().stream()
                    .filter(gameLevel::tileContainsFood)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
    }
}