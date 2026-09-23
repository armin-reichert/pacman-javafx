/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.entities.world.door.Door;
import de.amr.pacmanfx.core.entities.world.door.DoorDataComp;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameLevelView;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.Optional;

import static java.util.function.Predicate.not;

/**
 * Sprite sheet based renderer for classic Arcade Pac-Man game level.
 *
 * <p>The XXL game variants with custom-map support use a vector renderer instead.
 */
public class ArcadePacMan_GameLevel_Renderer extends BaseRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
    private final ArcadePacMan_RenderConfig renderConfig;

    public ArcadePacMan_GameLevel_Renderer(Canvas canvas, ArcadePacMan_RenderConfig renderConfig) {
        super(canvas);
        this.renderConfig = renderConfig;
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevelView(GameLevel level, InfoMap renderInfo, RenderingLayer _, int _, Vector2f _) -> renderGameLevel(level, renderInfo);
            case GameEntityView(Energizer energizer, RenderingLayer _, int _, Vector2f _) -> hideEnergizerIfOff(energizer);
            default -> {}
        }
    }

    private void renderGameLevel(GameLevel level, InfoMap info) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final int emptyPixelsOverMaze = terrain.emptyRowsOverMaze() * TS;

        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setImageSmoothing(true);

        if (info.getBoolean(LevelRenderInfoKey.SHOW_EMPTY_MAZE)) {
            // Empty maze is shown when level is complete and when the flashing animation is running
            if (info.getBoolean(LevelRenderInfoKey.SHOW_BRIGHT_MAZE)) {
                if (renderConfig.assets().containsAsset("maze.bright")) {
                    info.put(LevelRenderInfoKey.BRIGHT_MAZE_IMAGE, renderConfig.assets().image("maze.bright"));
                }
                final var brightMazeImage = info.get(LevelRenderInfoKey.BRIGHT_MAZE_IMAGE, Image.class);
                ctx.drawImage(brightMazeImage, 0, emptyPixelsOverMaze);
            } else {
                final RectShort emptyMapSprite = spriteSheet.findSpriteSequence(SpriteID.MAP_EMPTY)[0];
                drawSprite(emptyMapSprite, 0, emptyPixelsOverMaze, false);
            }
            if (info.getBoolean(LevelRenderInfoKey.MAZE_IS_FLASHING)) {
                final House house = level.entitySet().entities().theOne(House.class);
                hideGhostHouseDoors(house);
            }
        }
        else {
            drawSprite(spriteSheet.findSprite(SpriteID.MAP_FULL), 0, emptyPixelsOverMaze, false);
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
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(backgroundColor());
        fillSquareAtTileCenter(doorData.leftTile(),  TS + 0.5);
        fillSquareAtTileCenter(doorData.rightTile(), TS + 0.5);
        ctx.restore();
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