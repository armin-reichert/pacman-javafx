/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevelRenderer extends BaseRenderer implements GameLevelRenderer, Renderer {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        setSpriteSheet(uiConfig.spriteSheet());
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {
    }

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        drawMaze(gameLevel, info);
        gameLevel.optMessage().ifPresent(message -> drawGameLevelMessage(gameLevel, message));
    }

    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        int emptySpaceOverMaze = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (info.getBoolean(CommonRenderInfo.MAZE_BRIGHT)) {
            Image brightMazeImage = uiConfig.assets().image("maze.bright");
            ctx().drawImage(brightMazeImage, 0, emptySpaceOverMaze);
        }
        else if (info.getBoolean(CommonRenderInfo.MAZE_EMPTY)) {
            drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, emptySpaceOverMaze, false);
            // over-paint door tiles
            gameLevel.house().map(House::leftDoorTile) .ifPresent(tile -> fillSquareAtTileCenter(tile, TS + 0.5));
            gameLevel.house().map(House::rightDoorTile).ifPresent(tile -> fillSquareAtTileCenter(tile, TS + 0.5));
        }
        else {
            drawSprite(spriteSheet().sprite(SpriteID.MAP_FULL), 0, emptySpaceOverMaze, false);
            // Over-paint eaten food tiles
            gameLevel.tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel.foodStore()::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten or dark-blinking energizer tiles
            gameLevel.energizerPositions().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfo.MAZE_BLINKING) || gameLevel.foodStore().tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
    }

    protected void drawGameLevelMessage(GameLevel gameLevel, GameLevelMessage message) {
        double x = message.x(), y = message.y();
        switch (message.type()) {
            case MessageType.GAME_OVER
                -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFontTS(), x, y);
            case MessageType.READY
                -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFontTS(), x, y);
            case MessageType.TEST
                -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()), ARCADE_WHITE, arcadeFontTS(), x, y);
        }
    }
}