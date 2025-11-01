/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer implements GameLevelRenderer {

    protected GameUI_Config uiConfig;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        setSpriteSheet(uiConfig.spriteSheet());
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {}

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        drawMaze(gameLevel, info);
        gameLevel.optMessage().ifPresent(message -> drawGameLevelMessage(gameLevel, message));
    }

    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        float emptySpaceOverMazePixels = TS(terrain.emptyRowsOverMaze());
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
            Image mazeImage = uiConfig.assets().image("maze.bright.%d".formatted(colorMapIndex));
            ctx.drawImage(mazeImage, 0, emptySpaceOverMazePixels);
        } else if (info.getBoolean(CommonRenderInfoKey.MAZE_EMPTY)) {
            RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSprite(mazeSprite, 0, emptySpaceOverMazePixels, false);
        } else {
            RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
            drawSprite(mazeSprite, 0, emptySpaceOverMazePixels, false);
            // Over-paint the eaten pellets (pellets are part of the maze image)
            FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(foodLayer::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten and dark-phase energizers
            foodLayer.energizerTiles().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfoKey.MAZE_BLINKING) || foodLayer.hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
    }

    protected void drawGameLevelMessage(GameLevel gameLevel, GameLevelMessage message) {
        switch (message.type()) {
            case MessageType.GAME_OVER -> fillTextCentered("GAME  OVER",
                ARCADE_RED, arcadeFontTS(), message.x(), message.y());
            case MessageType.READY -> fillTextCentered("READY!",
                ARCADE_YELLOW, arcadeFontTS(), message.x(), message.y());
            case MessageType.TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                ARCADE_WHITE, arcadeFontTS(), message.x(), message.y());
        }
    }
}