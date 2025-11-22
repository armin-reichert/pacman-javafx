/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;

public class Arcade_BootScene2D_Renderer extends GameScene2D_Renderer {

    public static final int RASTER_SIZE = 16;

    private final Vector2f minPoint;
    private final Vector2f maxPoint;

    public Arcade_BootScene2D_Renderer(Arcade_BootScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        createDefaultDebugInfoRenderer(canvas, spriteSheet);

        final double width = spriteSheet.sourceImage().getWidth();
        final double height = spriteSheet.sourceImage().getHeight();
        // ignore left half of sprite sheet image containing maze images
        minPoint = Vector2f.of(width / 2, 0);
        maxPoint = Vector2f.of(width - RASTER_SIZE, height - RASTER_SIZE);
    }

    public void draw() {
        TickTimer timer = scene().context().gameState().timer();
        if (timer.tickCount() == 1) {
            clearCanvas();
        }
        // during first second, nothing happens...
        else if (timer.betweenSeconds(1, 2) && timer.tickCount() % 4 == 0) {
            // change pattern every 4th tick
            clearCanvas();
            drawRandomHexDigits();
        }
        else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            // change pattern every 4th tick
            clearCanvas();
            drawRandomSpriteFragments();
        }
        else if (timer.atSecond(3.5)) {
            clearCanvas();
            draw16by16Grid();
        }

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }

    private void drawRandomHexDigits() {
        int numRows = ARCADE_MAP_SIZE_IN_PIXELS.y() / TS;
        int numCols = ARCADE_MAP_SIZE_IN_PIXELS.x() / TS;
        ctx.setFill(ARCADE_WHITE);
        ctx.setFont(arcadeFont8());
        for (int row = 0; row < numRows; ++row) {
            double y = scaled(TS(row + 1));
            for (int col = 0; col < numCols; ++col) {
                int hexDigit = randomInt(0, 16);
                ctx.fillText(Integer.toHexString(hexDigit), scaled(TS(col)), y);
            }
        }
    }

    private void drawRandomSpriteFragments() {
        int numRows = ARCADE_MAP_SIZE_IN_PIXELS.y() / RASTER_SIZE;
        int numCols = ARCADE_MAP_SIZE_IN_PIXELS.x() / RASTER_SIZE;
        for (int row = 0; row < numRows; ++row) {
            if (randomInt(0, 100) < 20) continue;
            RectShort fragment1 = randomSpriteFragment(), fragment2 = randomSpriteFragment();
            int split = numCols / 8 + randomInt(0, numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                drawSprite(col < split ? fragment1 : fragment2, RASTER_SIZE * col, RASTER_SIZE * row, true);
            }
        }
    }

    private RectShort randomSpriteFragment() {
        return new RectShort(
            (int) lerp(minPoint.x(), maxPoint.x(), randomFloat(0, 1)),
            (int) lerp(minPoint.y(), maxPoint.y(), randomFloat(0, 1)),
            RASTER_SIZE, RASTER_SIZE);
    }

    private void draw16by16Grid() {
        double gridWidth = scaled(ARCADE_MAP_SIZE_IN_PIXELS.x());
        double gridHeight = scaled(ARCADE_MAP_SIZE_IN_PIXELS.y());
        int numRows = ARCADE_MAP_SIZE_IN_PIXELS.y() / RASTER_SIZE;
        int numCols = ARCADE_MAP_SIZE_IN_PIXELS.x() / RASTER_SIZE;
        double thin = scaled(2), thick = scaled(4);
        ctx.setStroke(ARCADE_WHITE);
        for (int row = 0; row <= numRows; ++row) {
            ctx.setLineWidth(row == 0 || row == numRows ? thick : thin);
            double y = scaled(row * RASTER_SIZE);
            ctx.strokeLine(0, y, gridWidth, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            ctx.setLineWidth(col == 0 || col == numCols ? thick : thin);
            double x = scaled(col * RASTER_SIZE);
            ctx.strokeLine(x, 0, x, gridHeight);
        }
    }
}