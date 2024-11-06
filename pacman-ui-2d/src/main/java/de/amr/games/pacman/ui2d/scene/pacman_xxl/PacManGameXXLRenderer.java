/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman_xxl;

import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static java.util.function.Predicate.not;

/**
 * As this game variant allows playing arbitrary custom maps, we use a
 * vector graphics rendering that draws wall and obstacle paths generated from
 * the map data.
 *
 * @author Armin Reichert
 */
public class PacManGameXXLRenderer implements GameRenderer {

    private final AssetStorage assets;
    private final GameSpriteSheet spriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;
    private Color bgColor;

    public PacManGameXXLRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        spriteSheet = assets.get("pacman_xxl.spritesheet");
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    @Override
    public PacManGameXXLRenderer copy() {
        return new PacManGameXXLRenderer(assets);
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
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        canvas.getGraphicsContext2D().setImageSmoothing(true);
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
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = checkNotNull(color);
    }

    @Override
    public void update(GameModel game) {}

    @Override
    public void drawWorld(GameContext context, GameWorld world, double x, double y) {
        terrainRenderer.setMapBackgroundColor(bgColor);
        if (flashMode) {
            terrainRenderer.setMapBackgroundColor(bgColor);
            terrainRenderer.setWallStrokeColor(blinkingOn ? Color.WHITE : Color.BLACK);
            terrainRenderer.setWallFillColor(blinkingOn   ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(ctx(), world.map().terrain());
        }
        else {
            Map<String, String> mapColorScheme = context.game().currentMapColorScheme();
            terrainRenderer.setMapBackgroundColor(bgColor);
            terrainRenderer.setWallStrokeColor(Color.web(mapColorScheme.get("stroke")));
            terrainRenderer.setWallFillColor(Color.web(mapColorScheme.get("fill")));
            terrainRenderer.setDoorColor(Color.web(mapColorScheme.get("door")));
            terrainRenderer.drawMap(ctx(), world.map().terrain());
            foodRenderer.setPelletColor(Color.web(mapColorScheme.get("pellet")));
            foodRenderer.setEnergizerColor(Color.web(mapColorScheme.get("pellet")));
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
        }
        context.game().bonus().ifPresent(bonus -> drawStaticBonus(spriteSheet, bonus));
    }

    private void drawStaticBonus(GameSpriteSheet spriteSheet, Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            drawSprite(bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            drawSprite(bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
        }
    }
}