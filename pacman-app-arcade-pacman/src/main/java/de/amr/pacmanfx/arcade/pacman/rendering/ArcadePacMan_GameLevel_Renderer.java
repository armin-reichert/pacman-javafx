/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man. ThePac-Man XXL Pac-Man game subclasses this class to use a generic map
 * renderer instead of a sprite based one.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseRenderer implements SpriteRenderer, GameLevelRenderer {

    private final Image brightMapImage;

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas, Image brightMapImage) {
        super(canvas);
        this.brightMapImage = brightMapImage; // may be null e.g. in Pac-Man XXL where mazes are rendered without images
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void applyLevelSettings(GameRules rules, GameLevel level, RenderInfo info) {}

    @Override
    public void drawLevel(GameContext game, GameLevel level, RenderInfo info) {
        drawMap(level, info);

        final MessageView messageView = game.session().hud().messageView();
        if (messageView.data().messageType() != MessageType.NO_MESSAGE) {
            drawLevelMessage(messageView, messagePosition(level));
        }
    }

    protected void drawMap(GameLevel level, RenderInfo info) {
        final House house = level.entities().house();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final int emptySpaceOverMazePixels = terrain.emptyRowsOverMaze() * WorldMap.TS;
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
                drawSprite(spriteSheet().findSpriteSequence(SpriteID.MAP_EMPTY)[0], 0, emptySpaceOverMazePixels, false);
            }
            if (info.getBoolean(CommonRenderInfoKey.MAP_FLASHING)) {
                // Hide ghost house doors while flashing
                if (house != null) {
                    ctx.setFill(backgroundColor());
                    if (house.floorplan().leftDoorTile() != null) {
                        fillSquareAtTileCenter(house.floorplan().leftDoorTile(), WorldMap.TS + 0.5);
                    }
                    if (house.floorplan().rightDoorTile() != null) {
                        fillSquareAtTileCenter(house.floorplan().rightDoorTile(), WorldMap.TS + 0.5);
                    }
                }
            }
        }
        else {
            drawSprite(spriteSheet().findSprite(SpriteID.MAP_FULL), 0, emptySpaceOverMazePixels, false);
            // Over-paint eaten food tiles
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(level.food()::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten or dark-blinking energizer tiles
            foodLayer.energizerTiles().stream()
                .filter(tile -> !info.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE) || level.food().hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    protected void drawLevelMessage(MessageView messageView, Vector2f pos) {
        switch (messageView.data().messageType()) {
            case GAME_OVER -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFont8(), pos.x(), pos.y());
            case READY -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFont8(), pos.x(), pos.y());
        }
    }

    protected Vector2f messagePosition(GameLevel level) {
        final House house = level.entities().house();
        Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
    }
}