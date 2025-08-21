/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevelRenderer extends GameLevelRenderer implements SpriteRendererMixin {

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
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        ctx().setFill(backgroundColor);
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (mazeBright) {
            drawBrightGameLevel();
        } else if (gameContext.gameLevel().uneatenFoodCount() == 0) {
            drawEmptyGameLevel(gameContext.gameLevel());
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), !energizerBright);
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
        Image brightMazeImage = uiConfig.assets().image("flashing_maze");
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