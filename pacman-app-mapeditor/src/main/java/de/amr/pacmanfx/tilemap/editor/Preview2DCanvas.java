/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.ArcadeMap.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.parseColor;

public class Preview2DCanvas extends Canvas {

    private final TerrainMapRenderer terrainRenderer;
    private final FoodMapRenderer foodMapRenderer;
    private final EditorActorRenderer actorRenderer;

    private final DoubleProperty gridSize = new SimpleDoubleProperty(TS);

    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);

    public Preview2DCanvas() {
        terrainRenderer = new TerrainMapRenderer(this);
        terrainRenderer.scalingProperty().bind(gridSize.divide(TS));
        foodMapRenderer = new FoodMapRenderer(this);
        foodMapRenderer.scalingProperty().bind(gridSize.divide(TS));
        actorRenderer = new EditorActorRenderer(this);
        actorRenderer.scalingProperty().bind(gridSize.divide(TS));
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

    public void draw(WorldMap worldMap, TerrainMapColorScheme colorScheme) {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(colorScheme.floorColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (terrainVisible.get()) {
            terrainRenderer.setColorScheme(colorScheme);
            terrainRenderer.draw(worldMap, worldMap.obstacles());
            Vector2i houseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
            Vector2i houseMaxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
            if (houseMinTile != null && houseMaxTile != null) {
                terrainRenderer.drawHouse(houseMinTile, houseMaxTile.minus(houseMinTile).plus(1, 1));
            }
        }
        if (foodVisible.get()) {
            Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            worldMap.tiles().forEach(tile -> foodMapRenderer.drawTile(tile, worldMap.content(LayerID.FOOD, tile)));
        }
        if (actorsVisible.get()) {
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_PAC), PAC_SPRITE);
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_RED_GHOST), RED_GHOST_SPRITE);
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_PINK_GHOST), PINK_GHOST_SPRITE);
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_CYAN_GHOST), CYAN_GHOST_SPRITE);
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_ORANGE_GHOST), ORANGE_GHOST_SPRITE);
            actorRenderer.drawActor(worldMap.getTerrainTileProperty(WorldMapProperty.POS_BONUS), BONUS_SPRITE);
        }
    }
}
