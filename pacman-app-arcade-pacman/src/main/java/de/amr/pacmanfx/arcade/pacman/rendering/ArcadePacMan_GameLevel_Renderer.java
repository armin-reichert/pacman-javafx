/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    private final Image brightMazeImage;

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas,  Image brightMazeImage) {
        super(canvas);
        this.brightMazeImage = brightMazeImage; // may be null e.g. in Pac-Man XXL where mazes are rendered without images
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
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
        int emptySpaceOverMazePixels = terrain.emptyRowsOverMaze() * TS;
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (info.getBoolean(CommonRenderInfoKey.MAZE_EMPTY)) {
            // Empty maze is shown when level is complete and when the flashing animation is running
            if (info.getBoolean(CommonRenderInfoKey.MAZE_BRIGHT)) {
                // Flashing animation bright phase
                ctx.drawImage(brightMazeImage, 0, emptySpaceOverMazePixels);
            } else {
                drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, emptySpaceOverMazePixels, false);
            }
            if (info.getBoolean(CommonRenderInfoKey.MAZE_FLASHING)) {
                // Hide ghost house doors while flashing
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
        final double x = message.x(), y = message.y();
        switch (message.type()) {
            case GAME_OVER -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFont8(), x, y);
            case READY -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFont8(), x, y);
            case TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()), ARCADE_WHITE, arcadeFont8(), x, y);
        }
    }
}