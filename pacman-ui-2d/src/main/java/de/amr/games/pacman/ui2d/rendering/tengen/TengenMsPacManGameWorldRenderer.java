/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.*;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameWorldRenderer implements MsPacManGameWorldRenderer {

    private final AssetStorage assets;
    private MsPacManGameSpriteSheet tmpSpriteSheet;
    private TengenMsPacManGameSpriteSheet tengenSpriteSheet;

    private final SpriteRenderer spriteRenderer = new SpriteRenderer();
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    private boolean flashMode;
    private boolean blinkingOn;

    public TengenMsPacManGameWorldRenderer(AssetStorage assets) {
        this.assets = assets;
        tengenSpriteSheet = assets.get("tengen.spritesheet");
        // for now, just use Ms Pac-Man spritesheet
        tmpSpriteSheet = assets.get("tengen.spritesheet.tmp");
        spriteRenderer.setSpriteSheet(tmpSpriteSheet);
        terrainRenderer.scalingPy.bind(spriteRenderer.scalingPy);
        terrainRenderer.setMapBackgroundColor(spriteRenderer.backgroundColorPy.get());
        foodRenderer.scalingPy.bind(spriteRenderer.scalingPy);
    }

    @Override
    public SpriteRenderer spriteRenderer() {
        return spriteRenderer;
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
        return spriteRenderer.scalingPy;
    }

    @Override
    public ObjectProperty<Color> backgroundColorProperty() {
        return spriteRenderer.backgroundColorPy;
    }

    @Override
    public void drawWorld(GraphicsContext g, GameContext context, GameWorld world) {
        TileMap terrain = world.map().terrain();
        Color wallStrokeColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_STROKE, Color.WHITE);
        Color wallFillColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_FILL, Color.GREEN);
        Color doorColor = getColorFromMap(terrain, PROPERTY_COLOR_DOOR, Color.YELLOW);
        if (flashMode) {
            terrainRenderer.setWallStrokeColor(blinkingOn ? Color.WHITE : Color.BLACK);
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(g, terrain);
        } else {
            MsPacManTengenGame tengenGame = (MsPacManTengenGame) context.game();
            int mapNumber = tengenGame.mapNumberByLevelNumber(tengenGame.levelNumber());
            // Maps 1-9 are the Arcade maps
            if ("Arcade".equals(terrain.getProperty("tengen_category"))) {
                drawArcadeWorld(g, world, mapNumber, 0, 3 * TS);
            } else {
                // Maps 10- are the non-Arcade maps
                drawNonArcadeWorld(g, world, mapNumber - 9, 0, 3 * TS);
            }
            hideActorSprite(g, terrain.getTileProperty("pos_pac", v2i(14, 26)), 0, 0);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_1_red", v2i(13, 14)), 0, 0);
            // The ghosts in the house are sitting some pixels below their home position
            // TODO: check if the ghosts in Tengen all start from the bottom of the house, if yes, change map properties
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_2_pink", v2i(13, 17)), 0, 4);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_3_cyan", v2i(11, 17)), 0, 4);
            hideActorSprite(g, terrain.getTileProperty("pos_ghost_4_orange", v2i(15, 17)), 0, 4);
            world.map().food().tiles().filter(world::hasEatenFoodAt).forEach(tile -> overPaintFood(g, world, tile));
            if (!blinkingOn) {
                world.energizerTiles().forEach(tile -> overPaintFood(g, world, tile));
            }
        }
        context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, (MovingBonus) bonus));
    }

    private void hideActorSprite(GraphicsContext g, Vector2i tile, double offX, double offY) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS + TS + offX;
        double cy = tile.y() * TS + HTS + offY;
        double spriteSize = 2 * TS;
        g.setFill(backgroundColorProperty().get());
        g.fillRect(scaled(cx - TS), scaled(cy - TS), scaled(spriteSize), scaled(spriteSize));
    }

    private void drawArcadeWorld(GraphicsContext g, GameWorld world, int mapNumber, double x, double y) {
        Image mazesImage = assets.get("tengen.mazes.arcade");
        int width = world.map().terrain().numCols() * TS, height = (world.map().terrain().numRows() - 5) * TS;
        int index = mapNumber - 1;
        int rowIndex = index / 3, colIndex = index % 3;
        SpriteArea area = new SpriteArea(colIndex * width, rowIndex * height, width, height);
        double scaling = scalingProperty().get();
        g.save();
        g.scale(scaling, scaling);
        g.drawImage(mazesImage, area.x(), area.y(), area.width(), area.height(), x, y, area.width(), area.height());
        g.restore();
    }

    private void drawNonArcadeWorld(GraphicsContext g, GameWorld world, int mapNumber, double x, double y) {
        Image mazesImage = assets.get("tengen.mazes.non_arcade");
        SpriteArea area = nonArcadeMapArea(world, mapNumber);
        double scaling = scalingProperty().get();
        g.save();
        g.scale(scaling, scaling);
        g.drawImage(mazesImage, area.x(), area.y(), area.width(), area.height(), x, y, area.width(), area.height());
        g.restore();
    }

    private SpriteArea nonArcadeMapArea(GameWorld world, int mapNumber) {
        int width = world.map().terrain().numCols() * TS, height = (world.map().terrain().numRows() - 5) * TS;
        if (mapNumber <= 8) { // first row
            int colIndex = mapNumber - 1;
            return new SpriteArea(colIndex * width, 0, width, height);
        }
        if (mapNumber <= 16) { // second row
            int colIndex = (mapNumber - 1) % 8;
            return new SpriteArea(colIndex * width, 248, width, height);
        }
        if (mapNumber <= 24) {
            return new SpriteArea(0, 0, 224, 248);
        }
        if (mapNumber <= 33){
            return new SpriteArea(0, 0, 224, 248);
        }
        return new SpriteArea(0, 0, 224, 248);
    }

    @Override
    public void drawMovingBonus(GraphicsContext g, MovingBonus movingBonus) {
        g.save();
        g.translate(0, movingBonus.elongationY());
        if (movingBonus.state() == Bonus.STATE_EDIBLE) {
            spriteRenderer.drawEntitySprite(g,  movingBonus.entity(),
                    spriteRenderer.spriteSheet().bonusSymbolSprite(movingBonus.symbol()));
        } else if (movingBonus.state() == Bonus.STATE_EATEN) {
            spriteRenderer.drawEntitySprite(g, movingBonus.entity(),
                    spriteRenderer.spriteSheet().bonusValueSprite(movingBonus.symbol()));
        }
        g.restore();
    }

    @Override
    public void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        double scaling = scalingProperty().get();
        var sprite = animation.currentSprite(tmpSpriteSheet.clapperboardSprites());
        if (sprite != null) {
            spriteRenderer.drawSpriteCenteredOverBox(g, sprite, x, y);
            g.setFont(font);
            g.setFill(textColor.darker());
            var numberX = scaling * (x + sprite.width() - 25);
            var numberY = scaling * (y + 18);
            g.setFill(textColor);
            g.fillText(animation.number(), numberX, numberY);
            var textX = scaling * (x + sprite.width());
            g.fillText(animation.text(), textX, numberY);
        }
    }
}
