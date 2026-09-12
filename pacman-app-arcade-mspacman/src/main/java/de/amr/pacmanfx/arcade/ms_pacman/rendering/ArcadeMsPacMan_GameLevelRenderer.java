/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Door;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.door.comp.DoorDataComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Sprite sheet based renderer for Arcade Ms. Pac-Man.
 */
public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer {

    protected final ActorSpriteAnimController animController;

    protected final AssetMap assets;

    public ArcadeMsPacMan_GameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas, AssetMap assets) {
        super(canvas);
        this.animController = requireNonNull(animController);
        this.assets = assets; // may be NULL e.g. in Ms. Pac-Man XXL where maze is drawn without images
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
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

        // Ms. Pac-Man maps are used with different color maps.
        // We store the color map index inside the map properties.
        final int colorMapIndex = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);

        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setImageSmoothing(true);

        if (info.getBoolean(LevelRenderInfoKey.MAZE_IS_FLASHING)) {
            if (info.getBoolean(LevelRenderInfoKey.SHOW_BRIGHT_MAZE)) {
                final String brightMazeKey = "maze.bright.%d".formatted(colorMapIndex);
                final Image brightMazeImage = assets.image(brightMazeKey);
                ctx.drawImage(brightMazeImage, 0, emptyPixelsOverMaze);
                hideGhostHouseDoors(level.entities().house());
            }
            else {
                final RectShort emptyMazeSprite = spriteSheet().findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
                drawSprite(emptyMazeSprite, 0, emptyPixelsOverMaze, false);
            }
        }
        else if (info.getBoolean(LevelRenderInfoKey.SHOW_EMPTY_MAZE)) {
            final RectShort emptyMazeSprite = spriteSheet().findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
            drawSprite(emptyMazeSprite, 0, emptyPixelsOverMaze, false);
        }
        else {
            final RectShort mapSprite = spriteSheet().findSpriteSequence(SpriteID.FULL_MAPS)[colorMapIndex];
            drawSprite(mapSprite, 0, emptyPixelsOverMaze, false);
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