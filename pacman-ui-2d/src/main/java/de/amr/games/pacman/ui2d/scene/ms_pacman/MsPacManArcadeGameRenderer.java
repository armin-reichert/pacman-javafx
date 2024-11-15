/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.ms_pacman.MapConfigurationManager;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class MsPacManArcadeGameRenderer implements GameRenderer {

    private static final RectArea[] MAPS_WITH_FOOD_SPRITES = {
            rect(0,     0, 226, 248),
            rect(0,   248, 226, 248),
            rect(0, 2*248, 226, 248),
            rect(0, 3*248, 226, 248),
            rect(0, 4*248, 226, 248),
            rect(0, 5*248, 226, 248),
    };

    private static final RectArea[] MAPS_WITHOUT_FOOD_SPRITES = {
            rect(228,     0, 226, 248),
            rect(228,   248, 226, 248),
            rect(228, 2*248, 226, 248),
            rect(228, 3*248, 226, 248),
            rect(228, 4*248, 226, 248),
            rect(228, 5*258, 226, 248),
    };

    private static final RectArea[] FLASHING_MAP_SPRITES = {
            rect(0,     0, 226, 248),
            rect(0,   248, 226, 248),
            rect(0, 2*248, 226, 248),
            rect(0, 3*248, 226, 248),
            rect(0, 4*248, 226, 248),
            rect(0, 5*248, 226, 248),
    };

    private final AssetStorage assets;
    private final Canvas canvas;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private RectArea mapWithFoodSprite;
    private RectArea mapWithoutFoodSprite;
    private ImageArea mapFlashingSprite;
    private boolean flashMode;
    private boolean blinkingOn;
    private Color bgColor = Color.BLACK;

    public MsPacManArcadeGameRenderer(AssetStorage assets, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.canvas = checkNotNull(canvas);
        flashingMazesImage = assets.get("ms_pacman.flashing_mazes");
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public MsPacManGameSpriteSheet spriteSheet() {
        return assets.get("ms_pacman.spritesheet");
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
    public Vector2f getMessageAnchorPosition() {
        return new Vector2f(14 * TS, 21 * TS);
    }

    @Override
    public void update(GameLevel level) {
        if (level.mapConfig() != null) {
            // select map sprites for current color scheme
            var colorScheme = level.mapConfig().colorScheme();
            int index = MapConfigurationManager.colorSchemeIndex(colorScheme);
            if (index != -1) {
                mapWithFoodSprite    = MAPS_WITH_FOOD_SPRITES[index];
                mapWithoutFoodSprite = MAPS_WITHOUT_FOOD_SPRITES[index];
                mapFlashingSprite    =  new ImageArea(flashingMazesImage, FLASHING_MAP_SPRITES[index]);
            } else {
                Logger.error("Could not identify color scheme {}", colorScheme);
            }
        }
    }

    @Override
    public void drawWorld(GameContext context, GameWorld world, double x, double y) {
        if (flashMode) {
            if (blinkingOn) {
                drawSubImageScaled(mapFlashingSprite.source(), mapFlashingSprite.area(), x - 3, y); //TODO: WTF
            } else {
                drawSpriteScaled(mapWithoutFoodSprite, x, y);
            }
        } else {
            ctx().save();
            ctx().scale(scalingPy.get(), scalingPy.get());
            drawSpriteUnscaled(mapWithFoodSprite, x, y);
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            ctx().restore();
        }
        context.level().bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet(), (MovingBonus) bonus));
    }

    public void drawClapperBoard(Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(MsPacManGameSpriteSheet.CLAPPERBOARD_SPRITES);
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(sprite, x, y);
            ctx().setFont(font);
            ctx().setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            ctx().setFill(textColor);
            ctx().fillText(animation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            ctx().fillText(animation.text(), textX, numberY);
        }
    }

    public void drawMovingBonus(GameSpriteSheet spriteSheet, MovingBonus bonus) {
        ctx().save();
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawSprite(bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawSprite(bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    public void drawMsPacManMidwayCopyright(double x, double y, Color color, Font font) {
        Image image = assets.get("ms_pacman.logo.midway");
        drawImageScaled(image, x, y + 2, t(4) - 2, t(4));
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx().fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx().fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}