/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes.playscene;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonGameLevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man. ThePac-Man XXL Pac-Man game subclasses this class to use a generic map
 * renderer instead of a sprite based one.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseRenderer implements SpriteRenderer {

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof GameLevel level)) {
            return;
        }

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final int emptyPixelsOverMaze = terrain.emptyRowsOverMaze() * WorldMap.TS;

        ctx.save();
        ctx.scale(scaling(), scaling());

        if (info.getBoolean(CommonGameLevelRenderInfoKey.MAZE_EMPTY)) {
            // Empty maze is shown when level is complete and when the flashing animation is running
            if (info.getBoolean(CommonGameLevelRenderInfoKey.BRIGHT_PHASE_ON)) {
                final var brightMazeImage = info.get(CommonGameLevelRenderInfoKey.BRIGHT_MAZE_IMAGE, Image.class);
                if (brightMazeImage != null) {
                    ctx.drawImage(brightMazeImage, 0, emptyPixelsOverMaze);
                }
            } else {
                drawSprite(spriteSheet().findSpriteSequence(SpriteID.MAP_EMPTY)[0], 0, emptyPixelsOverMaze, false);
            }
            if (info.getBoolean(CommonGameLevelRenderInfoKey.FLASHING)) {
                final House house = level.entities().house();
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
            drawSprite(spriteSheet().findSprite(SpriteID.MAP_FULL), 0, emptyPixelsOverMaze, false);
            drawEatenFood(level);
        }
        ctx.restore();
    }

    private void drawEatenFood(GameLevel level) {
        // Over-paint eaten food tiles
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final FoodState foodState = level.food();
        foodLayer.tiles()
            .filter(not(foodLayer::isEnergizerTile))
            .filter(foodState::hasEatenFoodAtTile)
            .forEach(tile -> fillSquareAtTileCenter(tile, 4));
    }
}