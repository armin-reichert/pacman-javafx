/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseSpriteRenderer implements GameLevelRenderer {

    private final Image brightMazeImage;

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas, SpriteSheet<?> spriteSheet, Image brightMazeImage) {
        super(canvas, spriteSheet);
        this.brightMazeImage = brightMazeImage; // may be null e.g. in Pac-Man XXL where mazes are rendered without images
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
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
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        int emptySpaceOverMazePixels = terrain.emptyRowsOverMaze() * TS;
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
            ctx.drawImage(brightMazeImage, 0, emptySpaceOverMazePixels);
        }
        else if (info.getBoolean(CommonRenderInfoKey.MAZE_EMPTY)) {
            drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, emptySpaceOverMazePixels, false);
            // Over-paint door tiles
            terrain.optHouse().map(House::leftDoorTile) .ifPresent(tile -> fillSquareAtTileCenter(tile, TS + 0.5));
            terrain.optHouse().map(House::rightDoorTile).ifPresent(tile -> fillSquareAtTileCenter(tile, TS + 0.5));
        }
        else {
            drawSprite(spriteSheet().sprite(SpriteID.MAP_FULL), 0, emptySpaceOverMazePixels, false);
            // Over-paint eaten food tiles
            FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(foodLayer::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten or dark-blinking energizer tiles
            foodLayer.energizerTiles().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfoKey.ENERGIZER_BLINKING) || foodLayer.hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    protected void drawGameLevelMessage(GameLevel gameLevel, GameLevelMessage message) {
        double x = message.x(), y = message.y();
        switch (message.type()) {
            case MessageType.GAME_OVER
                -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFont8(), x, y);
            case MessageType.READY
                -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFont8(), x, y);
            case MessageType.TEST
                -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()), ARCADE_WHITE, arcadeFont8(), x, y);
        }
    }
}