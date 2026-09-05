/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadeMsPacMan_GameLevelRenderer extends BaseRenderer implements SpriteRenderer {

    protected final ActorSpriteAnimController animController;

    protected final AssetMap assets;

    protected InfoMap infoMap;

    public ArcadeMsPacMan_GameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas, AssetMap assets) {
        super(canvas);
        this.animController = requireNonNull(animController);
        this.assets = assets; // may be NULL e.g. in Ms. Pac-Man XXL where maze is drawn without images
    }

    public InfoMap infoMap() {
        return infoMap;
    }

    public void setInfoMap(InfoMap infoMap) {
        this.infoMap = infoMap;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameLevel level)) {
            return;
        }
        drawLevel(level);
    }

    protected void drawLevel(GameLevel level) {
        final House house = level.entities().house();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final float emptySpaceOverMazePixels = tilesPx(terrain.emptyRowsOverMaze());
        final int colorMapIndex = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);

        ctx.save();
        ctx.scale(scaling(), scaling());

        if (infoMap.getBoolean(CommonRenderInfoKey.MAP_FLASHING)) {
            if (infoMap.getBoolean(CommonRenderInfoKey.MAP_BRIGHT)) {
                final Image brightMazeImage = assets.image("maze.bright.%d".formatted(colorMapIndex));
                ctx.drawImage(brightMazeImage, 0, emptySpaceOverMazePixels);
                hideGhostHouseDoors(house);
            }
            else {
                final RectShort emptyMazeSprite = spriteSheet().findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
                drawSprite(emptyMazeSprite, 0, emptySpaceOverMazePixels, false);
            }
        }
        else if (infoMap.getBoolean(CommonRenderInfoKey.MAP_EMPTY)) {
            final RectShort emptyMazeSprite = spriteSheet().findSpriteSequence(SpriteID.EMPTY_MAPS)[colorMapIndex];
            drawSprite(emptyMazeSprite, 0, emptySpaceOverMazePixels, false);
        }
        else {
            final RectShort mapSprite = spriteSheet().findSpriteSequence(SpriteID.FULL_MAPS)[colorMapIndex];
            drawSprite(mapSprite, 0, emptySpaceOverMazePixels, false);

            final FoodLayer foodLayer = level.worldMap().foodLayer();
            // Over-paint the eaten pellets (pellets are part of the maze image)
            foodLayer.tiles()
                .filter(not(foodLayer::isEnergizerTile))
                .filter(level.food()::hasEatenFoodAtTile)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            // Over-paint eaten and dark-phase energizers
            foodLayer.energizerTiles().stream()
                .filter(tile -> !infoMap.getBoolean(CommonRenderInfoKey.ENERGIZER_VISIBLE) || level.food().hasEatenFoodAtTile(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    private void hideGhostHouseDoors(House house) {
        ctx.setFill(backgroundColor());
        if (house.floorplan().leftDoorTile() != null) {
            fillSquareAtTileCenter(house.floorplan().leftDoorTile(), TS + 0.5);
        }
        if (house.floorplan().rightDoorTile() != null) {
            fillSquareAtTileCenter(house.floorplan().rightDoorTile(), TS + 0.5);
        }
    }

    protected void drawGameLevelMessage(MessageView messageView, Vector2f pos) {
        switch (messageView.type().messageType()) {
            case MessageType.GAME_OVER
                -> fillTextCentered("GAME  OVER", ARCADE_RED, arcadeFont8(), pos.x(), pos.y());
            case MessageType.READY
                -> fillTextCentered("READY!", ARCADE_YELLOW, arcadeFont8(), pos.x(), pos.y());
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