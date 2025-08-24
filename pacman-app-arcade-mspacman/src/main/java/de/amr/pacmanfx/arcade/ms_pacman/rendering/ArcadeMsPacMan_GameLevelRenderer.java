/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer implements GameLevelRenderer, SpriteRendererMixin {

    protected GameUI_Config uiConfig;
    protected BrightMazesSpriteSheet brightMazesSpriteSheet;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig, BrightMazesSpriteSheet brightMazesSpriteSheet) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        this.brightMazesSpriteSheet = brightMazesSpriteSheet; // can be null in Ms. Pac-Man XXL!
    }

    @Override
    public void applyLevelSettings(GameContext gameContext) {
        // Nothing to do
    }

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        this(canvas, uiConfig, null);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameContext gameContext, RenderInfo info) {
        GameLevel gameLevel = gameContext.gameLevel();
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (info.getBoolean("bright")) {
            drawBrightMaze(colorMapIndex);
        } else if (info.getBoolean("empty")) {
            RectShort maze = spriteSheet().spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSprite(maze, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), colorMapIndex, !info.getBoolean("blinkingOn"));
        }
        ctx().restore();
        drawGameLevelMessage(gameContext.gameLevel());
    }

    private void drawBrightMaze(int index) {
        Image mazeImage = uiConfig.assets().image("maze.bright.%d".formatted(index));
        ctx.drawImage(mazeImage, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE));
    }

    private void drawGameLevelWithFood(GameLevel gameLevel, int colorMapIndex, boolean energizerDark) {
        RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
        drawSprite(mazeSprite, 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        // Overpaint the eaten pellets as they are part of the maze image
        gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
        // Draw the energizers, overpaint them if they are in dark phase
        gameLevel.energizerPositions().stream()
                .filter(tile -> energizerDark || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
    }

    protected void drawGameLevelMessage(GameLevel gameLevel) {
        gameLevel.optMessage().ifPresent(message -> {
            switch (message.type()) {
                case MessageType.GAME_OVER -> fillTextCentered("GAME  OVER",
                    ARCADE_RED, arcadeFontTS(), message.x(), message.y());
                case MessageType.READY -> fillTextCentered("READY!",
                    ARCADE_YELLOW, arcadeFontTS(), message.x(), message.y());
                case MessageType.TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                    ARCADE_WHITE, arcadeFontTS(), message.x(), message.y());
            }
        });
    }
}