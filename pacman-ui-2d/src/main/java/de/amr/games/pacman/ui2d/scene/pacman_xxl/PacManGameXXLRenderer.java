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
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

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
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public PacManGameXXLRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        spriteSheet = assets.get("pacman_xxl.spritesheet");
        terrainRenderer.scalingPy.bind(scalingPy);
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
    public void setRendererFor(GameModel game) {}

    @Override
    public void drawWorld(GameContext context, GameWorld world) {
        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());
        if (flashMode) {
            terrainRenderer.setWallStrokeColor(blinkingOn ? Color.WHITE : Color.BLACK);
            terrainRenderer.setWallFillColor(blinkingOn   ? Color.BLACK : Color.WHITE);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(ctx(), world.map().terrain());
        }
        else {
            terrainRenderer.setWallStrokeColor(Color.web(world.map().colorSchemeOrDefault().stroke()));
            terrainRenderer.setWallFillColor(Color.web(world.map().colorSchemeOrDefault().fill()));
            terrainRenderer.setDoorColor(Color.web(world.map().colorSchemeOrDefault().door()));
            terrainRenderer.drawMap(ctx(), world.map().terrain());
            foodRenderer.setPelletColor(Color.web(world.map().colorSchemeOrDefault().pellet()));
            foodRenderer.setEnergizerColor(Color.web(world.map().colorSchemeOrDefault().pellet()));
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