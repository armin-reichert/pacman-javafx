/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.ImageArea;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer implements GameRenderer {

    static final List<WorldMapColoring> WORLD_MAP_COLORINGS = List.of(
        new WorldMapColoring("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColoring("47B7FF", "DEDEFF", "FCB5FF","FFFF00"),
        new WorldMapColoring("DE9751", "DEDEFF", "FCB5FF","FF0000"),
        new WorldMapColoring("2121FF", "FFB751", "FCB5FF","DEDEFF"),
        new WorldMapColoring("FFB7FF", "FFFF00", "FCB5FF","00FFFF"),
        new WorldMapColoring("FFB7AE", "FF0000", "FCB5FF","DEDEFF")
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

    private static final Vector2f MESSAGE_POSITION = new Vector2f(14 * TS, 21 * TS);

    private final AssetStorage assets;
    private final MsPacManGameSpriteSheet spriteSheet;
    private final Canvas canvas;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final Image flashingMazesImage;
    private RectArea fullMapSprite;
    private RectArea emptyMapSprite;
    private ImageArea flashingMapSprite;
    private boolean flashMode;
    private boolean blinking;
    private Color bgColor = Color.BLACK;

    public MsPacManGameRenderer(AssetStorage assets, MsPacManGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.spriteSheet = checkNotNull(spriteSheet);
        this.canvas = checkNotNull(canvas);
        flashingMazesImage = assets.get("ms_pacman.flashing_mazes");
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public MsPacManGameSpriteSheet spriteSheet() {
        return spriteSheet;
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
    public Vector2f getMessagePosition() {
        return MESSAGE_POSITION;
    }

    @Override
    public void setMessagePosition(Vector2f position) {}

    @Override
    public void setWorldMap(WorldMap worldMap) {
        int index = worldMap.getConfigValue("colorMapIndex");
        fullMapSprite = FULL_MAPS_SPRITES[index];
        emptyMapSprite = EMPTY_MAPS_SPRITES[index];
        flashingMapSprite = new ImageArea(flashingMazesImage, FLASHING_MAP_SPRITES[index]);
    }

    @Override
    public void drawWorld(GameWorld world, double x, double y) {
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
    }

    public void drawBonus(Bonus bonus) {
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