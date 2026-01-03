/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.GameLevelMessageType;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    protected final AssetMap assets;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, AssetMap assets) {
        super(canvas);
        this.assets = assets; // may be NULL e.g. in Ms. Pac-Man XXL where maze is drawn without images
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {}

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        drawMaze(gameLevel, info);
        gameLevel.optMessage().ifPresent(message -> drawGameLevelMessage(gameLevel, message));
    }

    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        final float emptySpaceOverMazePixels = TS(terrain.emptyRowsOverMaze());
        final int colorMapIndex = gameLevel.worldMap().getConfigValue(GameUI_Config.ConfigKey.COLOR_MAP_INDEX);

        ctx.save();
        ctx.scale(scaling(), scaling());

        if (info.getBoolean(CommonRenderInfoKey.MAZE_FLASHING)) {
            if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
                final Image brightMazeImage = assets.image("maze.bright.%d".formatted(colorMapIndex));
                ctx.drawImage(brightMazeImage, 0, emptySpaceOverMazePixels);
                hideGhostHouseDoors(terrain);
            }
            else {
                final RectShort emptyMazeSprite = spriteSheet().sprites(SpriteID.EMPTY_MAZES)[colorMapIndex];
                drawSprite(emptyMazeSprite, 0, emptySpaceOverMazePixels, false);
            }
        }
        else if (info.getBoolean(CommonRenderInfoKey.MAZE_EMPTY)) {
            final RectShort emptyMazeSprite = spriteSheet().sprites(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSprite(emptyMazeSprite, 0, emptySpaceOverMazePixels, false);
        }
        else {
            RectShort mazeSprite = spriteSheet().sprites(SpriteID.FULL_MAZES)[colorMapIndex];
            drawSprite(mazeSprite, 0, emptySpaceOverMazePixels, false);
            // Over-paint the eaten pellets (pellets are part of the maze image)
            FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(foodLayer::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten and dark-phase energizers
            foodLayer.energizerTiles().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfoKey.ENERGIZER_BLINKING) || foodLayer.hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    private void hideGhostHouseDoors(TerrainLayer terrain) {
        terrain.optHouse().ifPresent(house -> {
            ctx.setFill(background());
            if (house.leftDoorTile() != null) {
                fillSquareAtTileCenter(house.leftDoorTile(), TS + 0.5);
            }
            if (house.rightDoorTile() != null) {
                fillSquareAtTileCenter(house.rightDoorTile(), TS + 0.5);
            }
        });
    }

    protected void drawGameLevelMessage(GameLevel gameLevel, GameLevelMessage message) {
        switch (message.type()) {
            case GameLevelMessageType.GAME_OVER -> fillTextCentered("GAME  OVER",
                ARCADE_RED, arcadeFont8(), message.x(), message.y());
            case GameLevelMessageType.READY -> fillTextCentered("READY!",
                ARCADE_YELLOW, arcadeFont8(), message.x(), message.y());
            case GameLevelMessageType.TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                ARCADE_WHITE, arcadeFont8(), message.x(), message.y());
        }
    }
}