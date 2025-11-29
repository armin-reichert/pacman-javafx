/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    private final TengenMsPacMan_UIConfig uiConfig;
    private final TengenMsPacMan_SpriteSheet spriteSheet;

    public TengenMsPacMan_GameLevelRenderer(Canvas canvas, TengenMsPacMan_UIConfig uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        this.spriteSheet = uiConfig.spriteSheet();
        ctx.setImageSmoothing(false);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {
        WorldMap worldMap = gameLevel.worldMap();
        // store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAZE_SPRITE_SET)) {
            int numFlashes = gameLevel.numFlashes();
            MazeSpriteSet mazeSpriteSet = uiConfig.createMazeSpriteSet(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAZE_SPRITE_SET, mazeSpriteSet);
            Logger.debug("Maze sprite set created: {}", mazeSpriteSet);
        }
    }

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        applyLevelSettings(gameLevel, info);
        if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
            int flashingIndex = info.get(CommonRenderInfoKey.MAZE_FLASHING_INDEX, Integer.class);
            uiConfig.configureHighlightedMazeRenderInfo(info, gameLevel, flashingIndex);
        } else {
            long tick = info.get(CommonRenderInfoKey.TICK, Long.class);
            MapCategory mapCategory = info.get(CONFIG_KEY_MAP_CATEGORY, MapCategory.class);
            uiConfig.configureNormalMazeRenderInfo(info, mapCategory, gameLevel.worldMap(), tick);
        }
        Image mazeImage = info.get(CommonRenderInfoKey.MAZE_IMAGE, Image.class);
        RectShort mazeSprite = info.get(CommonRenderInfoKey.MAZE_SPRITE, RectShort.class);
        ctx.setImageSmoothing(imageSmoothing());
        int x = 0, y = gameLevel.worldMap().terrainLayer().emptyRowsOverMaze() * TS;
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(gameLevel);
        drawFood(gameLevel);
        gameLevel.optMessage().ifPresent(message -> {
            switch (message.type()) {
                case GAME_OVER -> drawGameOverMessage(gameLevel, message);
                case READY -> drawReadyMessage(message);
                case TEST -> drawTestMessage(gameLevel, message);
            }
        });
    }

    private void drawFood(GameLevel gameLevel) {
        requireNonNull(gameLevel);
        ctx.save();
        ctx.scale(scaling(), scaling());
        MazeSpriteSet recoloredMaze =  gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAZE_SPRITE_SET);
        Color pelletColor = Color.web(recoloredMaze.mazeImage().colorScheme().pelletColorRGB());
        drawPellets(gameLevel, pelletColor);
        drawEnergizers(gameLevel, pelletColor);
        ctx.restore();
    }

    private void drawPellets(GameLevel gameLevel, Color pelletColor) {
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::isFoodTile)
            .filter(not(foodLayer::isEnergizerTile)).forEach(tile -> {
                ctx.setFill(background());
                fillSquareAtTileCenter(tile, 4);
                if (!foodLayer.hasEatenFoodAtTile(tile)) {
                    // draw pellet using the right color
                    ctx.setFill(pelletColor);
                    fillSquareAtTileCenter(tile, 2);
                }
        });
    }

    private void drawEnergizers(GameLevel gameLevel, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * HTS;
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        foodLayer.tiles().filter(foodLayer::isEnergizerTile).forEach(tile -> {
            ctx.setFill(background());
            fillSquareAtTileCenter(tile, TS + 2);
            if (!foodLayer.hasEatenFoodAtTile(tile) && gameLevel.blinking().state() == Pulse.State.ON) {
                ctx.setFill(pelletColor);
                // draw pixelated "circle"
                double cx = tile.x() * TS, cy = tile.y() * TS;
                ctx.fillRect(cx + offset, cy, HTS, size);
                ctx.fillRect(cx, cy + offset, size, HTS);
                ctx.fillRect(cx + 1, cy + 1, size - 2, size - 2);
            }
        });
    }

    private void drawGameOverMessage(GameLevel gameLevel, GameLevelMessage message) {
        final NES_ColorScheme colorScheme = gameLevel.worldMap().getConfigValue(CONFIG_KEY_NES_COLOR_SCHEME);
        Color color = gameLevel.isDemoLevel()
            ? Color.web(colorScheme.strokeColorRGB())
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
        final MazeSpriteSet recoloredMaze = worldMap.getConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAZE_SPRITE_SET);
        final Color doorColor = Color.web(recoloredMaze.mazeImage().colorScheme().strokeColorRGB());
        final double scaledTileSize = scaled(TS);
        final double xMin = house.leftDoorTile().x() * scaledTileSize;
        final double yMin = house.leftDoorTile().y() * scaledTileSize + scaled(5); // 5 pixels down
        ctx.setFill(doorColor);
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

        ctx.setFill(background());
        ctx.fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        final Vector2i pacTile = gameLevel.worldMap().terrainLayer()
            .getTileProperty(DefaultWorldMapPropertyName.POS_PAC, Vector2i.of(14, 26));
        overPaintActorSprite(pacTile, margin);

        final Vector2i redGhostTile = gameLevel.worldMap().terrainLayer()
            .getTileProperty(DefaultWorldMapPropertyName.POS_GHOST_1_RED, Vector2i.of(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        double halfMargin = 0.5f * margin;
        double overPaintSize = scaled(2 * TS) - margin;
        ctx.fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - HTS),
            overPaintSize, overPaintSize);
    }
}