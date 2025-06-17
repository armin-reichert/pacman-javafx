/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * A vector graphics renderer that draws wall and obstacle paths generated from the tile map data.
 */
public class VectorGraphicsGameRenderer implements SpriteGameRenderer {

    private final SpriteSheet<?> spriteSheet;
    private final GraphicsContext ctx;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private Color bgColor;
    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

    public VectorGraphicsGameRenderer(SpriteSheet<?> spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
        terrainRenderer.scalingProperty().bind(scalingPy);
        foodRenderer.scalingProperty().bind(scalingPy);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void applyRenderingHints(GameLevel level) {}

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    public void setBackgroundColor(Color color) {
        bgColor = requireNonNull(color);
        blinkingOnColors = new TerrainMapColorScheme(bgColor, Color.BLACK, Color.WHITE, Color.BLACK);
        blinkingOffColors = new TerrainMapColorScheme(bgColor, Color.WHITE, Color.BLACK, Color.BLACK);
    }

    @Override
    public void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        WorldMap worldMap = level.worldMap();
        if (mazeHighlighted) {
            terrainRenderer.setColorScheme(energizerHighlighted ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.drawTerrain(ctx, worldMap, worldMap.obstacles());
        }
        else {
            //TODO move into applyMapSettings?
            Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
            TerrainMapColorScheme colors = new TerrainMapColorScheme(
                bgColor,
                Color.web(colorMap.get("fill")),
                Color.web(colorMap.get("stroke")),
                Color.web(colorMap.get("door"))
            );
            terrainRenderer.setColorScheme(colors);

            terrainRenderer.drawTerrain(ctx, worldMap, worldMap.obstacles());
            terrainRenderer.drawHouse(ctx, level.houseMinTile(), level.houseSizeInTiles());
            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
            worldMap.tiles().filter(level::tileContainsFood).filter(not(level::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx, tile));
            if (energizerHighlighted) {
                level.energizerTiles().filter(level::tileContainsFood).forEach(tile -> foodRenderer.drawEnergizer(ctx, tile));
            }
        }
    }

}