/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
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
public class GenericMapRenderer extends BaseRenderer {

    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

    public GenericMapRenderer(Canvas canvas) {
        super(canvas);
        terrainRenderer.scalingProperty().bind(scalingProperty());
        foodRenderer.scalingProperty().bind(scalingProperty());
        setBackgroundColor(Color.BLACK);
        backgroundColorProperty().addListener((py, ov, newColor) -> updateBlinkingColors(newColor));
        updateBlinkingColors(backgroundColor());
    }

    private void updateBlinkingColors(Color backgroundColor) {
        blinkingOnColors = new TerrainMapColorScheme(backgroundColor, Color.BLACK, Color.WHITE, Color.BLACK);
        blinkingOffColors = new TerrainMapColorScheme(backgroundColor, Color.WHITE, Color.BLACK, Color.BLACK);

    }

    public void drawLevel(GameLevel level, boolean mazeHighlighted, boolean energizerHighlighted) {
        WorldMap worldMap = level.worldMap();
        if (mazeHighlighted) {
            terrainRenderer.setColorScheme(energizerHighlighted ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.drawTerrain(ctx, worldMap, worldMap.obstacles());
        }
        else {
            //TODO move into applyMapSettings?
            Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
            TerrainMapColorScheme colors = new TerrainMapColorScheme(
                backgroundColor(),
                Color.web(colorMap.get("fill")),
                Color.web(colorMap.get("stroke")),
                Color.web(colorMap.get("door"))
            );
            terrainRenderer.setColorScheme(colors);

            terrainRenderer.drawTerrain(ctx, worldMap, worldMap.obstacles());
            level.house().ifPresent(house -> terrainRenderer.drawHouse(ctx, house.minTile(), house.sizeInTiles()));
            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
            worldMap.tiles().filter(level::tileContainsFood).filter(not(level::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx, tile));
            if (energizerHighlighted) {
                level.energizerPositions().stream().filter(level::tileContainsFood).forEach(tile -> foodRenderer.drawEnergizer(ctx, tile));
            }
        }
    }
}