/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.*;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.tengenmspacman.sprites.MapImageSet;
import de.amr.pacmanfx.tengenmspacman.sprites.NES_WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableGameLevel;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends BaseRenderer {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_GameLevelRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        if (r instanceof RenderableGameLevel(GameLevel level, InfoMap renderInfo)) {
            renderGameLevel(level, renderInfo);
        }
    }

    private void renderGameLevel(GameLevel level, InfoMap renderInfo) {
        final WorldMap worldMap = level.worldMap();
        final TerrainLayer terrainLayer = worldMap.terrainLayer();
        final FoodLayer foodLayer = worldMap.foodLayer();

        drawMaze(renderInfo, 0, terrainLayer.emptyRowsOverMaze() * TS);
        overPaintActorSprites(level.entities().house(), terrainLayer);

        final FoodState foodState = level.food();
        final boolean blinkingOn = level.heartbeat().state() == Pulse.State.ON;
        drawFood(worldMap, foodLayer, foodState, blinkingOn);
    }

    private void drawMaze(InfoMap renderInfo, int x, int y) {
        final Image mazeImage = renderInfo.get(LevelRenderInfoKey.MAZE_IMAGE, Image.class);
        final RectShort mazeSprite = renderInfo.get(LevelRenderInfoKey.MAZE_SPRITE, RectShort.class);
        final int width = mazeSprite.width();
        final int height = mazeSprite.height();
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), width, height,
            scaled(x), scaled(y), scaled(width), scaled(height)
        );
    }

    private void drawFood(WorldMap worldMap, FoodLayer foodLayer, FoodState foodState, boolean blinkingOn) {
        final MapImageSet mapImageSet = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET);
        final NES_WorldMapColorScheme colorScheme = mapImageSet.mapImage().colorScheme();
        final Color pelletColor = Color.valueOf(colorScheme.pellet());

        ctx.save();
        ctx.scale(scaling(), scaling());
        drawPellets(foodLayer, foodState, pelletColor);
        drawEnergizers(foodLayer, foodState, pelletColor, blinkingOn);
        ctx.restore();
    }

    private void drawPellets(FoodLayer foodLayer, FoodState foodState, Color pelletColor) {
        foodLayer.tiles()
            .filter(foodLayer::isFoodTile)
            .filter(not(foodLayer::isEnergizerTile)).forEach(tile -> {
                // overpaint the pellet from the map image
                ctx.setFill(backgroundColor());
                fillSquareAtTileCenter(tile, 4);
                if (!foodState.hasEatenFoodAtTile(tile)) {
                    // draw pellet using the right color
                    ctx.setFill(pelletColor);
                    fillSquareAtTileCenter(tile, 2);
                }
        });
    }

    private void drawEnergizers(FoodLayer foodLayer, FoodState foodState, Color pelletColor, boolean blinkingOn) {
        final double size = TS;
        final double centerOffset = 0.5 * WorldMap.HTS;
        foodLayer.tiles().filter(foodLayer::isEnergizerTile).forEach(tile -> {
            // overpaint energizer pixels from map image
            ctx.setFill(backgroundColor());
            fillSquareAtTileCenter(tile, TS + 2);
            // draw energizer if not eaten and blinking is in ON phase
            if (!foodState.hasEatenFoodAtTile(tile) && blinkingOn) {
                final int x = tile.x() * TS;
                final int y = tile.y() * TS;
                // draw pixelated "circle"
                // TODO use sprite instead?
                ctx.setFill(pelletColor);
                ctx.fillRect(x + centerOffset, y, WorldMap.HTS, size);
                ctx.fillRect(x, y + centerOffset, size, WorldMap.HTS);
                ctx.fillRect(x + 1, y + 1, size - 2, size - 2);
            }
        });
    }

    private void overPaintActorSprites(House house, TerrainLayer terrain) {
        // Over-paint area at house bottom where the ghost sprites are shown in map
        final double margin = scaling();
        final double scaledTileSize = scaled(TS);
        final var inHouseArea = new Rectangle2D(
            0.5 * margin + scaledTileSize * (house.floorplan().minTile().x() + 1),
            0.5 * margin + scaledTileSize * (house.floorplan().minTile().y() + 2),
            scaledTileSize * (house.sizeInTiles().x() - 2) - margin,
            scaledTileSize * 2 - margin
        );

        ctx.setFill(backgroundColor());
        ctx.fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        final Vector2i pacTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_PAC, WorldMap.tile(14, 26));
        overPaintActorSprite(pacTile, margin);

        final Vector2i redGhostTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_GHOST_1_RED, WorldMap.tile(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        final double halfMargin = 0.5f * margin;
        final double overPaintSize = scaled(2 * TS) - margin;
        ctx.fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - WorldMap.HTS),
            overPaintSize, overPaintSize);
    }
}