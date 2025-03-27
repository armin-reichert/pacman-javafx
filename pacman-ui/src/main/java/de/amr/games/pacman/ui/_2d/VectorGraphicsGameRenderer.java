/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.tilemap.rendering.FoodMapRenderer;
import de.amr.games.pacman.tilemap.rendering.TerrainMapColorScheme;
import de.amr.games.pacman.tilemap.rendering.TerrainMapRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.Globals.assertNotNull;
import static java.util.function.Predicate.not;

/**
 * As this game variant allows playing arbitrary custom maps, we use aButtonKey
 * vector graphics rendering that draws wall and obstacle paths generated from
 * the map data.
 *
 * @author Armin Reichert
 */
public class VectorGraphicsGameRenderer implements GameRenderer {

    static final Vector2f DEFAULT_MESSAGE_ANCHOR_POSITION = new Vector2f(14f * TS, 21 * TS);

    private final GameSpriteSheet spriteSheet;
    private final Canvas canvas;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private Vector2f messageAnchorPosition;
    private boolean mazeHighlighted;
    private boolean blinkingOn;
    private Color bgColor;
    private TerrainMapColorScheme blinkingOnColors;
    private TerrainMapColorScheme blinkingOffColors;

    public VectorGraphicsGameRenderer(GameSpriteSheet spriteSheet, Canvas canvas) {
        this.canvas = assertNotNull(canvas);
        this.spriteSheet = assertNotNull(spriteSheet);
        terrainRenderer.scalingProperty().bind(scalingPy);
        foodRenderer.scalingProperty().bind(scalingPy);
        messageAnchorPosition = DEFAULT_MESSAGE_ANCHOR_POSITION;
        setBackgroundColor(Color.BLACK);
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
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = assertNotNull(color);
        blinkingOnColors = new TerrainMapColorScheme(bgColor, Color.BLACK, Color.WHITE, Color.BLACK);
        blinkingOffColors = new TerrainMapColorScheme(bgColor, Color.WHITE, Color.BLACK, Color.BLACK);
    }

    @Override
    public Vector2f getMessagePosition() {
        return messageAnchorPosition;
    }

    public void setMessagePosition(Vector2f messageAnchorPosition) {
        this.messageAnchorPosition = messageAnchorPosition;
    }

    @Override
    public void drawGameLevel(GameLevel level, double x, double y) {
        WorldMap worldMap = level.worldMap();
        if (mazeHighlighted) {
            terrainRenderer.setColorScheme(blinkingOn ? blinkingOnColors : blinkingOffColors);
            terrainRenderer.drawTerrain(ctx(), worldMap, worldMap.obstacles());
        }
        else {
            Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
            TerrainMapColorScheme colors = new TerrainMapColorScheme(
                bgColor,
                Color.web(colorMap.get("fill")),
                Color.web(colorMap.get("stroke")),
                Color.web(colorMap.get("door"))
            );
            terrainRenderer.setColorScheme(colors);
            terrainRenderer.drawTerrain(ctx(), worldMap, worldMap.obstacles());
            terrainRenderer.drawHouse(ctx(), level.houseMinTile(), level.houseSizeInTiles());
            foodRenderer.setPelletColor(Color.web(colorMap.get("pellet")));
            foodRenderer.setEnergizerColor(Color.web(colorMap.get("pellet")));
            worldMap.tiles().filter(level::hasFoodAt).filter(not(level::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                level.energizerTiles().filter(level::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
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