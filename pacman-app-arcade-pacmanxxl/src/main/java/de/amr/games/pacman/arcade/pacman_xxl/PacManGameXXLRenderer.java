/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.tilemap.rendering.FoodMapRenderer;
import de.amr.games.pacman.tilemap.rendering.TerrainColorScheme;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static java.util.function.Predicate.not;

/**
 * As this game variant allows playing arbitrary custom maps, we use a
 * vector graphics rendering that draws wall and obstacle paths generated from
 * the map data.
 *
 * @author Armin Reichert
 */
public class PacManGameXXLRenderer implements GameRenderer {

    static final Vector2f DEFAULT_MESSAGE_ANCHOR_POSITION = new Vector2f(14f * TS, 21 * TS);

    private final AssetStorage assets;
    private final GameSpriteSheet spriteSheet;
    private final Canvas canvas;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainRenderer terrainRenderer = new TerrainRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private Vector2f messageAnchorPosition;
    private boolean mazeHighlighted;
    private boolean blinkingOn;
    private Color bgColor;
    private TerrainColorScheme blinkingOnColors;
    private TerrainColorScheme blinkingOffColors;

    public PacManGameXXLRenderer(AssetStorage assets, PacManGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = assertNotNull(assets);
        this.canvas = assertNotNull(canvas);
        this.spriteSheet = assertNotNull(spriteSheet);
        terrainRenderer.scalingPy.bind(scalingPy);
        foodRenderer.scalingPy.bind(scalingPy);
        messageAnchorPosition = DEFAULT_MESSAGE_ANCHOR_POSITION;
        setBackgroundColor(Color.BLACK);
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void setMazeHighlighted(boolean highlighted) {
        mazeHighlighted = highlighted;
    }

    @Override
    public void setBlinking(boolean blinking) {
        this.blinkingOn = blinking;
    }

    @Override
    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = assertNotNull(color);
        blinkingOnColors = new TerrainColorScheme(bgColor, Color.BLACK, Color.WHITE, Color.BLACK);
        blinkingOffColors = new TerrainColorScheme(bgColor, Color.WHITE, Color.BLACK, Color.BLACK);
    }

    @Override
    public Vector2f getMessagePosition() {
        return messageAnchorPosition;
    }

    public void setMessagePosition(Vector2f messageAnchorPosition) {
        this.messageAnchorPosition = messageAnchorPosition;
    }

    @Override
    public void drawWorld(GameWorld world, double x, double y) {
        WorldMap worldMap = world.map();
        if (mazeHighlighted) {
            terrainRenderer.setColors(blinkingOn ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.drawTerrain(ctx(), worldMap.terrain(), worldMap.obstacles());
        }
        else {
            Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
            TerrainColorScheme colors = new TerrainColorScheme(
                bgColor,
                Color.web(colorMap.get("fill")),
                Color.web(colorMap.get("stroke")),
                Color.web(colorMap.get("door"))
            );
            terrainRenderer.setColors(colors);
            terrainRenderer.drawTerrain(ctx(), worldMap.terrain(), worldMap.obstacles());
            terrainRenderer.drawHouse(ctx(), world.houseTopLeftTile(), world.houseSize());
            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
            worldMap.food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
        }
    }

    public void drawBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawActorSprite(bonus.actor(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawActorSprite(bonus.actor(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}