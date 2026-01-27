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
import static de.amr.pacmanfx.ui.ArcadePalette.*;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man. ThePac-Man XXL Pac-Man game subclasses this class to use a generic map
 * renderer instead of a sprite based one.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    private final Image brightMapImage;

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas,  Image brightMapImage) {
        super(canvas);
        this.brightMapImage = brightMapImage; // may be null e.g. in Pac-Man XXL where mazes are rendered without images
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void applyLevelSettings(GameLevel level, RenderInfo info) {}

    @Override
    public void drawLevel(GameLevel level, RenderInfo info) {
        drawMap(level, info);
        level.optMessage().ifPresent(message -> drawLevelMessage(level, message));
    }

    protected void drawMap(GameLevel level, RenderInfo info) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final int emptySpaceOverMazePixels = terrain.emptyRowsOverMaze() * TS;
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (info.getBoolean(CommonRenderInfoKey.MAP_EMPTY)) {
            // Empty maze is shown when level is complete and when the flashing animation is running
            if (info.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
                // Flashing animation bright phase
                if (brightMapImage != null) {
                    ctx.drawImage(brightMapImage, 0, emptySpaceOverMazePixels);
                }
            } else {
                drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, emptySpaceOverMazePixels, false);
            }
            if (info.getBoolean(CommonRenderInfoKey.MAP_FLASHING)) {
                // Hide ghost house doors while flashing
                terrain.optHouse().ifPresent(house -> {
                    ctx.setFill(backgroundColor());
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
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(foodLayer::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten or dark-blinking energizer tiles
            foodLayer.energizerTiles().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE) || foodLayer.hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    protected void drawLevelMessage(GameLevel level, GameLevelMessage msg) {
        switch (msg.type()) {
            case GAME_OVER -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFont8(), msg.x(), msg.y());
            case READY -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFont8(), msg.x(), msg.y());
            case TEST -> fillTextCentered("TEST    L%02d".formatted(level.number()), ARCADE_WHITE, arcadeFont8(), msg.x(), msg.y());
        }
    }
}