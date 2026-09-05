/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static java.util.function.Predicate.not;

/**
 * Vector-based renderer for maze terrain, food, and ghost house.
 * Used by XXL and other dynamic-map variants via delegation.
 */
public class GenericLevelRenderer extends BaseRenderer {

    public enum RenderInfoKey {TERRAIN_MAP_COLORING}

    private final TerrainMapVectorRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private TerrainMapColoring blinkingOnMapColoring;
    private TerrainMapColoring blinkingOffMapColoring;

    private InfoMap infoMap;

    public GenericLevelRenderer(Canvas canvas) {
        super(canvas);

        terrainRenderer = new TerrainMapVectorRenderer(canvas);
        terrainRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        terrainRenderer.scalingProperty().bind(scalingProperty());

        foodRenderer = new FoodMapRenderer(canvas);
        foodRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        foodRenderer.scalingProperty().bind(scalingProperty());

        houseRenderer = new ArcadeHouseRenderer(canvas);
        houseRenderer.mapColoringProperty().bind(terrainRenderer.mapColoringProperty());
        houseRenderer.scalingProperty().bind(scalingProperty());

        backgroundColorProperty().addListener((_, _, newColor) -> updateColors(newColor));
        updateColors(backgroundColor());
    }

    public void setInfoMap(InfoMap infoMap) {
        this.infoMap = infoMap;
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof  GameLevel level)) {
            return;
        }
        if (infoMap.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
            terrainRenderer.setMapColoring(infoMap.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE) ? blinkingOnMapColoring : blinkingOffMapColoring);
            terrainRenderer.render(level.worldMap(), tick);
        }
        else {
            final TerrainMapColoring mapColoring = infoMap.get(RenderInfoKey.TERRAIN_MAP_COLORING, TerrainMapColoring.class);
            terrainRenderer.setMapColoring(mapColoring);
            terrainRenderer.render(level.worldMap(), tick);

            final House house = level.entities().house();
            if (house != null) {
                houseRenderer.render(house, tick);
            }

            // Color scheme is set by the map selector
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final GenericWorldMapColorScheme foodColorScheme = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
            final Color pelletColor = Color.valueOf(foodColorScheme.pellet());
            foodRenderer.setPelletColor(pelletColor);
            foodLayer.tiles()
                .filter(level.food()::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .forEach(foodRenderer::drawPellet);

            if (infoMap.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE)) {
                foodRenderer.setEnergizerColor(pelletColor);
                foodLayer.energizerTiles().stream()
                    .filter(level.food()::hasFoodAtTile)
                    .forEach(foodRenderer::drawEnergizer);
            }
        }
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
}