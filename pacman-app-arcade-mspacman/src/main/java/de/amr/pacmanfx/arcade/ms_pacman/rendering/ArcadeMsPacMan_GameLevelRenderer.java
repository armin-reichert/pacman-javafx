/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends BaseSpriteRenderer implements GameLevelRenderer {

    protected GameUI_Config uiConfig;

    public ArcadeMsPacMan_GameLevelRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {
        Map<String, String> colorMap = gameLevel.worldMap().getConfigValue("colorMap");
        var colorScheme = new TerrainMapColorScheme(
            backgroundColor(),
            Color.web(colorMap.get("fill")),
            Color.web(colorMap.get("stroke")),
            Color.web(colorMap.get("door"))
        );
        info.put("terrainMapColorScheme", colorScheme);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameLevel gameLevel, RenderInfo info) {
        drawMaze(gameLevel, info);
        drawGameLevelMessage(gameLevel);
    }

    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
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
            gameLevel.tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten and dark-phase energizers
            gameLevel.energizerPositions().stream()
                .filter(tile -> !info.getBoolean("blinkingOn") || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx().restore();
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