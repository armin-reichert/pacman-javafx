/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static java.lang.Math.clamp;
import static java.util.Objects.requireNonNull;

/**
 * Boot scene simulating the boot process of the Arcade machine. Shows random hex codes, sprite fragments
 * and a grid before the intro scene starts. This scene is used by the Arcade and the XXL variants so we pass the
 * corresponding spritesheet as a parameter.
 */
public class Arcade_BootScene2D_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    public static final int GRID_SIZE = 16;

    private final SpriteSheet<?> spriteSheet;
    private final Rectangle2D spriteRegion;

    public Arcade_BootScene2D_Renderer(
        UIPreferences prefs,
        GameScene2D scene,
        Canvas canvas,
        SpriteSheet<?> spriteSheet,
        Rectangle2D spriteRegion)
    {
        super(canvas);
        requireNonNull(prefs);
        requireNonNull(scene);
        this.spriteSheet = requireNonNull(spriteSheet);
        this.spriteRegion = requireNonNull(spriteRegion);

        createDefaultDebugInfoRenderer(prefs, scene, canvas);
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(GameScene2D scene) {
        final TickTimer stateTimer = scene.context().currentGame().control().state().timer();
        final boolean fourthTick = stateTimer.tickCount() % 4 == 0;
        if (stateTimer.tickCount() == 1) {
            clearCanvas();
        }
        else if (stateTimer.betweenSeconds(1, 2) && fourthTick) {
            showRandomHexDigits(scene);
        }
        else if (stateTimer.betweenSeconds(2, 3.5) && fourthTick) {
            showRandomSpriteFragments(scene);
        }
        else if (stateTimer.atSecond(3.5)) {
            showGrid(scene);
        }
        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }

    private void showRandomHexDigits(GameScene2D scene) {
        final Vector2i sceneSize = scene.unscaledSize();
        final int numRows = sceneSize.y() / TS;
        final int numCols = sceneSize.x() / TS;
        fillCanvas(backgroundColor());
        ctx.setFill(ARCADE_WHITE);
        ctx.setFont(arcadeFont8());
        for (int row = 0; row < numRows; ++row) {
            final double y = scaled(TS(row + 1));
            for (int col = 0; col < numCols; ++col) {
                final byte hexDigit = randomByte(0, 16);
                ctx.fillText(Integer.toHexString(hexDigit), scaled(TS(col)), y);
            }
        }
    }

    private void showRandomSpriteFragments(GameScene2D scene) {
        final Vector2i sceneSize = scene.unscaledSize();
        final int numRows = sceneSize.y() / GRID_SIZE;
        final int numCols = sceneSize.x() / GRID_SIZE;
        fillCanvas(backgroundColor());
        for (int row = 0; row < numRows; ++row) {
            if (randomInt(0, 100) < 20) continue;
            final RectShort fragment1 = randomSpriteFragment();
            final RectShort fragment2 = randomSpriteFragment();
            final int splitCol = numCols / 8 + randomInt(0, numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                drawSprite(col < splitCol ? fragment1 : fragment2, GRID_SIZE * col, GRID_SIZE * row, true);
            }
        }
    }

    private RectShort randomSpriteFragment() {
        double xMin = lerp(spriteRegion.getMinX(), spriteRegion.getMaxX(), randomFloat(0, 1));
        xMin = clamp(xMin, spriteRegion.getMinX(), spriteRegion.getMaxX() - GRID_SIZE);
        double yMin = lerp(spriteRegion.getMinY(), spriteRegion.getMaxY(), randomFloat(0, 1));
        yMin = clamp(yMin, spriteRegion.getMinY(), spriteRegion.getMaxY() - GRID_SIZE);
        return new RectShort((short) xMin, (short) yMin, GRID_SIZE, GRID_SIZE);
    }

    private void showGrid(GameScene2D scene) {
        final Vector2i sceneSize = scene.unscaledSize();
        final double gridWidth = scaled(sceneSize.x());
        final double gridHeight = scaled(sceneSize.y());
        final int numRows = (int) (gridHeight / GRID_SIZE);
        final int numCols = (int) (gridWidth / GRID_SIZE);
        final double thin = scaled(2), thick = scaled(4);
        fillCanvas(backgroundColor());
        ctx.setStroke(ARCADE_WHITE);
        for (int row = 0; row <= numRows; ++row) {
            final double y = scaled(row * GRID_SIZE);
            ctx.setLineWidth(row == 0 || row == numRows ? thick : thin);
            ctx.strokeLine(0, y, gridWidth, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            final double x = scaled(col * GRID_SIZE);
            ctx.setLineWidth(col == 0 || col == numCols ? thick : thin);
            ctx.strokeLine(x, 0, x, gridHeight);
        }
    }
}