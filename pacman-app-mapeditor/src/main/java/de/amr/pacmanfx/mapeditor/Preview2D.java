/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.mapeditor.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import de.amr.pacmanfx.uilib.tilemap.ArcadeHouseRenderer;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapVectorRenderer;
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

    private final PreviewRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private final DoubleProperty gridSize = new SimpleDoubleProperty(TS);
    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);

    public Preview2D() {
        DoubleBinding scaling = gridSize.divide(TS);
        terrainRenderer = new PreviewRenderer(this);
        terrainRenderer.scalingProperty().bind(scaling);
        foodRenderer = new FoodMapRenderer(this);
        foodRenderer.scalingProperty().bind(scaling);
        houseRenderer = new ArcadeHouseRenderer(this);
        houseRenderer.scalingProperty().bind(scaling);
        houseRenderer.colorSchemeProperty().bind(terrainRenderer.colorSchemeProperty());
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
            terrainRenderer.draw(worldMap, worldMap.obstacles());
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
