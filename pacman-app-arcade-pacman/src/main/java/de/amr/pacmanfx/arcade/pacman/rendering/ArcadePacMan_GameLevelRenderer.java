/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
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

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevelRenderer extends BaseRenderer implements GameLevelRenderer, SpriteRendererMixin {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void applyLevelSettings(GameContext gameContext) {
        // nothing to do
    }

    @Override
    public void drawGameLevel(GameContext gameContext, RenderInfo info) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (info.getBoolean("mazeBright")) {
            drawBrightGameLevel();
        } else if (gameContext.gameLevel().uneatenFoodCount() == 0) {
            drawEmptyGameLevel(gameContext.gameLevel());
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), !info.getBoolean("blinkingPhaseOn"));
        }
        ctx().restore();
        drawGameLevelMessage(gameContext.gameLevel());
    }

    private void drawEmptyGameLevel(GameLevel gameLevel) {
        drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        // hide doors
        gameLevel.house().ifPresent(house -> fillSquareAtTileCenter(house.leftDoorTile(), TS + 0.5));
        gameLevel.house().ifPresent(house -> fillSquareAtTileCenter(house.rightDoorTile(), TS + 0.5));
    }

    private void drawBrightGameLevel() {
        Image brightMazeImage = uiConfig.assets().image("maze.bright");
        ctx().drawImage(brightMazeImage, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
    }

    private void drawGameLevelWithFood(GameLevel gameLevel, boolean energizerDark) {
        drawSprite(spriteSheet().sprite(SpriteID.MAP_FULL), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
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