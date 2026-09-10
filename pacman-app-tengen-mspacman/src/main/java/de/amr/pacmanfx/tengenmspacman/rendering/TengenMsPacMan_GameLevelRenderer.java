/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.entities.Door;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.door.comp.DoorLayoutComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.*;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.sprites.*;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Common_GameLevelRendererKey;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Objects;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer {

    /**
     * Strange map #15 (maze #32) has a "psychedelic" animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private static int strangeMap15AnimationFrame(long tick) {
        final long phase = (tick % 32) / 8;
        return (int) (phase < 3 ? phase : 1);
    }

    public TengenMsPacMan_GameLevelRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        Objects.requireNonNull(r);
        if (r instanceof GameLevel level) {
            drawLevel(level, tick);
        } else {
            throw new IllegalArgumentException("Cannot draw object of class %s".formatted(r.getClass()));
        }
    }

    private void drawLevel(GameLevel level, long tick) {
        final WorldMap worldMap = level.worldMap();
        final TerrainLayer terrainLayer = worldMap.terrainLayer();
        final FoodLayer foodLayer = worldMap.foodLayer();

        // store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET)) {
            final int numFlashes = 3;
            final MapImageSet mapImageSet = TengenMsPacMan_MapRepository.instance().createMapImageSet(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET, mapImageSet);
            Logger.debug("Maze sprite set created: {}", mapImageSet);
        }

        if (info.getBoolean(Common_GameLevelRendererKey.BRIGHT)) {
            final int flashingIndex = info.get(Common_GameLevelRendererKey.FLASHING_INDEX, Integer.class);
            configureHighlightedMapRenderInfo(info, worldMap, flashingIndex);
        }
        else {
            final MapCategory mapCategory = info.get(TengenMsPacMan_GameLevelRendererKey.MAP_CATEGORY, MapCategory.class);
            configureNormalMapRenderInfo(info, mapCategory, worldMap, tick);
        }

        drawMaze(0, terrainLayer.emptyRowsOverMaze() * TS);
        overPaintActorSprites(level.entities().house(), terrainLayer);

        final FoodState foodState = level.food();
        final boolean blinkingOn = level.heartbeat().state() == Pulse.State.ON;
        drawFood(worldMap, foodLayer, foodState, blinkingOn);
    }

    private void drawMaze(int x, int y) {
        final Image mazeImage = info.get(Common_GameLevelRendererKey.IMAGE, Image.class);
        final RectShort mazeSprite = info.get(Common_GameLevelRendererKey.SPRITE, RectShort.class);
        final int width = mazeSprite.width();
        final int height = mazeSprite.height();
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), width, height,
            scaled(x), scaled(y), scaled(width), scaled(height)
        );
    }

    private void drawFood(WorldMap worldMap, FoodLayer foodLayer, FoodState foodState, boolean blinkingOn) {
        final MapImageSet recoloredMazeSprites = worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET);
        final NES_WorldMapColorScheme colorScheme = recoloredMazeSprites.mapImage().colorScheme();
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

    private void configureHighlightedMapRenderInfo(InfoMap info, WorldMap worldMap, int flashingIndex) {
        final MapImageSet imageSet = worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET);
        final int i = Math.clamp(flashingIndex, 0, imageSet.flashingMapImages().size() - 1);
        final ColorSchemedMapSprite flashingMapImage = imageSet.flashingMapImages().get(i);
        info.put(Common_GameLevelRendererKey.IMAGE, flashingMapImage.spriteSheetImage());
        info.put(Common_GameLevelRendererKey.SPRITE, flashingMapImage.sprite());
    }

    private void configureNormalMapRenderInfo(InfoMap info, MapCategory mapCategory, WorldMap worldMap, long tick) {
        final MapImageSet imageSet = worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET);
        info.put(Common_GameLevelRendererKey.IMAGE, imageSet.mapImage().spriteSheetImage());
        final int mapNumber = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
            final int spriteIndex = strangeMap15AnimationFrame(tick);
            info.put(Common_GameLevelRendererKey.SPRITE, NonArcadeMapsSpriteSheet.instance().findSpriteSequence(MAP32_ANIMATED)[spriteIndex]);
        } else {
            info.put(Common_GameLevelRendererKey.SPRITE, imageSet.mapImage().sprite());
        }
    }
}