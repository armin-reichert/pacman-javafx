/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.tengen;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManArcadeGameRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.PROPERTY_COLOR_WALL_FILL;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameWorldRenderer {

    private final AssetStorage assets;
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final Image arcadeMazesImage;
    private final Image nonArcadeMazesImage;
    //TODO temporary
    private final MsPacManArcadeGameRenderer rendererMsPacMan;

    private ImageArea mapSprite;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public TengenMsPacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());

        rendererMsPacMan = new MsPacManArcadeGameRenderer(assets);
        rendererMsPacMan.scalingProperty().bind(scalingProperty());

        arcadeMazesImage = assets.image("tengen.mazes.arcade");
        nonArcadeMazesImage = assets.image("tengen.mazes.non_arcade");
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }


    @Override
    public void setFlashMode(boolean flashMode) {
        this.flashMode = flashMode;
    }

    @Override
    public void setBlinkingOn(boolean blinkingOn) {
        this.blinkingOn = blinkingOn;
    }

    @Override
    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorPy;
    }

    @Override
    public void drawWorld(GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        TileMap terrain = world.map().terrain();
        if (flashMode) {
            // Flash mode uses vector rendering
            Color wallFillColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_FILL, Color.GREEN);
            terrainRenderer.setWallStrokeColor(Color.WHITE);
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : wallFillColor);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(ctx(), terrain);
        } else {
            if (mapSprite == null) {
                Logger.error("No map sprite selected");
                return;
            }
            // map sprite is selected when game level starts, so it should always be set here
            ctx().drawImage(mapSprite.source(),
                mapSprite.area().x() + 0.5, mapSprite.area().y() + 0.5,
                mapSprite.area().width() - 1, mapSprite.area().height() - 1,
                0, scaled(3 * TS), scaled(mapSprite.area().width()), scaled(mapSprite.area().height()));
            hideActorSprite(terrain.getTileProperty("pos_pac", v2i(14, 26)), 0, 0);
            hideActorSprite(terrain.getTileProperty("pos_ghost_1_red", v2i(13, 14)), 0, 0);
            // The ghosts in the house are sitting some pixels below their home position
            // TODO: check if they really start from the bottom of the house, if yes, change map properties
            hideActorSprite(terrain.getTileProperty("pos_ghost_2_pink",   v2i(13, 17)), 0, 4);
            hideActorSprite(terrain.getTileProperty("pos_ghost_3_cyan",   v2i(11, 17)), 0, 4);
            hideActorSprite(terrain.getTileProperty("pos_ghost_4_orange", v2i(15, 17)), 0, 4);
            ctx().save();
            ctx().scale(scalingPy.get(), scalingPy.get());
            // Food
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            ctx().restore();
            context.game().bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
        }
    }

    @Override
    public void selectMap(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet) {
        int width = worldMap.terrain().numCols() * TS;
        int height = (worldMap.terrain().numRows() - 5) * TS; // 3 empty rows before and 2 after maze source
        mapSprite = mapNumber <= 9  // Maps 1-9 are the Arcade maps, maps 10+ are the non-Arcade maps
            ? new ImageArea(arcadeMazesImage, arcadeMapArea(mapNumber, width, height))
            : new ImageArea(nonArcadeMazesImage, nonArcadeMapArea(mapNumber - 9, width, height));
        Logger.info("Tengen map # {}: area: {}", mapNumber, mapSprite.area());
    }

    /**
     * @param mapNumber number of Arcade map (1-9)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map area in Arcade maps image
     */
    private RectArea arcadeMapArea(int mapNumber, int width, int height) {
        int index = mapNumber - 1;
        return new RectArea((index % 3) * width, (index / 3) * height, width, height);
    }

    /**
     * @param mapNumber number of non-Arcade map (1-37)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map area in non-Arcade maps image
     */
    private RectArea nonArcadeMapArea(int mapNumber, int width, int height) {
        int col, y;
        switch (mapNumber) {
            case 1,2,3,4,5,6,7,8            -> { col = (mapNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { col = (mapNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { col = (mapNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { col = (mapNumber - 25); y = 840;  }
            case 34,35,36,37                -> { col = (mapNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + mapNumber);
        }
        return new RectArea(col * width, y, width, height);
    }

    private void hideActorSprite(Vector2i tile, double offX, double offY) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS + TS + offX;
        double cy = tile.y() * TS + HTS + offY;
        double spriteSize = 2 * TS;
        ctx().setFill(backgroundColorProperty().get());
        ctx().fillRect(scaled(cx - TS), scaled(cy - TS), scaled(spriteSize), scaled(spriteSize));
    }

    //TODO temporary hack until Tengen sprite sheet is usable

    @Override
    public void drawAnimatedEntity(AnimatedEntity guy) {
        if (guy.entity() instanceof MovingBonus) {
            rendererMsPacMan.drawAnimatedEntity(guy);
        } else {
            GameWorldRenderer.super.drawAnimatedEntity(guy);
        }
    }
}