/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.EMPTY_MAZE_SPRITE;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.FULL_MAZE_SPRITE;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GameRenderer implements GameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final GraphicsContext ctx;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
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
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void drawMaze(GameLevel level, double x, double y, Paint backgroundColor, boolean mazeHighlighted, boolean blinking) {
        double scaling = scaling();
        ctx.save();
        ctx.scale(scaling, scaling);
        if (mazeHighlighted) {
            ctx.drawImage(THE_ASSETS.image("pacman.flashing_maze"), x, y);
        } else {
            if (level.uneatenFoodCount() == 0) {
                drawSpriteSheetRegion(EMPTY_MAZE_SPRITE, x, y);
            } else {
                drawSpriteSheetRegion(FULL_MAZE_SPRITE, x, y);
                overPaintEatenPelletTiles(level, backgroundColor);
                overPaintEnergizerTiles(level, tile -> !blinking || level.hasEatenFoodAt(tile), backgroundColor);
            }
        }
        ctx.restore();
    }
}