/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.EMPTY_MAZE_SPRITE;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.FULL_MAZE_SPRITE;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GameRenderer implements GameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;
    private boolean mazeHighlighted;
    private boolean blinkingOn;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = assertNotNull(spriteSheet);
        ctx = assertNotNull(canvas).getGraphicsContext2D();
    }

    @Override
    public void applyMapSettings(WorldMap worldMap) {}

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() {
        return ctx.getCanvas();
    }

    @Override
    public void setMazeHighlighted(boolean highlighted) {
        this.mazeHighlighted = highlighted;
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
    public Vector2f getMessagePosition() {
        return ArcadePacMan_GameModel.MESSAGE_POSITION;
    }

    @Override
    public void drawMaze(GameLevel level, double x, double y, Paint backgroundColor) {
        double scaling = scaling();
        ctx.save();
        ctx.scale(scaling, scaling);
        if (mazeHighlighted) {
            ctx.drawImage(THE_UI.assets().image("pacman.flashing_maze"), x, y);
        } else {
            if (level.uneatenFoodCount() == 0) {
                drawSpriteUnscaled(EMPTY_MAZE_SPRITE, x, y);
            } else {
                drawSpriteUnscaled(FULL_MAZE_SPRITE, x, y);
                overPaintEatenPelletTiles(level, backgroundColor);
                overPaintEnergizerTiles(level, tile -> !blinkingOn || level.hasEatenFoodAt(tile), backgroundColor);
            }
        }
        ctx.restore();
    }
}