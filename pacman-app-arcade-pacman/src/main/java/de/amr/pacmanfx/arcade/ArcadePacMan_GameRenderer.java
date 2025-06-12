/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ArcadePacMan_SpriteSheet.sprite;
import static de.amr.pacmanfx.ui.PacManGames_Env.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GameRenderer extends SpriteGameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void applyRenderingHints(GameLevel level) {}

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void drawLevel(GameLevel level, double x, double y, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (mazeHighlighted) {
            ctx.drawImage(theAssets().image("pacman.flashing_maze"), x, y);
        }
        else if (level.uneatenFoodCount() == 0) {
            drawSprite(sprite(SpriteID.MAP_EMPTY), x, y);
        } else {
            drawSprite(sprite(SpriteID.MAP_FULL), x, y);
            overPaintEatenPelletTiles(level, backgroundColor);
            overPaintEnergizerTiles(level, tile -> !energizerHighlighted || level.tileContainsEatenFood(tile), backgroundColor);
        }
        ctx.restore();
    }

    @Override
    public void drawLevelCounter(LevelCounter levelCounter, Vector2f sceneSizeInPixels) {
        float x = sceneSizeInPixels.x() - 4 * TS, y = sceneSizeInPixels.y() - 2 * TS;
        for (byte symbol : levelCounter.symbols()) {
            RectArea sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    public void drawBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            RectArea sprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
            drawActorSprite(bonus.actor(), sprite);
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            RectArea sprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
            drawActorSprite(bonus.actor(), sprite);
        }
    }
}