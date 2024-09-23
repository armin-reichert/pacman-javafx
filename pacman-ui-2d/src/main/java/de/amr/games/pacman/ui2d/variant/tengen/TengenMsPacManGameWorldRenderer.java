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
import de.amr.games.pacman.ui2d.rendering.*;
import de.amr.games.pacman.ui2d.variant.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManArcadeGameWorldRenderer;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
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
public class TengenMsPacManGameWorldRenderer implements MsPacManGameWorldRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final AssetStorage assets;
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();

    //TODO temporary
    private final MsPacManArcadeGameWorldRenderer rendererMsPacMan;

    private SpriteSheetArea mapSprite;
    private boolean flashMode;
    private boolean blinkingOn;

    public TengenMsPacManGameWorldRenderer(AssetStorage assets) {
        this.assets = assets;
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());

        rendererMsPacMan = new MsPacManArcadeGameWorldRenderer(assets);
        rendererMsPacMan.scalingProperty().bind(scalingProperty());
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return assets.get("tengen.spritesheet");
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
    public void drawWorld(GraphicsContext g, GameContext context, GameWorld world) {
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
            context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, (MovingBonus) bonus));
        }
    }

    @Override
    public void selectMap(WorldMap worldMap, int mapNumber) {
        int width = worldMap.terrain().numCols() * TS;
        int height = (worldMap.terrain().numRows() - 5) * TS; // 3 empty rows before and 2 after maze source
        // Maps 1-9 are the Arcade maps, maps 10+ are the non-Arcade maps
        mapSprite = mapNumber <= 9
                ? new SpriteSheetArea(assets.get("tengen.mazes.arcade"), arcadeMapArea(mapNumber, width, height))
                : new SpriteSheetArea(assets.get("tengen.mazes.non_arcade"), nonArcadeMapArea(mapNumber - 9, width, height));
        Logger.info("Tengen map # {}: area: {}", mapNumber, mapSprite.area());
    }

    // Maps are all the same size and arranged in a 3x3 grid
    private RectArea arcadeMapArea(int arcadeMapNumber, int width, int height) {
        int index = arcadeMapNumber - 1;
        int rowIndex = index / 3, colIndex = index % 3;
        return new RectArea(colIndex * width, rowIndex * height, width, height);
    }

    // Maps have same width but different height and are arranged in 5 rows
    private RectArea nonArcadeMapArea(int nonArcadeMapNumber, int width, int height) {
        if (nonArcadeMapNumber <= 8) { // row #1, maps 1-8
            int colIndex = nonArcadeMapNumber - 1;
            return new RectArea(colIndex * width, 0, width, height);
        }
        else if (nonArcadeMapNumber <= 16) { // row #2, maps 9-16
            int colIndex = (nonArcadeMapNumber - 1) % 8;
            return new RectArea(colIndex * width, 248, width, height);
        }
        else if (nonArcadeMapNumber <= 24) { // row #3, maps 17-24
            int colIndex = (nonArcadeMapNumber - 1) % 8;
            return new RectArea(colIndex * width, 544, width, height);
        }
        else if (nonArcadeMapNumber <= 33) { // row #4, maps 18-33
            int colIndex = (nonArcadeMapNumber - 1) % 9;
            return new RectArea(colIndex * width, 840, width, height);
        }
        else if (nonArcadeMapNumber <= 37) { // row #5, maps 34-37
            int colIndex = (nonArcadeMapNumber - 1) % 4;
            return new RectArea(colIndex * width, 1136, width, height);
        }
        throw new IllegalArgumentException("Illegal map number: " + nonArcadeMapNumber);
    }

    @Override
    public void drawMovingBonus(GraphicsContext g, MovingBonus bonus) {
        g.save();
        g.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(g, bonus.entity(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(g, bonus.entity(), spriteSheet().bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(spriteSheet().clapperboardSprites());
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(g, sprite, x, y);
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
            MsPacManGameWorldRenderer.super.drawAnimatedEntity(g, guy);
        }
    }
}