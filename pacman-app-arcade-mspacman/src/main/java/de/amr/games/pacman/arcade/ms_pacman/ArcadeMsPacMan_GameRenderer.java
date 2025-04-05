/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui._2d.GameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_GameRenderer implements GameRenderer {

    private record ImageRegion(Image image, RectArea area) {}

    private static final RectArea[] FULL_MAZE_REGIONS = {
        rect(0,     0, 224, 248),
        rect(0,   248, 224, 248),
        rect(0, 2*248, 224, 248),
        rect(0, 3*248, 224, 248),
        rect(0, 4*248, 224, 248),
        rect(0, 5*248, 224, 248),
    };

    private static final RectArea[] EMPTY_MAZE_REGIONS = {
        rect(228,     0, 224, 248),
        rect(228,   248, 224, 248),
        rect(228, 2*248, 224, 248),
        rect(228, 3*248, 224, 248),
        rect(228, 4*248, 224, 248),
        rect(228, 5*258, 224, 248),
    };

    private static final RectArea[] FLASHING_MAZE_REGIONS = {
        rect(0,     0, 224, 248),
        rect(0,   248, 224, 248),
        rect(0, 2*248, 224, 248),
        rect(0, 3*248, 224, 248),
        rect(0, 4*248, 224, 248),
        rect(0, 5*248, 224, 248),
    };

    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final GraphicsContext ctx;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final Image flashingMazesImage;
    private RectArea fullMazeSpritesheetRegion;
    private RectArea emptyMazeSpritesheetRegion;
    private ImageRegion flashingMazeImageRegion;

    public ArcadeMsPacMan_GameRenderer(ArcadeMsPacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = assertNotNull(spriteSheet);
        ctx = assertNotNull(canvas).getGraphicsContext2D();
        //TODO maybe create flashing maze from normal image at runtime by color exchanges?
        flashingMazesImage = THE_UI.assets().get("ms_pacman.flashing_mazes");
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() {
        return ctx.getCanvas();
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void applyMapSettings(WorldMap worldMap) {
        int colorMapIndex = worldMap.getConfigValue("colorMapIndex");
        fullMazeSpritesheetRegion = FULL_MAZE_REGIONS[colorMapIndex];
        emptyMazeSpritesheetRegion = EMPTY_MAZE_REGIONS[colorMapIndex];
        flashingMazeImageRegion = new ImageRegion(flashingMazesImage, FLASHING_MAZE_REGIONS[colorMapIndex]);
    }

    @Override
    public void drawMaze(GameLevel level, double x, double y, Paint backgroundColor, boolean mazeHighlighted, boolean blinking) {
        if (mazeHighlighted) {
            drawImageRegionScaled(flashingMazeImageRegion.image(), flashingMazeImageRegion.area(), x, y);
        } else if (level.uneatenFoodCount() == 0) {
            drawSpriteScaled(emptyMazeSpritesheetRegion, x, y);
        } else {
            drawSpriteScaled(fullMazeSpritesheetRegion, x, y);
            ctx.save();
            ctx.scale(scaling(), scaling());
            overPaintEatenPelletTiles(level, backgroundColor);
            overPaintEnergizerTiles(level, tile -> !blinking || level.hasEatenFoodAt(tile), backgroundColor);
            ctx.restore();
        }
    }

    public void drawBonus(Bonus bonus) {
        MovingBonus movingBonus = (MovingBonus) bonus;
        ctx.save();
        ctx.setImageSmoothing(false);
        ctx.translate(0, movingBonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawActorSprite(bonus.actor(), spriteSheet().bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawActorSprite(bonus.actor(), spriteSheet().bonusValueSprite(bonus.symbol()));
        }
        ctx.restore();
    }

    public void drawClapperBoard(ClapperboardAnimation clapperboardAnimation, double x, double y) {
        clapperboardAnimation.currentSprite().ifPresent(sprite -> {
            Font font = THE_UI.assets().scaledArcadeFont(scaled(TS));
            drawSpriteScaledCenteredOverTile(sprite, x, y);
            Color textColor = Color.web(Arcade.Palette.WHITE);
            ctx.setFont(font);
            ctx.setFill(textColor.darker());
            var numberX = scaled(x + sprite.width() - 25);
            var numberY = scaled(y + 18);
            ctx.setFill(textColor);
            ctx.fillText(clapperboardAnimation.number(), numberX, numberY);
            var textX = scaled(x + sprite.width());
            ctx.fillText(clapperboardAnimation.text(), textX, numberY);
        });
    }

    public void drawMsPacManMidwayCopyright(double x, double y, Color color, Font font) {
        Image image = THE_UI.assets().get("ms_pacman.logo.midway");
        ctx.drawImage(image, scaled(x), scaled(y + 2), scaled(tiles_to_px(4) - 2), scaled(tiles_to_px(4)));
        ctx.setFont(font);
        ctx.setFill(color);
        ctx.fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        ctx.fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}