/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

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

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel) {
        // Nothing to do
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        float emptySpaceOverMaze = TS(GameLevel.EMPTY_ROWS_OVER_MAZE);
        int colorMapIndex = gameLevel.worldMap().getConfigValue("colorMapIndex");
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (info.getBoolean("bright")) {
            Image mazeImage = uiConfig.assets().image("maze.bright.%d".formatted(colorMapIndex));
            ctx.drawImage(mazeImage, 0, emptySpaceOverMaze);
        } else if (info.getBoolean("empty")) {
            RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.EMPTY_MAZES)[colorMapIndex];
            drawSprite(mazeSprite, 0, emptySpaceOverMaze, false);
        } else {
            RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[colorMapIndex];
            drawSprite(mazeSprite, 0, emptySpaceOverMaze, false);
            // Over-paint the eaten pellets (pellets are part of the maze image)
            gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten and dark-phase energizers
            gameLevel.energizerPositions().stream()
                .filter(tile -> !info.getBoolean("blinkingOn") || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
        drawGameLevelMessage(gameLevel);
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