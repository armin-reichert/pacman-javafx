/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.MapConfigKey;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.Vector2i.vec2_int;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRendererMixin, GameLevelRenderer {

    /**
     * Strange map #15 (maze #32) has a "psychedelic" animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private static int strangeMap15AnimationFrame(long tick) {
        final long phase = (tick % 32) / 8;
        return (int) (phase < 3 ? phase : 1);
    }

    private final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_GameLevelRenderer(Canvas canvas, TengenMsPacMan_UIConfig uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void applyLevelSettings(GameLevel level, RenderInfo info) {
        final WorldMap worldMap = level.worldMap();
        // store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(MapConfigKey.MAP_IMAGE_SET)) {
            final int numFlashes = level.numFlashes();
            final MapImageSet mapImageSet = TengenMsPacMan_MapRepository.instance().createMazeSpriteSet(worldMap, numFlashes);
            worldMap.setConfigValue(MapConfigKey.MAP_IMAGE_SET, mapImageSet);
            Logger.debug("Maze sprite set created: {}", mapImageSet);
        }
    }

    @Override
    public void drawLevel(GameLevel level, RenderInfo info) {
        final WorldMap worldMap = level.worldMap();
        applyLevelSettings(level, info);
        if (info.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
            final int flashingIndex = info.get(CommonRenderInfoKey.MAZE_FLASHING_INDEX, Integer.class);
            configureHighlightedMapRenderInfo(info, worldMap, flashingIndex);
        } else {
            final long tick = info.get(CommonRenderInfoKey.TICK, Long.class);
            final MapCategory mapCategory = info.get(MapConfigKey.MAP_CATEGORY, MapCategory.class);
            configureNormalMapRenderInfo(info, mapCategory, worldMap, tick);
        }
        final Image mazeImage = info.get(CommonRenderInfoKey.MAZE_IMAGE, Image.class);
        final RectShort mazeSprite = info.get(CommonRenderInfoKey.MAZE_SPRITE, RectShort.class);
        final int x = 0, y = worldMap.terrainLayer().emptyRowsOverMaze() * TS;
        ctx.setImageSmoothing(imageSmoothing());
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(level);
        drawFood(level);
        level.optMessage().ifPresent(message -> {
            switch (message.type()) {
                case GAME_OVER -> drawGameOverMessage(level, message);
                case READY -> drawReadyMessage(message);
                case TEST -> drawTestMessage(level, message);
            }
        });
    }

    private void drawFood(GameLevel level) {
        requireNonNull(level);
        final WorldMap worldMap = level.worldMap();
        final MapImageSet recoloredMazeSprites = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final NES_ColorScheme colorScheme = recoloredMazeSprites.mapImage().colorScheme();
        final Color pelletColor = Color.valueOf(colorScheme.pelletColorRGB());
        final boolean blinkingOn = level.blinking().state() == Pulse.State.ON;

        ctx.save();
        ctx.scale(scaling(), scaling());
        drawPellets(worldMap.foodLayer(), pelletColor);
        drawEnergizers(worldMap.foodLayer(), pelletColor, blinkingOn);
        ctx.restore();
    }

    private void drawPellets(FoodLayer foodLayer, Color pelletColor) {
        foodLayer.tiles()
            .filter(foodLayer::isFoodTile)
            .filter(not(foodLayer::isEnergizerTile)).forEach(tile -> {
                // overpaint the pellet from the map image
                ctx.setFill(backgroundColor());
                fillSquareAtTileCenter(tile, 4);
                if (!foodLayer.hasEatenFoodAtTile(tile)) {
                    // draw pellet using the right color
                    ctx.setFill(pelletColor);
                    fillSquareAtTileCenter(tile, 2);
                }
        });
    }

    private void drawEnergizers(FoodLayer foodLayer, Color pelletColor, boolean blinkingOn) {
        final double size = TS;
        final double centerOffset = 0.5 * HTS;
        foodLayer.tiles().filter(foodLayer::isEnergizerTile).forEach(tile -> {
            // overpaint energizer pixels from map image
            ctx.setFill(backgroundColor());
            fillSquareAtTileCenter(tile, TS + 2);
            // draw energizer if not eaten and blinking is in ON phase
            if (!foodLayer.hasEatenFoodAtTile(tile) && blinkingOn) {
                final int x = tile.x() * TS;
                final int y = tile.y() * TS;
                // draw pixelated "circle"
                // TODO use sprite instead?
                ctx.setFill(pelletColor);
                ctx.fillRect(x + centerOffset, y, HTS, size);
                ctx.fillRect(x, y + centerOffset, size, HTS);
                ctx.fillRect(x + 1, y + 1, size - 2, size - 2);
            }
        });
    }

    private void drawGameOverMessage(GameLevel level, GameLevelMessage message) {
        final NES_ColorScheme colorScheme = level.worldMap().getConfigValue(MapConfigKey.NES_COLOR_SCHEME);
        final Color color = level.isDemoLevel()
            ? Color.valueOf(colorScheme.strokeColorRGB())
            : uiConfig.assets().color("color.game_over_message");
        fillTextCentered(GAME_OVER_MESSAGE_TEXT, color, arcadeFont8(), message.x(), message.y());
    }

    private void drawReadyMessage(GameLevelMessage message) {
        fillTextCentered(READY_MESSAGE_TEXT, uiConfig.assets().color("color.ready_message"), arcadeFont8(), message.x(), message.y());
    }

    private void drawTestMessage(GameLevel gameLevel, GameLevelMessage message) {
        fillTextCentered(LEVEL_TEST_MESSAGE_TEXT_PATTERN.formatted(
            gameLevel.number()), nesColor(0x28), arcadeFont8(), message.x(), message.y());
    }

    public void drawDoor(WorldMap worldMap) {
        final House house = worldMap.terrainLayer().optHouse().orElse(null);
        if (house == null) {
            return;
        }
        final MapImageSet recoloredImageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final Color strokeColor = Color.valueOf(recoloredImageSet.mapImage().colorScheme().strokeColorRGB());
        final double scaledTileSize = scaled(TS);
        final double xMin = house.leftDoorTile().x() * scaledTileSize;
        final double yMin = house.leftDoorTile().y() * scaledTileSize + scaled(5); // 5 pixels down
        ctx.setFill(strokeColor);
        ctx.fillRect(xMin, yMin, 2 * scaledTileSize, scaled(2));
    }

    private void overPaintActorSprites(GameLevel gameLevel) {
        final House house = gameLevel.worldMap().terrainLayer().optHouse().orElse(null);
        if (house == null) {
            return;
        }

        // Over-paint area at house bottom where the ghost sprites are shown in map
        final double margin = scaling();
        final double scaledTileSize = scaled(TS);
        final var inHouseArea = new Rectangle2D(
            0.5 * margin + scaledTileSize * (house.minTile().x() + 1),
            0.5 * margin + scaledTileSize * (house.minTile().y() + 2),
            scaledTileSize * (house.sizeInTiles().x() - 2) - margin,
            scaledTileSize * 2 - margin
        );

        ctx.setFill(backgroundColor());
        ctx.fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        final Vector2i pacTile = gameLevel.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_PAC, vec2_int(14, 26));
        overPaintActorSprite(pacTile, margin);

        final Vector2i redGhostTile = gameLevel.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_GHOST_1_RED, vec2_int(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        final double halfMargin = 0.5f * margin;
        final double overPaintSize = scaled(2 * TS) - margin;
        ctx.fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - HTS),
            overPaintSize, overPaintSize);
    }

    private void configureHighlightedMapRenderInfo(RenderInfo info, WorldMap worldMap, int flashingIndex) {
        final MapImageSet imageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final int i = Math.clamp(flashingIndex, 0, imageSet.flashingMapImages().size() - 1);
        final ColorSchemedImage flashingMapImage = imageSet.flashingMapImages().get(i);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, flashingMapImage.spriteSheetImage());
        info.put(CommonRenderInfoKey.MAZE_SPRITE, flashingMapImage.sprite());
    }

    private void configureNormalMapRenderInfo(RenderInfo info, MapCategory mapCategory, WorldMap worldMap, long tick) {
        final MapImageSet imageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, imageSet.mapImage().spriteSheetImage());
        final int mapNumber = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
            final int spriteIndex = strangeMap15AnimationFrame(tick);
            info.put(CommonRenderInfoKey.MAZE_SPRITE, NonArcadeMapsSpriteSheet.instance().sprites(MAP32_ANIMATED)[spriteIndex]);
        } else {
            info.put(CommonRenderInfoKey.MAZE_SPRITE, imageSet.mapImage().sprite());
        }
    }
}