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
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheetArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.variant.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManArcadeGameRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameWorldRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final AssetStorage assets;
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();

    //TODO temporary
    private final MsPacManArcadeGameRenderer rendererMsPacMan;

    private SpriteSheetArea mapSprite;
    private boolean flashMode;
    private boolean blinkingOn;

    public TengenMsPacManGameRenderer(AssetStorage assets) {
        this.assets = assets;
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());

        rendererMsPacMan = new MsPacManArcadeGameRenderer(assets);
        rendererMsPacMan.scalingProperty().bind(scalingProperty());
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
    public void drawWorld(GraphicsContext g, GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        TileMap terrain = world.map().terrain();
        if (flashMode) {
            // Flash mode uses vector rendering
            terrainRenderer.setWallStrokeColor(blinkingOn ? Color.WHITE : Color.BLACK);
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(g, terrain);
        } else {
            if (mapSprite == null) {
                Logger.error("No map sprite selected");
                return;
            }
            // map sprite is selected when game level starts, so it should always be set here
            g.drawImage(mapSprite.source(),
                mapSprite.area().x() + 0.5, mapSprite.area().y() + 0.5,
                mapSprite.area().width() - 1, mapSprite.area().height() - 1,
                0, scaled(3 * TS), scaled(mapSprite.area().width()), scaled(mapSprite.area().height()));
            hideActorSprite(g, terrain.getTileProperty("pos_pac", v2i(14, 26)), 0, 0);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_1_red", v2i(13, 14)), 0, 0);
            // The ghosts in the house are sitting some pixels below their home position
            // TODO: check if they really start from the bottom of the house, if yes, change map properties
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_2_pink",   v2i(13, 17)), 0, 4);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_3_cyan",   v2i(11, 17)), 0, 4);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_4_orange", v2i(15, 17)), 0, 4);
            g.save();
            g.scale(scalingPy.get(), scalingPy.get());
            // Food
            overPaintEatenPellets(g, world);
            overPaintEnergizers(g, world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            g.restore();
            context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, spriteSheet, (MovingBonus) bonus));
        }
    }

    @Override
    public void selectMap(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet) {
        int width = worldMap.terrain().numCols() * TS;
        int height = (worldMap.terrain().numRows() - 5) * TS; // 3 empty rows before and 2 after maze source
        // Maps 1-9 are the Arcade maps, maps 10+ are the non-Arcade maps
        mapSprite = mapNumber <= 9
                ? new SpriteSheetArea(assets.get("tengen.mazes.arcade"), arcadeMapArea(mapNumber, width, height))
                : new SpriteSheetArea(assets.get("tengen.mazes.non_arcade"), nonArcadeMapArea(mapNumber - 9, width, height));
        Logger.info("Tengen map # {}: area: {}", mapNumber, mapSprite.area());
    }

    /**
     *
     * @param mapNumber number of Arcade map (1-9)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return sprite sheet area for map image
     */
    private RectArea arcadeMapArea(int mapNumber, int width, int height) {
        if (1 <= mapNumber && mapNumber <= 9) {
            int index = mapNumber - 1;
            return new RectArea((index % 3) * width, (index / 3) * height, width, height);
        }
        throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
    }

    /**
     * @param mapNumber number of non-Arcade map (1-37)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return sprite sheet area for map image
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

    @Override
    public void drawMovingBonus(GraphicsContext g, GameSpriteSheet spriteSheet, MovingBonus bonus) {
        g.save();
        g.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(g, bonus.entity(), spriteSheet, spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(g, bonus.entity(), spriteSheet, spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, GameSpriteSheet spriteSheet, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(g, spriteSheet, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            g.fillText(animation.text(), textX, numberY);
        }
    }

    private void hideActorSprite(GraphicsContext g, Vector2i tile, double offX, double offY) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS + TS + offX;
        double cy = tile.y() * TS + HTS + offY;
        double spriteSize = 2 * TS;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(scaled(cx - TS), scaled(cy - TS), scaled(spriteSize), scaled(spriteSize));
    }

    //TODO temporary


    @Override
    public void drawAnimatedEntity(GraphicsContext g, AnimatedEntity guy) {
        if (guy.entity() instanceof Ghost || guy.entity() instanceof MovingBonus) {
            rendererMsPacMan.drawAnimatedEntity(g, guy);
        } else {
            GameWorldRenderer.super.drawAnimatedEntity(g, guy);
        }
    }
}