/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman_xxl;

import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.SpriteRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.*;
import static java.util.function.Predicate.not;

public class PacManXXLGameWorldRenderer implements GameWorldRenderer {

    private final PacManGameSpriteSheet spriteSheet;
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    private boolean flashMode;
    private boolean blinkingOn;

    public PacManXXLGameWorldRenderer(AssetStorage assets) {
        spriteSheet = assets.get("pacman_xxl.spritesheet");
        spriteRenderer.setSpriteSheet(spriteSheet);
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
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(g, bonus));
    }

    private void drawStaticBonus(GraphicsContext g, Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            spriteRenderer.drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            spriteRenderer.drawEntitySprite(g,  bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}
