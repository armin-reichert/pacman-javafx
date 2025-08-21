/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends GameLevelRenderer implements SpriteRendererMixin {

    protected GameUI_Config uiConfig;
    protected BrightMazesSpriteSheet brightMazesSpriteSheet;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig, BrightMazesSpriteSheet brightMazesSpriteSheet) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        this.brightMazesSpriteSheet = brightMazesSpriteSheet; // can be null in Ms. Pac-Man XXL!
    }

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        this(canvas, uiConfig, null);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        ctx().setFill(backgroundColor);
        if (mazeBright) {
            drawBrightGameLevel(gameContext.gameLevel());
        } else if (gameContext.gameLevel().uneatenFoodCount() == 0) {
            drawEmptyGameLevel(gameContext.gameLevel());
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), !energizerBright);
        }
        drawGameLevelMessage(gameContext.gameLevel());
    }

    private void drawEmptyGameLevel(GameLevel gameLevel) {
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        RectShort maze = spriteSheet().spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
        drawSprite(maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
    }

    private void drawBrightGameLevel(GameLevel gameLevel) {
        if (brightMazesSpriteSheet != null) {
            int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
            RectShort[] brightMazes = brightMazesSpriteSheet.spriteSequence(BrightMazesSpriteSheet.SpriteID.BRIGHT_MAZES);
            RectShort maze = brightMazes[colorMapIndex];
            double s = scaling();
            ctx().drawImage(brightMazesSpriteSheet.sourceImage(),
                maze.x(), maze.y(), maze.width(), maze.height(),
                0, s * TS(GameLevel.EMPTY_ROWS_OVER_MAZE), s * maze.width(), s * maze.height());
        }
    }

    private void drawGameLevelWithFood(GameLevel gameLevel, boolean energizerDark) {
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        // Draw the maze
        RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
        drawSprite(mazeSprite, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), true);
        ctx().save();
        ctx().scale(scaling(), scaling());
        // Overpaint the eaten pellets as they are part of the maze image
        gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
        // Draw the energizers, overpaint them if they are in dark phase
        gameLevel.energizerPositions().stream()
                .filter(tile -> energizerDark || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        ctx().restore();
    }

    private void drawGameLevelMessage(GameLevel gameLevel) {
        // Draw message if available
        if (gameLevel.messageType() != GameLevel.MessageType.NONE && gameLevel.house().isPresent()) {
            House house = gameLevel.house().get();
            Vector2i houseSize = house.sizeInTiles();
            float cx = TS(house.minTile().x() + houseSize.x() * 0.5f);
            float cy = TS(house.minTile().y() + houseSize.y() + 1);
            switch (gameLevel.messageType()) {
                case GameLevel.MessageType.GAME_OVER -> fillTextCentered("GAME  OVER",
                        ARCADE_RED, arcadeFontTS(), cx, cy);
                case GameLevel.MessageType.READY -> fillTextCentered("READY!",
                        ARCADE_YELLOW, arcadeFontTS(), cx, cy);
                case GameLevel.MessageType.TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                        ARCADE_WHITE, arcadeFontTS(), cx, cy);
            }
        }
    }
}