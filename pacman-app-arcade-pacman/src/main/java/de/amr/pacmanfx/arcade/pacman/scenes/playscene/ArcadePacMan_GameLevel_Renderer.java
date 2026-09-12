/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.playscene;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.entities.Door;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.door.comp.DoorDataComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.function.Predicate.not;

/**
 * Sprite sheet based renderer for classic Arcade Pac-Man game level.
 *
 * <p>The XXL game variants with custom-map support use a vector renderer instead.
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
        if (r instanceof GameLevel level) {
            renderGameLevel(level);
        }
    }

    private void renderGameLevel(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final int emptyPixelsOverMaze = terrain.emptyRowsOverMaze() * TS;

        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setImageSmoothing(true);

        if (info.getBoolean(LevelRenderInfoKey.SHOW_EMPTY_MAZE)) {
            // Empty maze is shown when level is complete and when the flashing animation is running
            if (info.getBoolean(LevelRenderInfoKey.SHOW_BRIGHT_MAZE)) {
                final var brightMazeImage = info.get(LevelRenderInfoKey.BRIGHT_MAZE_IMAGE, Image.class);
                ctx.drawImage(brightMazeImage, 0, emptyPixelsOverMaze);
            } else {
                final RectShort emptyMapSprite = spriteSheet().findSpriteSequence(SpriteID.MAP_EMPTY)[0];
                drawSprite(emptyMapSprite, 0, emptyPixelsOverMaze, false);
            }
            if (info.getBoolean(LevelRenderInfoKey.MAZE_IS_FLASHING)) {
                hideGhostHouseDoors(level.entities().house());
            }
        }
        else {
            drawSprite(spriteSheet().findSprite(SpriteID.MAP_FULL), 0, emptyPixelsOverMaze, false);
            hideEatenPellets(level);
        }
        ctx.restore();
    }

    private void hideGhostHouseDoors(House house) {
        final Door door = house.door();
        final var doorData = door.reqComp(DoorDataComp.class);
        ctx.setFill(backgroundColor());
        fillSquareAtTileCenter(doorData.leftTile(),  TS + 0.5);
        fillSquareAtTileCenter(doorData.rightTile(), TS + 0.5);
    }

    private void hideEatenPellets(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final FoodState foodState = level.food();
        foodLayer.tiles()
            .filter(not(foodLayer::isEnergizerTile))
            .filter(foodState::hasEatenFoodAtTile)
            .forEach(tile -> fillSquareAtTileCenter(tile, 4));
    }
}