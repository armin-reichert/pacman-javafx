/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import de.amr.pacmanfx.mapeditor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.rendering.ArcadeHouseRenderer;
import de.amr.pacmanfx.uilib.rendering.FoodMapRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.rendering.TerrainMapVectorRenderer;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.EditorGlobals.ACTOR_SPRITES;
import static de.amr.pacmanfx.mapeditor.EditorUtil.getColorFromMap;

public class Preview2D extends Canvas {

    static class PreviewRenderer extends TerrainMapVectorRenderer implements ActorSpriteRenderer {
        public PreviewRenderer(Canvas canvas) {
            super(canvas);
        }
    }

    private final TerrainMapTileRenderer terrainTileRenderer;
    private final PreviewRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private final DoubleProperty gridSize = new SimpleDoubleProperty(TS);
    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);

    public Preview2D() {
        terrainRenderer = new PreviewRenderer(this);
        foodRenderer = new FoodMapRenderer(this);
        houseRenderer = new ArcadeHouseRenderer(this);
        houseRenderer.colorSchemeProperty().bind(terrainRenderer.colorSchemeProperty());

        // Fallback renderer for tiles that belong to incomplete obstacles
        terrainTileRenderer = new TerrainMapTileRenderer(this);
        terrainTileRenderer.setScatterTargetsDisplayed(false);
        terrainTileRenderer.setTunnelIconsDisplayed(false);
        terrainTileRenderer.setTerrainFilter((worldMap, tile) -> {
            byte code = worldMap.content(LayerID.TERRAIN, tile);
            return code == TerrainTile.ARC_SE.$
                || code == TerrainTile.ARC_NE.$
                || code == TerrainTile.ARC_SW.$
                || code == TerrainTile.ARC_NW.$
                || code == TerrainTile.WALL_H.$
                || code == TerrainTile.WALL_V.$;
        });

        DoubleBinding scaling = gridSize.divide(TS);
        terrainRenderer.scalingProperty().bind(scaling);
        terrainTileRenderer.scalingProperty().bind(scaling);
        foodRenderer.scalingProperty().bind(scaling);
        houseRenderer.scalingProperty().bind(scaling);
    }

    public DoubleProperty gridSizeProperty() {
        return gridSize;
    }

    public BooleanProperty terrainVisibleProperty() {
        return terrainVisible;
    }

    public BooleanProperty foodVisibleProperty() {
        return foodVisible;
    }

    public BooleanProperty actorsVisibleProperty() {
        return actorsVisible;
    }

    private void drawHouse(WorldMap worldMap) {
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
        if (minTile != null && maxTile != null) {
            double outerWidth = terrainRenderer.borderWallFullWidth();
            double innerWidth = terrainRenderer.borderWallInnerWidth();
            houseRenderer.drawHouse(minTile, maxTile.minus(minTile).plus(1, 1), outerWidth, innerWidth);
        }
    }

    public void draw(WorldMap worldMap, TerrainMapColorScheme colorScheme) {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(colorScheme.floorColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (terrainVisible.get()) {
            terrainRenderer.setColorScheme(colorScheme);
            terrainTileRenderer.setColorScheme(colorScheme);
            terrainTileRenderer.draw(worldMap);
            terrainRenderer.draw(worldMap);
            drawHouse(worldMap);
        }
        if (foodVisible.get()) {
            Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, ArcadeSprites.MS_PACMAN_COLOR_FOOD);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            worldMap.tiles().forEach(tile -> foodRenderer.drawTile(tile, worldMap.content(LayerID.FOOD, tile)));
        }
        if (actorsVisible.get()) {
            ACTOR_SPRITES.forEach((positionProperty, sprite) -> {
                Vector2i tile = worldMap.getTerrainTileProperty(positionProperty);
                if (tile != null) {
                    terrainRenderer.drawActorSprite(tile, sprite);
                }
            });
        }
    }
}
