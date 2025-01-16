/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.ImageArea;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer implements GameRenderer {

    private static final RectArea[] FULL_MAP_SPRITES = {
        rect(0,     0, 224, 248),
        rect(0,   248, 224, 248),
        rect(0, 2*248, 224, 248),
        rect(0, 3*248, 224, 248),
        rect(0, 4*248, 224, 248),
        rect(0, 5*248, 224, 248),
    };

    private static final RectArea[] EMPTY_MAP_SPRITES = {
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
    private boolean mazeHighlighted;
    private boolean blinking;
    private Color bgColor = Color.BLACK;

    public MsPacManGameRenderer(AssetStorage assets, MsPacManGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = assertNotNull(assets);
        this.spriteSheet = assertNotNull(spriteSheet);
        this.canvas = assertNotNull(canvas);
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
    public void setMazeHighlighted(boolean highlighted) {
        mazeHighlighted = highlighted;
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
        bgColor = assertNotNull(color);
    }

    @Override
    public Vector2f getMessagePosition() {
        return MESSAGE_POSITION;
    }

    @Override
    public void setMessagePosition(Vector2f position) {}

    @Override
    public void setWorldMap(WorldMap worldMap) {
        int colorMapIndex = worldMap.getConfigValue("colorMapIndex");
        fullMapSprite = FULL_MAP_SPRITES[colorMapIndex];
        emptyMapSprite = EMPTY_MAP_SPRITES[colorMapIndex];
        flashingMapSprite = new ImageArea(flashingMazesImage, FLASHING_MAP_SPRITES[colorMapIndex]);
    }

    @Override
    public void drawWorld(GameWorld world, double x, double y) {
        if (mazeHighlighted) {
            drawSubImageScaled(flashingMapSprite.source(), flashingMapSprite.area(), x, y);
        } else if (world.uneatenFoodCount() == 0) {
            drawSpriteScaled(emptyMapSprite, x, y);
        } else {
            drawSpriteScaled(fullMapSprite, x, y);
            ctx().save();
            ctx().scale(scaling(), scaling());
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
            case Bonus.STATE_EDIBLE -> drawActorSprite(bonus.actor(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawActorSprite(bonus.actor(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
        ctx().restore();
    }

    public void drawClapperBoard(ClapperboardAnimation clapperboardAnimation, double x, double y) {
        clapperboardAnimation.currentSprite().ifPresent(sprite -> {
            drawSpriteCenteredOverTile(sprite, x, y);
            Color textColor = Color.valueOf(Arcade.Palette.WHITE);
            ctx().setFont(scaledArcadeFont(TS));
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
        drawImageScaled(image, x, y + 2, tiles2Px(4) - 2, tiles2Px(4));
        ctx().setFont(font);
        ctx().setFill(color);
        ctx().fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx().fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx().fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}