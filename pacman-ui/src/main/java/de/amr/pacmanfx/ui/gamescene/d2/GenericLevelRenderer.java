/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.uilib.renderer.ArcadeHouseRenderer;
import de.amr.pacmanfx.uilib.renderer.FoodMapRenderer;
import de.amr.pacmanfx.uilib.renderer.TerrainMapVectorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelView;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Vector-based renderer for maze terrain, food, and ghost house.
 * Used by XXL and other dynamic-map variants via delegation.
 */
public class GenericLevelRenderer extends BaseRenderer {

    public enum RenderInfoKey {TERRAIN_MAP_COLORING, PELLET_COLOR}

    private final TerrainMapVectorRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final ArcadeHouseRenderer houseRenderer;

    private TerrainMapColoring blinkingOnMapColoring;
    private TerrainMapColoring blinkingOffMapColoring;

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

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevelView(GameLevel level, InfoMap renderInfo, RenderingLayer _, int _, Vector2f _)
                -> draw(level, renderInfo);
            case GameEntityView(House house, RenderingLayer _, int _, Vector2f _, InfoMap renderInfo)
                -> houseRenderer.drawHouse(house, renderInfo);
            case GameEntityView(Energizer energizer, RenderingLayer _, int _, Vector2f _, InfoMap renderInfo)
                -> draw(energizer, renderInfo);
            default
                -> super.render(r, tick);
        }
    }

    public void draw(GameLevel level, InfoMap renderInfo) {
        requireNonNull(level);
        requireNonNull(renderInfo);

        if (renderInfo.getBoolean(LevelRenderInfoKey.SHOW_BRIGHT_MAZE)) {
            terrainRenderer.setMapColoring(renderInfo.getBoolean(LevelRenderInfoKey.ENERGIZERS_SHOWN) ? blinkingOnMapColoring : blinkingOffMapColoring);
            terrainRenderer.draw(level.worldMap());
        }
        else {
            final TerrainMapColoring mapColoring = renderInfo.get(RenderInfoKey.TERRAIN_MAP_COLORING, TerrainMapColoring.class);
            terrainRenderer.setMapColoring(mapColoring);
            terrainRenderer.draw(level.worldMap());

            // Color scheme is set by the map selector
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final GenericWorldMapColorScheme foodColorScheme = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
            final Color pelletColor = Color.valueOf(foodColorScheme.pellet());
            foodRenderer.setPelletColor(pelletColor);
            foodLayer.tiles()
                .filter(level.food()::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .forEach(foodRenderer::drawPellet);

/*
            if (renderInfo.getBoolean(LevelRenderInfoKey.ENERGIZERS_SHOWN)) {
                foodRenderer.setEnergizerColor(pelletColor);
                foodLayer.energizerTiles().stream()
                    .filter(level.food()::hasFoodAtTile)
                    .forEach(foodRenderer::drawEnergizer);
            }
 */
        }
    }

    private void draw(Energizer energizer, InfoMap renderInfo) {
        if (energizer.isVisible() && energizer.on()) {
            final Color pelletColor = renderInfo.get(GenericLevelRenderer.RenderInfoKey.PELLET_COLOR, Color.class);
            final Vector2f center = energizer.pos().asVector2f();
            ctx.save();
            ctx.setFill(pelletColor);
            ctx.scale(scaling(), scaling());
            ctx.fillOval(center.x(), center.y(), 8, 8);
            ctx.restore();
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