/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.entities.world.Door;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.entities.world.DoorDataComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameLevelView;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Sprite sheet based renderer for Arcade Ms. Pac-Man.
 */
public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();
    protected final ActorSpriteAnimController animController;

    protected final AssetMap assets;

    public ArcadeMsPacMan_GameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas, AssetMap assets) {
        super(canvas);
        this.animController = requireNonNull(animController);
        this.assets = assets; // may be NULL e.g. in Ms. Pac-Man XXL where maze is drawn without images
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevelView(GameLevel level, InfoMap renderInfo, RenderingLayer _, int _, Vector2f _) -> renderGameLevel(level, renderInfo);
            case GameEntityView(Energizer energizer, RenderingLayer _, int _, Vector2f _, InfoMap renderInfo) -> hideEnergizerIfOff(energizer);
            default -> {}
        }
    }

    private void renderGameLevel(GameLevel level, InfoMap info) {
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
                final House house = level.entitySet().entities().theOne(House.class);
                hideGhostHouseDoors(house);
            }
            else {
                final RectShort emptyMazeSprite = spriteSheet.findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
                drawSprite(emptyMazeSprite, 0, emptyPixelsOverMaze, false);
            }
        }
        else if (info.getBoolean(LevelRenderInfoKey.SHOW_EMPTY_MAZE)) {
            final RectShort emptyMazeSprite = spriteSheet.findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
            drawSprite(emptyMazeSprite, 0, emptyPixelsOverMaze, false);
        }
        else {
            final RectShort mapSprite = spriteSheet.findSpriteSequence(SpriteID.FULL_MAPS)[colorMapIndex];
            drawSprite(mapSprite, 0, emptyPixelsOverMaze, false);
            hideEatenPellets(level);
        }
        ctx.restore();
    }

    private void hideEnergizerIfOff(Energizer energizer) {
        if (!energizer.on()) {
            final double size = scaled(9);
            ctx.save();
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(energizer.pos().x() - 0.5), scaled(energizer.pos().y() - 0.5), size, size);
            ctx.restore();
        }
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