/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;

import static de.amr.basics.math.MathAdds.lerp;
import static de.amr.basics.math.RandomNumbers.randomFloat;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static de.amr.basics.math.RectShort.sprite;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.lang.Math.clamp;
import static java.util.Objects.requireNonNull;

/**
 * Boot scene simulating the boot process of the Arcade machine. Shows random hex codes, sprite fragments
 * and a grid before the intro scene starts. This scene is used by the Arcade and the XXL variants so we pass the
 * corresponding spritesheet as a parameter.
 */
public class Arcade_BootScene2D_Renderer extends BaseRenderer implements SpriteRenderer {

    public static final int GRID_SIZE = 16;

    private final SpriteSheet spriteSheet;
    private final Rectangle2D spriteRegion;

    public Arcade_BootScene2D_Renderer(GameScene gameScene, Canvas canvas, SpriteSheet spriteSheet, Rectangle2D spriteRegion) {
        super(canvas);
        requireNonNull(gameScene);
        this.spriteSheet = requireNonNull(spriteSheet);
        this.spriteRegion = requireNonNull(spriteRegion);

        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof Arcade_BootScene2D bootScene)) {
            return;
        }

        final SceneCanvasRenderingComp r2D = bootScene.reqComp(SceneCanvasRenderingComp.class);
        switch (bootScene.sceneState) {
            case BLANK -> clearCanvas();
            case HEX_CODES -> {
                if (tick % 4 == 0) {
                    clearCanvas();
                    drawRandomHexDigits(bootScene, r2D.unscaledWidth(), r2D.unscaledHeight());
                }
            }
            case RANDOM_SPRITE_FRAGMENTS -> {
                if (tick % 4 == 0) {
                    clearCanvas();
                    drawRandomSpriteFragments(r2D.unscaledWidth(), r2D.unscaledHeight());
                }
            }
            case GRID -> {
                clearCanvas();
                drawGrid(r2D.unscaledWidth(), r2D.unscaledHeight());
            }
        }
    }

    private void drawRandomHexDigits(Arcade_BootScene2D bootScene, int width, int height) {
        final int numRows = height / TS;
        final int numCols = width / TS;
        ctx.setFill(ARCADE_WHITE);
        ctx.setFont(arcadeFont8());
        for (int row = 0; row < numRows; ++row) {
            final double y = scaled(TS * (row + 1));
            for (int col = 0; col < numCols; ++col) {
                final double x = scaled(TS * col);
                final int i = randomInt(0, bootScene.noise.length- 1);
                ctx.fillText(bootScene.noise[i], x, y);
            }
        }
    }

    private void drawRandomSpriteFragments(int width, int height) {
        final int numRows = height / GRID_SIZE;
        final int numCols = width / GRID_SIZE;
        for (int row = 0; row < numRows; ++row) {
            if (randomInt(0, 100) < 33) continue;
            final RectShort f1 = randomSpriteFragment();
            final RectShort f2 = randomSpriteFragment();
            final int splitCol = numCols / 8 + randomInt(0, numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                drawSprite(col < splitCol ? f1 : f2, GRID_SIZE * col, GRID_SIZE * row, true);
            }
        }
    }

    private RectShort randomSpriteFragment() {
        double xMin = lerp(spriteRegion.getMinX(), spriteRegion.getMaxX(), randomFloat(0, 1));
        xMin = clamp(xMin, spriteRegion.getMinX(), spriteRegion.getMaxX() - GRID_SIZE);
        double yMin = lerp(spriteRegion.getMinY(), spriteRegion.getMaxY(), randomFloat(0, 1));
        yMin = clamp(yMin, spriteRegion.getMinY(), spriteRegion.getMaxY() - GRID_SIZE);
        return sprite((short) xMin, (short) yMin, GRID_SIZE, GRID_SIZE);
    }

    private void drawGrid(int width, int height) {
        final double gridWidth = scaled(width);
        final double gridHeight = scaled(height);
        final int numRows = (int) (gridHeight / GRID_SIZE);
        final int numCols = (int) (gridWidth / GRID_SIZE);
        final double thin = scaled(2), thick = scaled(4);
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