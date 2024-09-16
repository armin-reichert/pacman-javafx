/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.*;
import static java.util.function.Predicate.not;

public class TengenMsPacManGameWorldRenderer implements GameWorldRenderer {

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
            terrainRenderer.setWallFillColor(blinkingOn   ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(g, terrain);
        }
        else {
            terrainRenderer.setWallStrokeColor(wallStrokeColor);
            terrainRenderer.setWallFillColor(wallFillColor);
            terrainRenderer.setDoorColor(doorColor);
            terrainRenderer.drawMap(g, terrain);
            Color foodColor = getColorFromMap(world.map().food(), PROPERTY_COLOR_FOOD, Color.ORANGE);
            foodRenderer.setPelletColor(foodColor);
            foodRenderer.setEnergizerColor(foodColor);
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition)).forEach(tile -> foodRenderer.drawPellet(g, tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
            }
        }
        context.game().bonus().ifPresent(bonus -> drawMovingBonus(g, (MovingBonus) bonus));
    }

    /**
     * Draws a moving bonus entity at its current position (including jump offset).
     * TODO reconsider this way of implementing the jumping bonus
     *
     * @param g     graphics context
     * @param movingBonus moving bonus entity
     */
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

}
