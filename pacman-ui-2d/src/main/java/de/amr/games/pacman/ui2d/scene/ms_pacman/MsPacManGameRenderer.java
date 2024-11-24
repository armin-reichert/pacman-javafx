/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer implements GameRenderer {

    public static final List<Map<String, Color>> COLOR_MAPS = List.of(
        Map.of("fill", Color.valueOf("FFB7AE"), "stroke", Color.valueOf("FF0000"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("DEDEFF")),
        Map.of("fill", Color.valueOf("47B7FF"), "stroke", Color.valueOf("DEDEFF"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("FFFF00")),
        Map.of("fill", Color.valueOf("DE9751"), "stroke", Color.valueOf("DEDEFF"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("FF0000")),
        Map.of("fill", Color.valueOf("2121FF"), "stroke", Color.valueOf("FFB751"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("DEDEFF")),
        Map.of("fill", Color.valueOf("FFB7FF"), "stroke", Color.valueOf("FFFF00"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("00FFFF")),
        Map.of("fill", Color.valueOf("FFB7AE"), "stroke", Color.valueOf("FF0000"), "door", Color.valueOf("FCB5FF"), "pellet", Color.valueOf("DEDEFF"))
    );

    private static final RectArea[] FULL_MAPS_SPRITES = {
        rect(0,     0, 224, 248),
        rect(0,   248, 224, 248),
        rect(0, 2*248, 224, 248),
        rect(0, 3*248, 224, 248),
        rect(0, 4*248, 224, 248),
        rect(0, 5*248, 224, 248),
    };

    private static final RectArea[] EMPTY_MAPS_SPRITES = {
        rect(228,     0, 224, 248),
        rect(228,   248, 224, 248),
        rect(228, 2*248, 224, 248),
        rect(228, 3*248, 224, 248),
        rect(228, 4*248, 224, 248),
        rect(228, 5*258, 224, 248),
    };

    private static final RectArea[] FLASHING_MAP_SPRITES = {
        rect(0,     0, 224, 248),
        rect(0,   248, 224, 248),
        rect(0, 2*248, 224, 248),
        rect(0, 3*248, 224, 248),
        rect(0, 4*248, 224, 248),
        rect(0, 5*248, 224, 248),
    };

    private final AssetStorage assets;
    private final Canvas canvas;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private RectArea fullMapSprite;
    private RectArea emptyMapSprite;
    private ImageArea flashingMapSprite;
    private boolean flashMode;
    private boolean blinking;
    private Color bgColor = Color.BLACK;

    public MsPacManGameRenderer(AssetStorage assets, Canvas canvas) {
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
    public void setBlinking(boolean blinking) {
        this.blinking = blinking;
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
    public void update(Map<String, Object> mapConfig) {
        int index = (int) mapConfig.get("colorMapIndex");
        fullMapSprite = FULL_MAPS_SPRITES[index];
        emptyMapSprite = EMPTY_MAPS_SPRITES[index];
        flashingMapSprite = new ImageArea(flashingMazesImage, FLASHING_MAP_SPRITES[index]);
    }

    @Override
    public void drawWorld(GameContext context, GameWorld world, double x, double y) {
        if (flashMode) {
            drawSubImageScaled(flashingMapSprite.source(), flashingMapSprite.area(), x, y);
        } else if (world.uneatenFoodCount() == 0) {
            drawSpriteScaled(emptyMapSprite, x, y);
        } else {
            ctx().save();
            ctx().scale(scaling(), scaling());
            drawSpriteUnscaled(fullMapSprite, x, y);
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinking || world.hasEatenFoodAt(tile));
            ctx().restore();
        }
        context.level().bonus().ifPresent(this::drawBonus);
    }

    private void drawBonus(Bonus bonus) {
        MovingBonus movingBonus = (MovingBonus) bonus;
        ctx().save();
        ctx().translate(0, movingBonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(bonus.entity(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(bonus.entity(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
        ctx().restore();
    }

    public void drawClapperBoard(Font font, Color textColor, ClapperboardAnimation clapperboardAnimation, double x, double y) {
        clapperboardAnimation.currentSprite().ifPresent(sprite -> {
            drawSpriteCenteredOverTile(sprite, x, y);
            ctx().setFont(font);
            ctx().setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            ctx().setFill(textColor);
            ctx().fillText(clapperboardAnimation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            ctx().fillText(clapperboardAnimation.text(), textX, numberY);
        });
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