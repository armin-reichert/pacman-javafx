/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.MapConfigKey;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;
import de.amr.pacmanfx.tengenmspacman.sprites.*;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.READY_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    /**
     * Strange map #15 (maze #32) has a "psychedelic" animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private static int strangeMap15AnimationFrame(long tick) {
        final long phase = (tick % 32) / 8;
        return (int) (phase < 3 ? phase : 1);
    }

    //TODO pass render config instead?
    private final AssetMap assets;

    public TengenMsPacMan_GameLevelRenderer(AssetMap assets, Canvas canvas) {
        super(canvas);
        this.assets = requireNonNull(assets);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void applyLevelSettings(GameRules rules, GameLevel level, RenderInfo info) {
        final WorldMap worldMap = level.worldMap();
        // store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(MapConfigKey.MAP_IMAGE_SET)) {
            final int numFlashes = rules.numLevelFlashes(level.number());
            final MapImageSet mapImageSet = TengenMsPacMan_MapRepository.instance().createMapImageSet(worldMap, numFlashes);
            worldMap.setConfigValue(MapConfigKey.MAP_IMAGE_SET, mapImageSet);
            Logger.debug("Maze sprite set created: {}", mapImageSet);
        }
    }

    @Override
    public void drawLevel(GameContext game, GameLevel level, RenderInfo info) {
        final WorldMap worldMap = level.worldMap();
        applyLevelSettings(game.variantConfig().rules(), level, info);
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
        final int x = 0, y = worldMap.terrainLayer().emptyRowsOverMaze() * WorldMap.TS;
        ctx.setImageSmoothing(imageSmoothing());
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(level);
        drawFood(level);

        game.session().hud().optMessage().ifPresent(message -> {
            switch (message.type()) {
                case GAME_OVER -> {
                    final MessageAnimation messageAnimation = game.session().value(
                        TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);
                    final Vector2f pos = (messageAnimation != null)
                        ? messageAnimation.pos().asVector2f()
                        : messagePosition(level);
                    drawGameOverMessage(game.session(), level.worldMap(), pos);
                }
                case READY -> drawReadyMessage(messagePosition(level));
            }
        });
    }

    private void drawFood(GameLevel level) {
        requireNonNull(level);
        final WorldMap worldMap = level.worldMap();
        final MapImageSet recoloredMazeSprites = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final NES_WorldMapColorScheme colorScheme = recoloredMazeSprites.mapImage().colorScheme();
        final Color pelletColor = Color.valueOf(colorScheme.pellet());
        final boolean blinkingOn = level.heartbeat().state() == Pulse.State.ON;

        ctx.save();
        ctx.scale(scaling(), scaling());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor, blinkingOn);
        ctx.restore();
    }

    private void drawPellets(GameLevel level, Color pelletColor) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::isFoodTile)
            .filter(not(foodLayer::isEnergizerTile)).forEach(tile -> {
                // overpaint the pellet from the map image
                ctx.setFill(backgroundColor());
                fillSquareAtTileCenter(tile, 4);
                if (!level.food().hasEatenFoodAtTile(tile)) {
                    // draw pellet using the right color
                    ctx.setFill(pelletColor);
                    fillSquareAtTileCenter(tile, 2);
                }
        });
    }

    private void drawEnergizers(GameLevel level, Color pelletColor, boolean blinkingOn) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final double size = WorldMap.TS;
        final double centerOffset = 0.5 * WorldMap.HTS;
        foodLayer.tiles().filter(foodLayer::isEnergizerTile).forEach(tile -> {
            // overpaint energizer pixels from map image
            ctx.setFill(backgroundColor());
            fillSquareAtTileCenter(tile, WorldMap.TS + 2);
            // draw energizer if not eaten and blinking is in ON phase
            if (!level.food().hasEatenFoodAtTile(tile) && blinkingOn) {
                final int x = tile.x() * WorldMap.TS;
                final int y = tile.y() * WorldMap.TS;
                // draw pixelated "circle"
                // TODO use sprite instead?
                ctx.setFill(pelletColor);
                ctx.fillRect(x + centerOffset, y, WorldMap.HTS, size);
                ctx.fillRect(x, y + centerOffset, size, WorldMap.HTS);
                ctx.fillRect(x + 1, y + 1, size - 2, size - 2);
            }
        });
    }

    private void drawGameOverMessage(GameSession session, WorldMap worldMap, Vector2f pos) {
        final NES_WorldMapColorScheme colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final Color color = session.isAttractMode()
            ? Color.valueOf(colorScheme.wallStroke())
            : assets.color("color.game_over_message");
        fillTextCentered(GAME_OVER_MESSAGE_TEXT, color, arcadeFont8(), pos.x(), pos.y());
    }

    private void drawReadyMessage(Vector2f pos) {
        fillTextCentered(READY_MESSAGE_TEXT, assets.color("color.ready_message"), arcadeFont8(), pos.x(), pos.y());
    }

    public void drawDoor(House house, WorldMap worldMap) {
        final MapImageSet recoloredImageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final Color strokeColor = Color.valueOf(recoloredImageSet.mapImage().colorScheme().wallStroke());
        final double scaledTileSize = scaled(WorldMap.TS);
        final double xMin = house.floorplan().leftDoorTile().x() * scaledTileSize;
        final double yMin = house.floorplan().leftDoorTile().y() * scaledTileSize + scaled(5); // 5 pixels down
        ctx.setFill(strokeColor);
        ctx.fillRect(xMin, yMin, 2 * scaledTileSize, scaled(2));
    }

    private void overPaintActorSprites(GameLevel level) {
        final House house = level.entities().house();

        // Over-paint area at house bottom where the ghost sprites are shown in map
        final double margin = scaling();
        final double scaledTileSize = scaled(WorldMap.TS);
        final var inHouseArea = new Rectangle2D(
            0.5 * margin + scaledTileSize * (house.floorplan().minTile().x() + 1),
            0.5 * margin + scaledTileSize * (house.floorplan().minTile().y() + 2),
            scaledTileSize * (house.sizeInTiles().x() - 2) - margin,
            scaledTileSize * 2 - margin
        );

        ctx.setFill(backgroundColor());
        ctx.fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        final Vector2i pacTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_PAC, WorldMap.tile(14, 26));
        overPaintActorSprite(pacTile, margin);

        final Vector2i redGhostTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_GHOST_1_RED, WorldMap.tile(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        final double halfMargin = 0.5f * margin;
        final double overPaintSize = scaled(2 * WorldMap.TS) - margin;
        ctx.fillRect(
            halfMargin + scaled(tile.x() * WorldMap.TS),
            halfMargin + scaled(tile.y() * WorldMap.TS - WorldMap.HTS),
            overPaintSize, overPaintSize);
    }

    private void configureHighlightedMapRenderInfo(RenderInfo info, WorldMap worldMap, int flashingIndex) {
        final MapImageSet imageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        final int i = Math.clamp(flashingIndex, 0, imageSet.flashingMapImages().size() - 1);
        final ColorSchemedMapSprite flashingMapImage = imageSet.flashingMapImages().get(i);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, flashingMapImage.spriteSheetImage());
        info.put(CommonRenderInfoKey.MAZE_SPRITE, flashingMapImage.sprite());
    }

    private void configureNormalMapRenderInfo(RenderInfo info, MapCategory mapCategory, WorldMap worldMap, long tick) {
        final MapImageSet imageSet = worldMap.getConfigValue(MapConfigKey.MAP_IMAGE_SET);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, imageSet.mapImage().spriteSheetImage());
        final int mapNumber = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
            final int spriteIndex = strangeMap15AnimationFrame(tick);
            info.put(CommonRenderInfoKey.MAZE_SPRITE, NonArcadeMapsSpriteSheet.instance().findSprites(MAP32_ANIMATED)[spriteIndex]);
        } else {
            info.put(CommonRenderInfoKey.MAZE_SPRITE, imageSet.mapImage().sprite());
        }
    }

    protected Vector2f messagePosition(GameLevel level) {
        final House house = level.entities().house();
        Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
    }
}